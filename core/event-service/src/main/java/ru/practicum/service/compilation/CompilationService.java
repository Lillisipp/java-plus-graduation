package ru.practicum.service.compilation;

import ru.practicum.model.compilation.CompilationDto;
import ru.practicum.model.compilation.NewCompilationDto;
import ru.practicum.model.compilation.UpdateCompilationRequest;
import ru.practicum.model.compilation.params.PublicCompilationSearchParams;

import java.util.List;

public interface CompilationService {
    CompilationDto save(NewCompilationDto newCompilationDto);

    CompilationDto patch(UpdateCompilationRequest updateCompilationRequestDto, Long compId);

    void delete(Long compId);

    CompilationDto findById(Long compId);

    List<CompilationDto> findCompilations(PublicCompilationSearchParams params);
}