package com.gdg.test.healthCheck.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final JavaMailSender mailSender;

    public HealthCheckController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Operation(summary = "서버 상태 확인")
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "test Backend Server is running!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mail-env")
    public ResponseEntity<Map<String, Object>> mailEnv() {
        Map<String, Object> res = new HashMap<>();
        res.put("mailUsernameSet", mailUsername != null && !mailUsername.isBlank());
        res.put("mailUsername", mailUsername);
        return ResponseEntity.ok(res);
    }

    // ✅ 테스트 메일 발송
    @Operation(summary = "테스트 메일 발송")
    @PostMapping("/mail-send")
    public ResponseEntity<Map<String, Object>> sendTestMail(@RequestBody TestMailRequest req) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(req.to());
        message.setSubject(req.subject());
        message.setText(req.text());
        message.setFrom(mailUsername);

        mailSender.send(message);

        Map<String, Object> res = new HashMap<>();
        res.put("sent", true);
        res.put("to", req.to());
        res.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(res);
    }

    public record TestMailRequest(String to, String subject, String text) {}
}
