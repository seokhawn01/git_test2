// src/main/java/com/gdg/test/controller/S3TestController.java
package com.gdg.test.controller;

import com.gdg.test.dto.S3UploadTestResponse;
import com.gdg.test.service.S3UploadTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

@Tag(name = "S3", description = "S3 연결 테스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3TestController {

    private final S3Client s3Client;
    private final S3UploadTestService s3UploadTestService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("S3Client bean OK");
    }

    @GetMapping("/buckets")
    public ResponseEntity<?> buckets() {
        return ResponseEntity.ok(
                s3Client.listBuckets().buckets()
                        .stream()
                        .map(b -> b.name())
                        .toList()
        );
    }

    @Operation(summary = "S3 업로드 테스트(자동 삭제)", description = "파일을 S3에 업로드한 뒤 즉시 삭제합니다.")
    @PostMapping(value = "/upload-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<S3UploadTestResponse> uploadTest(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(s3UploadTestService.uploadThenDelete(file));
    }
}
