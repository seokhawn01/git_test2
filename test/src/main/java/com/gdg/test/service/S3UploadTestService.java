// src/main/java/com/gdg/test/service/S3UploadTestService.java
package com.gdg.test.service;

import com.gdg.test.config.S3Props;
import com.gdg.test.dto.S3UploadTestResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class S3UploadTestService {

    private final S3Client s3Client;
    private final S3Props s3Props;

    public S3UploadTestService(S3Client s3Client, S3Props s3Props) {
        this.s3Client = s3Client;
        this.s3Props = s3Props;
    }

    public S3UploadTestResponse uploadThenDelete(MultipartFile file) {
        String bucket = (s3Props.bucketname() == null) ? "" : s3Props.bucketname().trim();

        if (bucket.isBlank()) {
            throw new IllegalStateException("S3 bucket 설정이 비어있습니다. (s3.bucketname 또는 S3_BUCKETNAME)");
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
                    .metadata(Map.of("purpose", "swagger-upload-test", "cleanup", "immediate"))
                    .build();

            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
            uploaded = true;

            upload = new S3UploadTestResponse.Upload("SUCCESS", "S3 putObject 성공");

        } catch (Exception e) {
            upload = new S3UploadTestResponse.Upload("FAILED", "S3 putObject 실패: " + safeMsg(e));
        }

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
