package com.gdg.test.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteCreateRequest(
        @NotBlank String title,
        @NotBlank String content
) {}
