package ru.practicum.service.events;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.params.PublicEventSearchParams;

import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getEvents(PublicEventSearchParams params,
                                  HttpServletRequest request);

    EventFullDto getById(Long eventId,
                         HttpServletRequest request);
}