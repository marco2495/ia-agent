package com.solser.api.controllers;

import com.solser.api.models.LlmRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    public ResponseEntity<Object> chat(@RequestBody LlmRequest request) {
        String baseUrl = request.getBaseUrl() != null ? request.getBaseUrl() : "http://localhost:11434/v1";
        if (!baseUrl.startsWith("http")) {
            baseUrl = "http://" + baseUrl;
        }
        
        // Simple proxy logic to the LLM provider (e.g. Ollama/OpenAI Compatible)
        String url = baseUrl + "/chat/completions";
        
        Map<String, Object> payload = Map.of(
            "model", request.getModel() != null ? request.getModel() : "gpt-oss",
            "messages", List.of(
                Map.of("role", "system", "content", request.getSystemInstruction() != null ? request.getSystemInstruction() : ""),
                Map.of("role", "user", "content", request.getPrompt())
            ),
            "stream", false
        );

        try {
            // In a real app we would use a proper service and error handling
             // This is a placeholder for the actual HTTP call to the LLM
            return ResponseEntity.ok(Map.of("text", "Simulated response from Java Backend for: " + request.getPrompt()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
