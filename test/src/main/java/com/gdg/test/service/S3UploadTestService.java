package com.gdg.test.service;

import com.gdg.test.dto.S3UploadTestResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.UUID;


@Service
public class S3UploadTestService {

    private final S3Client s3Client;

    /**
     * ✅ 여기 프로퍼티 키는 네 프로젝트 설정에 맞춰 바꿔도 된다.
     * 예)
     * - cloud.aws.s3.bucket
     * - aws.s3.bucket
     * - S3_BUCKET 환경변수 → ${S3_BUCKET}
     */
    @Value("${cloud.aws.s3.bucket:${S3_BUCKET:}}")
    private String bucket;

    public S3UploadTestService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public S3UploadTestResponse uploadThenDelete(MultipartFile file) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("S3 bucket 설정이 비어있습니다. (cloud.aws.s3.bucket 또는 S3_BUCKET)");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        String original = (file.getOriginalFilename() == null) ? "file" : file.getOriginalFilename();
        String ext = extractExt(original);
        String key = "healthcheck/test-" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);

        S3UploadTestResponse.Upload upload;
        S3UploadTestResponse.Cleanup cleanup;

        boolean uploaded = false;

        try {
            String contentType = (file.getContentType() == null) ? "application/octet-stream" : file.getContentType();

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    // 테스트 파일이라는 메타데이터(선택)
                    .metadata(java.util.Map.of("purpose", "swagger-upload-test", "cleanup", "immediate"))
                    .build();

            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
            uploaded = true;

            upload = new S3UploadTestResponse.Upload("SUCCESS", "S3 putObject 성공");

        } catch (Exception e) {
            upload = new S3UploadTestResponse.Upload("FAILED", "S3 putObject 실패: " + safeMsg(e));
        }

        // ✅ 업로드가 성공했으면 반드시 삭제 시도 (자동 정리)
        if (uploaded) {
            try {
                DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();

                s3Client.deleteObject(delReq);
                cleanup = new S3UploadTestResponse.Cleanup("SUCCESS", "업로드 테스트 파일 삭제 성공");
            } catch (Exception e) {
                cleanup = new S3UploadTestResponse.Cleanup("FAILED", "삭제 실패: " + safeMsg(e));
            }
        } else {
            cleanup = new S3UploadTestResponse.Cleanup("SKIPPED", "업로드가 실패하여 삭제 생략");
        }

        String contentType = (file.getContentType() == null) ? "application/octet-stream" : file.getContentType();

        return new S3UploadTestResponse(
                bucket,
                key,
                file.getSize(),
                contentType,
                upload,
                cleanup,
                Instant.now()
        );
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
