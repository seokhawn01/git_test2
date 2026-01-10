// src/main/java/com/gdg/test/config/PropsConfig.java
package com.gdg.test.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({S3Props.class, AwsS3Props.class})
public class PropsConfig {
}
