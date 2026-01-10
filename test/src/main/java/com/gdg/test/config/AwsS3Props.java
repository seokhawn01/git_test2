// src/main/java/com/gdg/test/config/AwsS3Props.java
package com.gdg.test.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record AwsS3Props(
        String baseUrl
) {
}
