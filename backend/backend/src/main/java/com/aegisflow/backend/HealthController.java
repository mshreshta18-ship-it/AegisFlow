package com.aegisflow.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> checkHealth() {
        return Map.of(
            "application", "AegisFlow",
            "status", "UP",
            "message", "Monitoring system is running"
        );
    }
}