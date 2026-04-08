package ru.practicum.service.events;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.client.request.RequestClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.event.EventState;
import ru.practicum.enums.request.ParticipationRequestStatus;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.mapper.events.EventsMapper;
import ru.practicum.model.Events;
import ru.practicum.model.params.PublicEventSearchParams;
import ru.practicum.repository.events.EventsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublicServiceImpl implements EventPublicService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventsRepository eventsRepository;
    private final EventsMapper eventsMapper;
    private final StatsClient statsClient;
    private final RequestClient requestClient;
    private final UserClient userClient;

    @Override
    public List<EventShortDto> getEvents(PublicEventSearchParams params,
                                         HttpServletRequest request) {
        log.info("Поиск публичных событий params={}", params);
        validateSearchParams(params);
        saveHit(request);

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Events> page = eventsRepository.findPublicEvents(params, pageable);

        List<Events> events = page.getContent();
        if (events.isEmpty()) {
            return List.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();
        Map<String, Long> viewsByUri = getViewsForUris(uris);

        return events.stream()
                .map(e -> {
                    EventShortDto dto = eventsMapper.toShortDto(e);
                    String uri = "/events/" + e.getId();
                    long views = viewsByUri.getOrDefault(uri, 0L);
                    UserShortDto initiator = getUserShort(e.getInitiatorId());
                    return new EventShortDto(
                            dto.id(),
                            dto.title(),
                            dto.annotation(),
                            dto.category(),
                            initiator,
                            dto.paid(),
                            dto.eventDate(),
                            views,
                            dto.confirmedRequests()
                    );
                })
                .toList();
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        log.info("Публичный запрос события по id={}", eventId);
        saveHit(request);
        Events events = eventsRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        String uri = request.getRequestURI();
        long views = getViewsForUris(List.of(uri)).getOrDefault(uri, 0L);
        EventFullDto dto = eventsMapper.toFullDto(events);
        dto.setInitiator(getUserShort(events.getInitiatorId()));
        dto.setConfirmedRequests(getConfirmedCount(eventId));
        dto.setViews(views);
        return dto;
    }

    private UserShortDto getUserShort(Long userId) {
        try {
            List<UserDto> users = userClient.find(List.of(userId), 0, 1);
            if (!users.isEmpty()) {
                return new UserShortDto(users.get(0).getId(), users.get(0).getName());
            }
        } catch (Exception e) {
            log.warn("Не удалось получить пользователя id={}: {}", userId, e.getMessage());
        }
        return new UserShortDto(userId, "Unknown");
    }

    private long getConfirmedCount(Long eventId) {
        try {
            return requestClient.findAllRequestsByEventIdAndStatus(eventId,
                    ParticipationRequestStatus.CONFIRMED.name()).size();
        } catch (Exception e) {
            log.warn("Не удалось получить количество подтверждённых заявок для eventId={}", eventId);
            return 0L;
        }
    }

    private Map<String, Long> getViewsForUris(List<String> uris) {
        String start = "2000-01-01 00:00:00";
        String end = LocalDateTime.now().format(FORMATTER);
        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);
        Map<String, Long> result = new HashMap<>();
        for (ViewStatsDto stat : stats) {
            result.put(stat.getUri(), stat.getHits());
        }
        return result;
    }

    private void saveHit(HttpServletRequest request) {
        EndpointHitDto hit = new EndpointHitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(FORMATTER)
        );
        try {
            statsClient.saveHit(hit);
        } catch (Exception e) {
            log.error("Не удалось отправить хит в stats-сервис: {}", e.getMessage(), e);
        }
    }

    private void validateSearchParams(PublicEventSearchParams params) {
        LocalDateTime start = params.getRangeStart();
        LocalDateTime end = params.getRangeEnd();
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("rangeStart must not be after rangeEnd");
        }
    }
}