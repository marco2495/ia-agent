package com.solser.api.services;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class WorkspaceTemplatesService {

    public List<Map<String, Object>> getTemplates() {
        // Migration of workspaceTemplates.js data
        List<Map<String, Object>> templates = new ArrayList<>();

        templates.add(Map.of(
            "id", "tpl-basic-chat",
            "name", "Chat Básico con IA",
            "description", "Un asistente simple que responde preguntas usando un modelo de lenguaje.",
            "nodes", List.of(
                Map.of("id", "start", "type", "START", "position", Map.of("x", 100, "y", 300)),
                Map.of("id", "llm-node", "type", "LLM", "position", Map.of("x", 400, "y", 300), "data", Map.of("label", "Asistente AI", "model", "llama3.1:8b", "provider", "ollama")),
                Map.of("id", "output", "type", "OUTPUT", "position", Map.of("x", 700, "y", 300))
            ),
            "connections", List.of(
                Map.of("id", "c1", "from", "start", "to", "llm-node"),
                Map.of("id", "c2", "from", "llm-node", "to", "output")
            )
        ));

        // Add more templates as needed base on workspaceTemplates.js content
        
        return templates;
    }
}
