package com.aegisflow.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class ServiceController {

    @GetMapping("/api/services")
    public List<Map<String, String>> getServices() {
        return List.of(
            Map.of("name", "Payment Service", "status", "UP"),
            Map.of("name", "Order Service", "status", "UP"),
            Map.of("name", "Inventory Service", "status", "DOWN")
        );
    }
}
