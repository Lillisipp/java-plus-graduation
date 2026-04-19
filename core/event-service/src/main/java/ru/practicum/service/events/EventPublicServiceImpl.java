package ru.practicum.service.events;

import com.google.protobuf.Timestamp;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.AnalyzerClient;
import ru.practicum.client.request.RequestClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.collector.CollectorClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.event.EventState;
import ru.practicum.enums.request.ParticipationRequestStatus;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.events.EventsMapper;
import ru.practicum.model.Events;
import ru.practicum.model.params.PublicEventSearchParams;
import ru.practicum.repository.events.EventsRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublicServiceImpl implements EventPublicService {

    private final EventsRepository eventsRepository;
    private final EventsMapper eventsMapper;
    private final RequestClient requestClient;
    private final UserClient userClient;
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;

    // Локальная память просмотров: нужна только для валидации "лайк можно
    // ставить только на просмотренное". Анализатор такого метода не отдаёт,
    // а заводить отдельную таблицу ради одной проверки избыточно.
    private final Map<Long, Set<Long>> viewedByUser = new ConcurrentHashMap<>();

    @Override
    public List<EventShortDto> getEvents(PublicEventSearchParams params,
                                         HttpServletRequest request) {
        log.info("Поиск публичных событий params={}", params);
        validateSearchParams(params);

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Events> page = eventsRepository.findPublicEvents(params, pageable);

        List<Events> events = page.getContent();
        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream().map(Events::getId).toList();
        Map<Long, Double> ratingByEventId = getRatings(eventIds);

        List<Long> initiatorIds = events.stream()
                .map(Events::getInitiatorId)
                .distinct()
                .toList();
        Map<Long, UserShortDto> initiatorsByIds = getUsersShortByIds(initiatorIds);

        return events.stream()
                .map(e -> {
                    EventShortDto dto = eventsMapper.toShortDto(e);
                    UserShortDto initiator = initiatorsByIds.getOrDefault(e.getInitiatorId(),
                            new UserShortDto(e.getInitiatorId(), "Unknown"));
                    return new EventShortDto(
                            dto.id(),
                            dto.title(),
                            dto.annotation(),
                            dto.category(),
                            initiator,
                            dto.paid(),
                            dto.eventDate(),
                            ratingByEventId.getOrDefault(e.getId(), 0.0),
                            dto.confirmedRequests()
                    );
                })
                .toList();
    }

    @Override
    public EventFullDto getById(Long eventId, Long userId, HttpServletRequest request) {
        log.info("Публичный запрос события по id={}, userId={}", eventId, userId);
        Events events = eventsRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (userId != null) {
            sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
            viewedByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(eventId);
        }

        EventFullDto dto = eventsMapper.toFullDto(events);
        dto.setInitiator(getUserShort(events.getInitiatorId()));
        dto.setConfirmedRequests(getConfirmedCount(eventId));
        dto.setRating(getRatings(List.of(eventId)).getOrDefault(eventId, 0.0));
        return dto;
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, int maxResults) {
        log.info("Рекомендации пользователя userId={}, maxResults={}", userId, maxResults);
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        List<RecommendedEventProto> recommendations = analyzerClient.getRecommendationsForUser(request);
        return buildRecommendationDtos(recommendations);
    }

    @Override
    public void likeEvent(Long userId, Long eventId) {
        log.info("Лайк от userId={} для eventId={}", userId, eventId);
        eventsRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        Set<Long> viewed = viewedByUser.getOrDefault(userId, Set.of());
        if (!viewed.contains(eventId)) {
            throw new ValidationException("Нельзя лайкнуть мероприятие без предварительного просмотра");
        }
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private List<EventShortDto> buildRecommendationDtos(List<RecommendedEventProto> recommendations) {
        if (recommendations.isEmpty()) {
            return List.of();
        }
        Map<Long, Double> scoreByEventId = new HashMap<>();
        for (RecommendedEventProto r : recommendations) {
            scoreByEventId.put(r.getEventId(), r.getScore());
        }
        List<Long> ids = List.copyOf(scoreByEventId.keySet());
        List<Events> events = eventsRepository.findByIdIn(ids);

        List<Long> initiatorIds = events.stream()
                .map(Events::getInitiatorId)
                .distinct()
                .toList();
        Map<Long, UserShortDto> initiatorsByIds = getUsersShortByIds(initiatorIds);

        return events.stream()
                .map(e -> {
                    EventShortDto dto = eventsMapper.toShortDto(e);
                    UserShortDto initiator = initiatorsByIds.getOrDefault(e.getInitiatorId(),
                            new UserShortDto(e.getInitiatorId(), "Unknown"));
                    return new EventShortDto(
                            dto.id(),
                            dto.title(),
                            dto.annotation(),
                            dto.category(),
                            initiator,
                            dto.paid(),
                            dto.eventDate(),
                            scoreByEventId.getOrDefault(e.getId(), 0.0),
                            dto.confirmedRequests()
                    );
                })
                .sorted(Comparator.comparingDouble(EventShortDto::rating).reversed())
                .toList();
    }

    private Map<Long, Double> getRatings(List<Long> eventIds) {
        Map<Long, Double> result = new HashMap<>();
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();
            List<RecommendedEventProto> scores = analyzerClient.getInteractionsCount(request);
            for (RecommendedEventProto score : scores) {
                result.put(score.getEventId(), score.getScore());
            }
        } catch (Exception e) {
            log.warn("Не удалось получить рейтинги eventIds={}: {}", eventIds, e.getMessage());
        }
        return result;
    }

    private Map<Long, UserShortDto> getUsersShortByIds(List<Long> userIds) {
        try {
            List<UserDto> users = userClient.find(userIds, 0, userIds.size());
            Map<Long, UserShortDto> result = new HashMap<>();
            for (UserDto user : users) {
                result.put(user.getId(), new UserShortDto(user.getId(), user.getName()));
            }
            return result;
        } catch (Exception e) {
            log.warn("Не удалось получить пользователей ids={}: {}", userIds, e.getMessage());
            return Map.of();
        }
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

    private void sendUserAction(Long userId, Long eventId, ActionTypeProto type) {
        try {
            Instant now = Instant.now();
            UserActionProto action = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(type)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(now.getEpochSecond())
                            .setNanos(now.getNano())
                            .build())
                    .build();
            collectorClient.collectUserAction(action);
        } catch (Exception e) {
            log.warn("Не удалось отправить действие userId={}, eventId={}, type={}: {}",
                    userId, eventId, type, e.getMessage());
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