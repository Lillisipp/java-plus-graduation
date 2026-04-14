package ru.practicum.service.events;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface EventAuthorizedService {
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    EventFullDto getUserEvent(Long userId, Long eventId);

    List<ParticipationRequestDto> findEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult rejectingRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);
}