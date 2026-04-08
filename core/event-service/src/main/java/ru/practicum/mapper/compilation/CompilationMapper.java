package ru.practicum.mapper.compilation;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.CompilationDto;
import ru.practicum.model.compilation.NewCompilationDto;

import java.util.List;

@UtilityClass
public class CompilationMapper {
    public static Compilation toModel(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    public static CompilationDto toDto(Compilation model, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(model.getId())
                .pinned(model.isPinned())
                .title(model.getTitle())
                .events(events)
                .build();
    }
}