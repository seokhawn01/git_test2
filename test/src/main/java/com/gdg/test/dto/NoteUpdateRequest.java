package com.gdg.test.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteUpdateRequest(
        @NotBlank(message = "content는 비어 있을 수 없습니다.")
        String content
) {}
