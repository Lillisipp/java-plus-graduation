package ru.practicum.controller.events;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.params.PublicEventSearchParams;
import ru.practicum.service.events.EventPublicServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventsPublicController {
    private final EventPublicServiceImpl eventPublicService;

    @GetMapping
    public List<EventShortDto> getEvents(@ModelAttribute @Valid PublicEventSearchParams params,
                                         HttpServletRequest request) {
        log.info("GET /events params={}", params);
        return eventPublicService.getEvents(params, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable Long id,
                                HttpServletRequest request) {
        log.info("GET /events/{}", id);
        return eventPublicService.getById(id, request);
    }
}