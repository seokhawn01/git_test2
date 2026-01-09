package com.gdg.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3UploadService s3UploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<S3UploadService.UploadResult> upload(
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        return ResponseEntity.ok(s3UploadService.upload(file));
    }
}
