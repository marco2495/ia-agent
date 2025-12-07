package com.solser.api.engine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface NodeProcessor {
    /**
     * Executes the logic for a specific node type.
     * @param node The node configuration data.
     * @param input The input payload from the previous node.
     * @return A processing result containing the output or next actions.
     */
    CompletableFuture<Map<String, Object>> process(Map<String, Object> node, Map<String, Object> input);
    
    /**
     * Returns the NodeType string this processor handles (e.g., "LLM", "TOOL").
     */
    String getSupportedType();
}
