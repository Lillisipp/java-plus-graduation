package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.request.ParticipationRequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.request.ParticipationRequestMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;

    @Override
    @Transactional
    public ParticipationRequestDto add(Long userId, Long eventId) {
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(userId)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Запрос на участие создан: requestId={}, requesterId={}, eventId={}",
                saved.getId(), userId, eventId);
        return ParticipationRequestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос с id " + requestId));

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Запрос не принадлежит пользователю с id " + userId);
        }

        request.setStatus(ParticipationRequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> findByRequesterId(Long requesterId) {
        return requestRepository.findByRequesterId(requesterId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> findByEventId(Long eventId) {
        return requestRepository.findByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> findByEventIdAndStatus(Long eventId, String status) {
        ParticipationRequestStatus requestStatus = ParticipationRequestStatus.valueOf(status);
        return requestRepository.findAllByEventIdAndStatus(eventId, requestStatus).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> findByRequestsId(Set<Long> requestsId) {
        return requestRepository.findAllById(requestsId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> updateRequestsStatus(Set<Long> requestsId, String status) {
        ParticipationRequestStatus newStatus = ParticipationRequestStatus.valueOf(status);
        List<ParticipationRequest> requests = requestRepository.findAllById(requestsId);

        if (requests.size() != requestsId.size()) {
            throw new NotFoundException("Не все запросы найдены");
        }

        for (ParticipationRequest request : requests) {
            request.setStatus(newStatus);
        }

        return requestRepository.saveAll(requests).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, String status) {
        ParticipationRequestStatus requestStatus = ParticipationRequestStatus.valueOf(status);
        return requestRepository.existsByEventIdAndRequesterIdAndStatus(eventId, userId, requestStatus);
    }
}