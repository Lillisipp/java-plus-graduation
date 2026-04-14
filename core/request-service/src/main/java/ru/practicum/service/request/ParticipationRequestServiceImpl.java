package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.event.EventClient;
import ru.practicum.dto.event.EventFullDto;
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
    private final EventClient eventClient;

    @Override
    @Transactional
    public ParticipationRequestDto add(Long userId, Long eventId) {
        // проверка на повторный запрос
        requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .ifPresent(r -> {
                    log.warn("Попытка добавить повторный запрос: requesterId={}, eventId={}", userId, eventId);
                    throw new ConflictException("нельзя добавить повторный запрос");
                });

        EventFullDto event = eventClient.findEventById(eventId);


        if (event.getInitiator() != null && userId.equals(event.getInitiator().id())) {
            log.warn("Инициатор пытается подать запрос на своё событие: userId={}, eventId={}", userId, eventId);
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии");
        }

        if (event.getPublishedOn() == null) {
            log.warn("Попытка подать заявку на неопубликованное событие: eventId={}, requesterId={}", eventId, userId);
            throw new ConflictException("нельзя участвовать в неопубликованном событии");
        }

        long limit = event.getParticipantLimit() == null ? 0L : event.getParticipantLimit();
        if (limit != 0L) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(
                    eventId, ParticipationRequestStatus.CONFIRMED);
            if (confirmedCount >= limit) {
                log.warn("Достигнут лимит участников: eventId={}, limit={}, confirmed={}", eventId, limit, confirmedCount);
                throw new ConflictException("достигнут лимит запросов на участие");
            }
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(userId)
                .status(ParticipationRequestStatus.PENDING)
                .build();

        // авто-подтверждение если модерация отключена или нет лимита
        if (Boolean.FALSE.equals(event.getRequestModeration())
                || (event.getParticipantLimit() != null && event.getParticipantLimit() == 0)) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
            log.debug("Премодерация отключена либо нет лимита, заявка CONFIRMED: eventId={}, requesterId={}", eventId, userId);
        }

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