package ru.practicum.controller.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.compilation.CompilationDto;
import ru.practicum.model.compilation.NewCompilationDto;
import ru.practicum.model.compilation.UpdateCompilationRequest;
import ru.practicum.service.compilation.CompilationService;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> save(@RequestBody @Valid NewCompilationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compilationService.save(dto));
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> patch(@RequestBody UpdateCompilationRequest dto,
                                                @PathVariable Long compId) {
        return ResponseEntity.ok(compilationService.patch(dto, compId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void delete(@PathVariable Long compId) {
        compilationService.delete(compId);
    }
}