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

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId,
                                                  @RequestParam(defaultValue = "10") int maxResults) {
        log.info("GET /events/recommendations userId={}, maxResults={}", userId, maxResults);
        return eventPublicService.getRecommendations(userId, maxResults);
    }

    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable Long id,
                                @RequestHeader(name = "X-EWM-USER-ID", required = false) Long userId,
                                HttpServletRequest request) {
        log.info("GET /events/{} userId={}", id, userId);
        return eventPublicService.getById(id, userId, request);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@PathVariable Long eventId,
                          @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("PUT /events/{}/like userId={}", eventId, userId);
        eventPublicService.likeEvent(userId, eventId);
    }
}