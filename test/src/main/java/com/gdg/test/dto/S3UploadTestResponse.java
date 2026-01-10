package com.gdg.test.dto;

import java.time.Instant;

public record S3UploadTestResponse(
        String bucket,
        String key,
        long size,
        String contentType,
        Upload upload,
        Cleanup cleanup,
        Instant timestamp
) {
    public record Upload(
            String status,      // "SUCCESS" | "FAILED"
            String message
    ) {}

    public record Cleanup(
            String status,      // "SUCCESS" | "FAILED" | "SKIPPED"
            String message
    ) {}
}
