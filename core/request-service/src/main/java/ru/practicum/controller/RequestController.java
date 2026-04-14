package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.request.ParticipationRequestService;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class RequestController {

    private final ParticipationRequestService requestService;

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> findAllRequestsByUserId(@PathVariable Long userId) {
        return requestService.findByRequesterId(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId,
                                              @RequestParam Long eventId) {
        return requestService.add(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/requests/events/{eventId}")
    public List<ParticipationRequestDto> findAllRequestsByEventId(@PathVariable Long eventId) {
        return requestService.findByEventId(eventId);
    }

    @GetMapping("/users/requests/events/{eventId}/status")
    public List<ParticipationRequestDto> findAllRequestsByEventIdAndStatus(
            @PathVariable Long eventId,
            @RequestParam String status) {
        return requestService.findByEventIdAndStatus(eventId, status);
    }

    @GetMapping("/users/requests")
    public List<ParticipationRequestDto> findAllRequestsByRequestsId(@RequestParam Set<Long> requestsId) {
        return requestService.findByRequestsId(requestsId);
    }

    @PutMapping("/users/requests/status")
    public List<ParticipationRequestDto> updateRequest(@RequestParam Set<Long> requestsId,
                                                       @RequestParam String status) {
        return requestService.updateRequestsStatus(requestsId, status);
    }

    @GetMapping("/users/requests/existence")
    public boolean findExistRequests(@RequestParam Long eventId,
                                     @RequestParam Long userId,
                                     @RequestParam String status) {
        return requestService.existsByEventIdAndUserIdAndStatus(eventId, userId, status);
    }
}