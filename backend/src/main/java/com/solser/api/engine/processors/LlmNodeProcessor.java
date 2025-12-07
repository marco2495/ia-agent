package com.solser.api.engine.processors;

import com.solser.api.engine.NodeProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class LlmNodeProcessor implements NodeProcessor {

    private final ChatClient chatClient;

    public LlmNodeProcessor(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public CompletableFuture<Map<String, Object>> process(Map<String, Object> node, Map<String, Object> input) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> data = (Map<String, Object>) node.get("data");
            String promptTemplate = (String) data.getOrDefault("prompt", "");
            
            // Simple template replacement
            String finalPrompt = promptTemplate.isEmpty() 
                ? stringify(input) 
                : promptTemplate.replace("{{input}}", stringify(input));

            String response = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();

            return Map.of("text_response", response);
        });
    }

    @Override
    public String getSupportedType() {
        return "LLM";
    }

    private String stringify(Object input) {
        if (input == null) return "";
        if (input instanceof String) return (String) input;
        return input.toString();
    }
}
