package com.solser.api.services;

import com.solser.api.engine.FlowExecutorService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FlowService {

    private final FlowExecutorService executorService;
    private final WorkspaceTemplatesService templatesService;

    public FlowService(FlowExecutorService executorService, WorkspaceTemplatesService templatesService) {
        this.executorService = executorService;
        this.templatesService = templatesService;
    }

    public Map<String, Object> executeFlow(String workspaceId, Map<String, Object> inputData) {
        // In a real scenario we would fetch the workspace from DB by ID.
        // Here we look up in templates for demo purposes.
        Map<String, Object> workspace = templatesService.getTemplates().stream()
                .filter(w -> w.get("id").equals(workspaceId))
                .findFirst()
                .orElse(null);

        if (workspace == null) {
            throw new RuntimeException("Workspace not found: " + workspaceId);
        }

        try {
            return executorService.executeHelper(workspace, inputData).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Flow execution failed", e);
        }
    }
}
