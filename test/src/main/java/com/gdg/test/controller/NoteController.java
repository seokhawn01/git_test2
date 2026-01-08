package com.gdg.test.controller;

import com.gdg.test.dto.NoteCreateRequest;
import com.gdg.test.dto.NoteResponse;
import com.gdg.test.dto.NoteUpdateRequest;
import com.gdg.test.service.NoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Note", description = "DB 저장/조회 테스트용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public NoteResponse create(@RequestBody @Valid NoteCreateRequest req) {
        return noteService.create(req);
    }

    @GetMapping
    public List<NoteResponse> findAll() {
        return noteService.findAll();
    }

    // ✅ 추가
    @PatchMapping("/{id}")
    public NoteResponse update(
            @PathVariable Long id,
            @RequestBody @Valid NoteUpdateRequest req
    ) {
        return noteService.update(id, req);
    }
}
