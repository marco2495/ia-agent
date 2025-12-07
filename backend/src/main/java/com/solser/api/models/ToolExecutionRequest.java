package com.solser.api.models;

import java.util.Map;
import lombok.Data;

@Data
public class ToolExecutionRequest {
    private String toolType;
    private Map<String, Object> config;
    private String resolvedAction;
    private String rawInput;
    private String workspaceId;
}
