package com.gdg.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "S3", description = "S3 연결 테스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3TestController {

    private final S3Client s3Client;

    /**
     * ✅ 버킷 이름을 환경변수/설정에서 가져오도록 해둠
     * - 우선순위: application.yml의 cloud.aws.s3.bucket -> 환경변수 S3_BUCKET
     *
     * 네가 이미 /api/s3/buckets 성공 중이니까,
     * 실제 업로드 테스트에는 “어느 버킷에 올릴지” 이름이 필요해서 추가한 거야.
     */
    @Value("${cloud.aws.s3.bucket:${S3_BUCKET:}}")
    private String bucket;

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

    @Operation(
            summary = "S3 업로드 테스트(자동 삭제)",
            description = "파일을 S3에 업로드(putObject)한 뒤 즉시 삭제(deleteObject)합니다. Swagger에서 파일 선택 후 Execute로 테스트하세요."
    )
    @PostMapping(value = "/upload-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTest(
            @RequestPart("file")
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "object")
                    )
            )
            MultipartFile file
    ) {
        if (bucket == null || bucket.isBlank()) {
            // 어떤 버킷에 업로드할지 정보가 없으면 테스트 자체가 불가
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "S3 bucket 설정이 비어있습니다.",
                    "hint", "application-prod.yml에 cloud.aws.s3.bucket 또는 컨테이너 환경변수 S3_BUCKET을 설정하세요."
            ));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "업로드할 파일이 비어있습니다.",
                    "hint", "Swagger에서 file을 선택하고 Execute 하세요."
            ));
        }

        String original = (file.getOriginalFilename() == null) ? "file" : file.getOriginalFilename();
        String ext = extractExt(original);
        String key = "healthcheck/test-" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bucket", bucket);
        result.put("key", key);
        result.put("size", file.getSize());
        result.put("contentType", file.getContentType());
        result.put("timestamp", Instant.now().toString());

        boolean uploaded = false;

        // 1) 업로드 시도
        try {
            String contentType = (file.getContentType() == null) ? "application/octet-stream" : file.getContentType();

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .metadata(Map.of("purpose", "swagger-upload-test", "cleanup", "immediate"))
                    .build();

            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
            uploaded = true;

            result.put("upload", Map.of(
                    "status", "SUCCESS",
                    "message", "S3 putObject 성공"
            ));
        } catch (Exception e) {
            result.put("upload", Map.of(
                    "status", "FAILED",
                    "message", "S3 putObject 실패: " + safeMsg(e)
            ));
        }

        // 2) 업로드 성공 시 즉시 삭제 시도
        if (uploaded) {
            try {
                DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();

                s3Client.deleteObject(delReq);

                result.put("cleanup", Map.of(
                        "status", "SUCCESS",
                        "message", "업로드 테스트 파일 삭제 성공"
                ));
            } catch (Exception e) {
                result.put("cleanup", Map.of(
                        "status", "FAILED",
                        "message", "삭제 실패: " + safeMsg(e)
                ));
            }
        } else {
            result.put("cleanup", Map.of(
                    "status", "SKIPPED",
                    "message", "업로드 실패로 삭제 생략"
            ));
        }

        // 업로드가 실패했으면 500으로 줘도 되지만, Swagger에서 “왜 실패했는지 body로 보는 맛” 때문에 200 유지 가능
        // 여기서는 upload FAILED면 500을 주는 방식으로 해둠.
        if (!uploaded) {
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    private static String extractExt(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        return filename.substring(idx + 1).trim();
    }

    private static String safeMsg(Exception e) {
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? e.getClass().getSimpleName() : m;
    }
}
