package com.solser.api.engine.processors;

import com.solser.api.engine.NodeProcessor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class LlmNodeProcessor implements NodeProcessor {

    // Optional injection in case one of them is missing
    @Autowired(required = false)
    private OllamaChatModel ollamaChatModel;

    @Autowired(required = false)
    private OpenAiChatModel openAiChatModel;

    public LlmNodeProcessor() {
        // Default constructor for Spring to use when autowiring fields
    }

    @Override
    public CompletableFuture<Map<String, Object>> process(Map<String, Object> node, Map<String, Object> input) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> data = (Map<String, Object>) node.get("data");
            String promptTemplate = (String) data.getOrDefault("prompt", "");
            String provider = (String) data.getOrDefault("provider", "openai");
            String model = (String) data.getOrDefault("model", "");

            // Simple template replacement
            String finalPrompt = promptTemplate.isEmpty() 
                ? stringify(input) 
                : promptTemplate.replace("{{input}}", stringify(input));

            ChatModel targetModel = resolveChatModel(provider);
            
            if (targetModel == null) {
                throw new RuntimeException("No ChatModel found for provider: " + provider);
            }

            // Check if model specific options needed
            Prompt promptRequest;
            if (targetModel instanceof OllamaChatModel && !model.isEmpty()) {
                 promptRequest = new Prompt(finalPrompt, org.springframework.ai.ollama.api.OllamaOptions.create().withModel(model));
            } else {
                 promptRequest = new Prompt(finalPrompt);
            }

            ChatResponse response = targetModel.call(promptRequest);
            String responseText = response.getResult().getOutput().getContent();

            return Map.of("text_response", responseText);
        });
    }

    private ChatModel resolveChatModel(String provider) {
        if ("ollama".equalsIgnoreCase(provider) && ollamaChatModel != null) {
            return ollamaChatModel;
        }
        if ("openai".equalsIgnoreCase(provider) && openAiChatModel != null) {
            return openAiChatModel;
        }
        // Fallback to whatever is available
        if (openAiChatModel != null) return openAiChatModel;
        if (ollamaChatModel != null) return ollamaChatModel;
        return null;
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
