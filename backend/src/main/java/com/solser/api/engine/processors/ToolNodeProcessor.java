package com.solser.api.engine.processors;

import com.solser.api.engine.NodeProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ToolNodeProcessor implements NodeProcessor {

    @Override
    public CompletableFuture<Map<String, Object>> process(Map<String, Object> node, Map<String, Object> input) {
        return CompletableFuture.supplyAsync(() -> {
            // Logic to execute database queries or other tools would go here
            // matching the 'runMongo', 'runPostgres' logic from Node.js
            return Map.of("tool_result", "Tool execution simulated", "input_received", input);
        });
    }

    @Override
    public String getSupportedType() {
        return "TOOL";
    }
}
