// src/main/java/com/gdg/test/dto/S3UploadTestResponse.java
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
    public record Upload(String status, String message) {}
    public record Cleanup(String status, String message) {}
}
