package com.solser.api.engine.processors;

import com.solser.api.engine.NodeProcessor;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class StartNodeProcessor implements NodeProcessor {
    @Override
    public CompletableFuture<Map<String, Object>> process(Map<String, Object> node, Map<String, Object> input) {
        return CompletableFuture.completedFuture(input);
    }
    @Override
    public String getSupportedType() {
        return "START";
    }
}
