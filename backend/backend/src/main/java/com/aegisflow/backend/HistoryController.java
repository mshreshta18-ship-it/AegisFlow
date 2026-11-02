
package com.aegisflow.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class HistoryController {

    private final ServiceHealthLogRepository repository;

    public HistoryController(ServiceHealthLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/history")
    public List<ServiceHealthLog> getHistory() {
        return repository.findAll();
    }
}
