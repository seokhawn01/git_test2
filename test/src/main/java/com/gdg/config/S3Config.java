package com.gdg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Configuration
public class S3Config {

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // ✅ accessKey/secretKey가 있으면: 로컬에서만 사용 (선택)
        if (accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank()) {

            log.info("S3Client: StaticCredentialsProvider 사용");
            AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);

            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .build();
        }

        // ✅ 없으면: EC2 IAM Role (Default Credential Chain)
        log.info("S3Client: Default Credential Chain 사용 (EC2 IAM Role)");
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
