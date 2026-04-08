package ru.practicum.controller.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.compilation.CompilationDto;
import ru.practicum.model.compilation.params.PublicCompilationSearchParams;
import ru.practicum.service.compilation.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    ResponseEntity<CompilationDto> findById(@PathVariable Long compId) {
        return ResponseEntity.ok(compilationService.findById(compId));
    }

    @GetMapping
    ResponseEntity<List<CompilationDto>> findCompilations(@ModelAttribute @Valid PublicCompilationSearchParams params) {
        return ResponseEntity.ok(compilationService.findCompilations(params));
    }
}