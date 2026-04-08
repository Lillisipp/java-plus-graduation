package ru.practicum.service.events;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.model.params.AdminEventSearchParams;

import java.util.List;

public interface EventsAdminService {

    List<EventFullDto> getEvents(AdminEventSearchParams params);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest);
}