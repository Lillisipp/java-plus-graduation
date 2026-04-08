package ru.practicum.service.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.request.RequestClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.event.EventState;
import ru.practicum.enums.event.StateActionAdminUpdateEvent;
import ru.practicum.enums.request.ParticipationRequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.events.EventsMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Events;
import ru.practicum.model.params.AdminEventSearchParams;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.events.EventsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsAdminServiceImpl implements EventsAdminService {

    private final EventsMapper mapper;
    private final EventsRepository eventsRepository;
    private final CategoryRepository categoryRepository;
    private final RequestClient requestClient;
    private final UserClient userClient;

    @Override
    public List<EventFullDto> getEvents(AdminEventSearchParams params) {
        var pageable = params.toPageable();
        List<Events> found = eventsRepository.findAdminEvents(params, pageable);

        return found.stream()
                .map(e -> {
                    EventFullDto dto = mapper.toFullDto(e);
                    dto.setInitiator(getUserShort(e.getInitiatorId()));
                    try {
                        long confirmed = requestClient.findAllRequestsByEventIdAndStatus(
                                e.getId(), ParticipationRequestStatus.CONFIRMED.name()).size();
                        dto.setConfirmedRequests(confirmed);
                    } catch (Exception ex) {
                        dto.setConfirmedRequests(0L);
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        mapper.updateEventFromAdminRequest(updateRequest, event);

        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(
                        "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
                );
            }
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Category with id=" + updateRequest.getCategory() + " not found"));
            event.setCategory(category);
        }

        if (updateRequest.getLocation() != null && event.getLocation() != null) {
            event.getLocation().setLat(updateRequest.getLocation().lat());
            event.getLocation().setLon(updateRequest.getLocation().lon());
        }

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == StateActionAdminUpdateEvent.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Можно опубликовать только событие в статусе PENDING");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == StateActionAdminUpdateEvent.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Events saved = eventsRepository.save(event);
        EventFullDto dto = mapper.toFullDto(saved);
        dto.setInitiator(getUserShort(saved.getInitiatorId()));
        return dto;
    }

    private UserShortDto getUserShort(Long userId) {
        try {
            List<UserDto> users = userClient.find(List.of(userId), 0, 1);
            if (!users.isEmpty()) {
                return new UserShortDto(users.get(0).getId(), users.get(0).getName());
            }
        } catch (Exception e) {
            log.warn("Не удалось получить пользователя id={}", userId);
        }
        return new UserShortDto(userId, "Unknown");
    }
}