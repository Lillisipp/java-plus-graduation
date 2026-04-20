package ru.practicum.service.events;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.request.RequestClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.event.EventState;
import ru.practicum.enums.event.StateActionUserUpdateEvent;
import ru.practicum.enums.request.ParticipationRequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.events.EventsMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Events;
import ru.practicum.model.Location;
import ru.practicum.repository.events.EventsRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.location.LocationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventAuthorizedServiceImpl implements EventAuthorizedService {

    private final EventsMapper mapper;
    private final EventsRepository eventsRepository;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final LocationService locationService;
    private final CategoryService categoryService;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Получение событий пользователя userId={}, from={}, size={}", userId, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Events> page = eventsRepository.findAllByInitiatorId(userId, pageable);
        UserShortDto initiator = getUserShort(userId);
        return page.getContent().stream()
                .map(e -> {
                    EventShortDto dto = mapper.toShortDto(e);
                    return new EventShortDto(dto.id(), dto.title(), dto.annotation(),
                            dto.category(), initiator, dto.paid(), dto.eventDate(),
                            dto.rating(), dto.confirmedRequests());
                })
                .toList();
    }

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события: userId={}, payload={}", userId, newEventDto);
        checkUserExists(userId);

        LocalDateTime newDate = newEventDto.getEventDate();
        if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
            );
        }

        Category category = categoryService.findCategoryEntityById(newEventDto.getCategory());
        Location location = locationService.saveLocation(newEventDto.getLocation());

        Events events = mapper.toEntity(newEventDto);
        events.setInitiatorId(userId);
        events.setCategory(category);
        events.setLocation(location);
        events.setState(EventState.PENDING);
        events.setCreatedOn(LocalDateTime.now());

        Events saved = eventsRepository.save(events);
        EventFullDto dto = mapper.toFullDto(saved);
        dto.setInitiator(getUserShort(userId));
        return dto;
    }

    @Transactional
    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Обновление события eventId={} пользователем userId={}", eventId, userId);
        checkUserExists(userId);
        Events event = checkEvent(eventId);
        checkInitiator(userId, eventId, event);

        if (updateRequest.getStateAction() != StateActionUserUpdateEvent.SEND_TO_REVIEW &&
                event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Не удается обновить опубликованное событие");
        }

        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(
                        "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
                );
            }
        }

        mapper.updateEventFromUserRequest(updateRequest, event);

        if (updateRequest.getCategory() != null) {
            Category newCategory = categoryService.findCategoryEntityById(updateRequest.getCategory());
            event.setCategory(newCategory);
        }

        if (updateRequest.getLocation() != null) {
            Location newLocation = locationService.saveLocation(updateRequest.getLocation());
            event.setLocation(newLocation);
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }

        Events saved = eventsRepository.save(event);
        EventFullDto dto = mapper.toFullDto(saved);
        dto.setInitiator(getUserShort(userId));
        return dto;
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Events event = checkEvent(eventId);
        checkInitiator(userId, eventId, event);
        EventFullDto dto = mapper.toFullDto(event);
        dto.setInitiator(getUserShort(userId));
        return dto;
    }

    @Override
    public List<ParticipationRequestDto> findEventRequests(Long userId, Long eventId) {
        checkUserExists(userId);
        Events event = checkEvent(eventId);
        checkInitiator(userId, eventId, event);
        return requestClient.findAllRequestsByEventId(eventId);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult rejectingRequest(Long userId, Long eventId,
                                                            EventRequestStatusUpdateRequest updateRequest) {
        checkUserExists(userId);
        Events event = checkEvent(eventId);
        checkInitiator(userId, eventId, event);

        List<Long> requestIds = updateRequest.getRequestIds();
        if (requestIds == null || requestIds.isEmpty()) {
            throw new ConflictException("RequestIds must not be empty");
        }

        Set<Long> requestIdSet = Set.copyOf(requestIds);
        List<ParticipationRequestDto> requests = requestClient.findAllRequestsByRequestsId(requestIdSet);

        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Some participation requests were not found");
        }

        for (ParticipationRequestDto r : requests) {
            if (!r.getEvent().equals(eventId)) {
                throw new ConflictException("Request does not belong to this event");
            }
            if (!ParticipationRequestStatus.PENDING.name().equals(r.getStatus())) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        ParticipationRequestStatus action = updateRequest.getStatus();
        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        if (action == ParticipationRequestStatus.REJECTED) {
            List<ParticipationRequestDto> updated = requestClient.updateRequest(
                    requestIdSet, ParticipationRequestStatus.REJECTED.name());
            rejectedDtos.addAll(updated);
        } else if (action == ParticipationRequestStatus.CONFIRMED) {
            long limit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0L;
            long confirmedCount = requestClient.findAllRequestsByEventIdAndStatus(
                    eventId, ParticipationRequestStatus.CONFIRMED.name()).size();

            if (limit != 0 && confirmedCount >= limit) {
                throw new ConflictException("The participant limit has been reached");
            }

            Set<Long> toConfirm = new java.util.LinkedHashSet<>();
            Set<Long> toReject = new java.util.LinkedHashSet<>();
            for (Long id : requestIds) {
                if (limit == 0 || confirmedCount < limit) {
                    toConfirm.add(id);
                    confirmedCount++;
                } else {
                    toReject.add(id);
                }
            }

            if (!toConfirm.isEmpty()) {
                confirmedDtos.addAll(requestClient.updateRequest(toConfirm, ParticipationRequestStatus.CONFIRMED.name()));
            }
            if (!toReject.isEmpty()) {
                rejectedDtos.addAll(requestClient.updateRequest(toReject, ParticipationRequestStatus.REJECTED.name()));
            }

            if (limit != 0 && confirmedCount >= limit) {
                List<ParticipationRequestDto> pending = requestClient
                        .findAllRequestsByEventIdAndStatus(eventId, ParticipationRequestStatus.PENDING.name());
                if (!pending.isEmpty()) {
                    Set<Long> pendingIds = pending.stream()
                            .map(ParticipationRequestDto::getId)
                            .collect(Collectors.toSet());
                    rejectedDtos.addAll(requestClient.updateRequest(pendingIds, ParticipationRequestStatus.REJECTED.name()));
                }
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedDtos);
        result.setRejectedRequests(rejectedDtos);
        return result;
    }

    private void checkUserExists(Long userId) {
        List<UserDto> users = userClient.find(List.of(userId), 0, 1);
        if (users.isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
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

    private Events checkEvent(Long eventId) {
        return eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));
    }

    private static void checkInitiator(Long userId, Long eventId, Events event) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("User " + userId + " is not initiator of event " + eventId);
        }
    }
}