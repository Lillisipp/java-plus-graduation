package ru.practicum.service.request;

import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Set;

public interface ParticipationRequestService {

    ParticipationRequestDto add(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> findByRequesterId(Long requesterId);

    List<ParticipationRequestDto> findByEventId(Long eventId);

    List<ParticipationRequestDto> findByEventIdAndStatus(Long eventId, String status);

    List<ParticipationRequestDto> findByRequestsId(Set<Long> requestsId);

    List<ParticipationRequestDto> updateRequestsStatus(Set<Long> requestsId, String status);

    boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, String status);
}