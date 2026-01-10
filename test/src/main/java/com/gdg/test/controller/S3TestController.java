package com.gdg.test.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;

@Tag(name = "S3", description = "S3 연결 테스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3TestController {

    private final S3Client s3Client;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        // S3Client 빈 주입만 되면 일단 OK
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

}
