package com.aegisflow.backend;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recovery")
public class RecoveryController {

    private final SelfHealingService selfHealingService;

    public RecoveryController(SelfHealingService selfHealingService) {
        this.selfHealingService = selfHealingService;
    }

    @PostMapping("/{serviceName}")
    public Map<String, String> recoverService(
            @PathVariable String serviceName) {

        String formattedName = serviceName
                .replace("-", " ")
                .toLowerCase();

        String service = switch (formattedName) {
            case "payment service", "payment" -> "Payment Service";
            case "order service", "order" -> "Order Service";
            case "inventory service", "inventory" -> "Inventory Service";
            default -> formattedName;
        };

        String result = selfHealingService.restartService(service);

        return Map.of(
                "service", service,
                "result", result
        );
    }
}
