// src/main/java/com/gdg/test/config/S3Props.java
package com.gdg.test.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
public record S3Props(
        String bucketname
) {
}
