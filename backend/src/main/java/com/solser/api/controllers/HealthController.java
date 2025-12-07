package com.solser.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.time.Instant;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "ok",
            "uptime", System.currentTimeMillis(), // Simplified uptime
            "timestamp", Instant.now().toString(),
            "stack", "Java Spring Boot"
        );
    }
}
