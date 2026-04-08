package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.compilation.CompilationMapper;
import ru.practicum.mapper.events.EventsMapper;
import ru.practicum.model.Events;
import ru.practicum.model.compilation.*;
import ru.practicum.model.compilation.params.PublicCompilationSearchParams;
import ru.practicum.repository.compilation.CompilationEventRepository;
import ru.practicum.repository.compilation.CompilationRepository;
import ru.practicum.repository.events.EventsRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationEventRepository compilationEventRepository;
    private final EventsRepository eventsRepository;
    private final EventsMapper eventsMapper;
    private final UserClient userClient;

    @Override
    @Transactional
    public CompilationDto save(NewCompilationDto newDto) {
        List<Events> events = eventsRepository.findByIdIn(List.copyOf(newDto.getEvents()));
        if (newDto.getEvents().size() != events.size()) {
            throw new NotFoundException("Найдены не все события, добавляемые в подборку");
        }

        try {
            Compilation savedCompilation = compilationRepository.save(CompilationMapper.toModel(newDto));
            List<CompilationEvent> list = events.stream()
                    .map(e -> new CompilationEvent(savedCompilation, e)).toList();
            compilationEventRepository.saveAll(list);

            return CompilationMapper.toDto(savedCompilation,
                    events.stream().map(this::toShortDtoWithInitiator).toList());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public CompilationDto patch(UpdateCompilationRequest updateDto, Long compId) {
        if (updateDto.getTitle() != null && (updateDto.getTitle().length() > 50 || updateDto.getTitle().isEmpty())) {
            throw new ValidationException("заголовок подборки должен быть в диапазоне от 1 до 50 символов");
        }
        Compilation updateCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("не найдена подборка с id " + compId));

        if (updateDto.getPinned() != null) {
            updateCompilation.setPinned(updateDto.getPinned());
        }
        if (updateDto.getTitle() != null) {
            updateCompilation.setTitle(updateDto.getTitle());
        }

        List<Events> events;
        if (updateDto.getEvents() != null) {
            events = eventsRepository.findByIdIn(List.copyOf(updateDto.getEvents()));
            if (updateDto.getEvents().size() != events.size()) {
                throw new NotFoundException("Найдены не все события, добавляемые в подборку");
            }
        } else {
            events = eventsRepository.findByIdIn(compilationEventRepository.findEventIdsByCompilationId(compId));
        }

        try {
            Compilation savedCompilation = compilationRepository.save(updateCompilation);
            List<CompilationEvent> list = events.stream()
                    .map(e -> new CompilationEvent(savedCompilation, e)).toList();
            compilationEventRepository.saveAll(list);
            return CompilationMapper.toDto(savedCompilation,
                    events.stream().map(this::toShortDtoWithInitiator).toList());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation result = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("не найдена подборка с id " + compId));
        compilationRepository.delete(result);
    }

    @Override
    public CompilationDto findById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("не найдена подборка с id " + compId));
        List<Events> events = eventsRepository.findByIdIn(
                compilationEventRepository.findEventIdsByCompilationId(compId));
        return CompilationMapper.toDto(compilation,
                events.stream().map(this::toShortDtoWithInitiator).toList());
    }

    @Override
    public List<CompilationDto> findCompilations(PublicCompilationSearchParams params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Page<Compilation> compilationPage = compilationRepository.findByPinned(params.getPinned(), pageable);

        List<Compilation> compilations = compilationPage.getContent();
        if (compilations.isEmpty()) {
            return List.of();
        }

        List<Long> cIds = compilations.stream().map(Compilation::getId).toList();
        List<CompilationEvent> compilationEvents = compilationEventRepository.findByCompilationIds(cIds);

        Map<Long, List<Events>> eventsByCompilationId = compilationEvents.stream()
                .collect(Collectors.groupingBy(
                        ce -> ce.getCompilation().getId(),
                        Collectors.mapping(CompilationEvent::getEvent, Collectors.toList())
                ));

        return compilations.stream()
                .map(c -> {
                    List<Events> events = eventsByCompilationId.getOrDefault(c.getId(), List.of());
                    List<EventShortDto> eventShortDtos = events.stream()
                            .map(this::toShortDtoWithInitiator)
                            .toList();
                    return CompilationMapper.toDto(c, eventShortDtos);
                })
                .toList();
    }

    private EventShortDto toShortDtoWithInitiator(Events event) {
        EventShortDto dto = eventsMapper.toShortDto(event);
        UserShortDto initiator = getUserShort(event.getInitiatorId());
        return new EventShortDto(dto.id(), dto.title(), dto.annotation(),
                dto.category(), initiator, dto.paid(), dto.eventDate(),
                dto.views(), dto.confirmedRequests());
    }

    private UserShortDto getUserShort(Long userId) {
        try {
            List<UserDto> users = userClient.find(List.of(userId), 0, 1);
            if (!users.isEmpty()) {
                return new UserShortDto(users.get(0).getId(), users.get(0).getName());
            }
        } catch (Exception ignored) {
        }
        return new UserShortDto(userId, "Unknown");
    }
}