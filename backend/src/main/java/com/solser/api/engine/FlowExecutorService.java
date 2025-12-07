package com.solser.api.engine;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class FlowExecutorService {

    private final Map<String, NodeProcessor> processors;

    public FlowExecutorService(List<NodeProcessor> processorList) {
        this.processors = processorList.stream()
            .collect(Collectors.toMap(NodeProcessor::getSupportedType, p -> p));
    }

    public CompletableFuture<Map<String, Object>> executeHelper(
        Map<String, Object> workspace, 
        Map<String, Object> initialInput
    ) {
        // Build Graph
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) workspace.get("nodes");
        List<Map<String, Object>> connections = (List<Map<String, Object>>) workspace.get("connections");
        
        Map<String, Map<String, Object>> nodeMap = nodes.stream()
            .collect(Collectors.toMap(n -> (String) n.get("id"), n -> n));
            
        Map<String, List<String>> adjList = new ConcurrentHashMap<>();
        if (connections != null) {
            for (Map<String, Object> conn : connections) {
                String from = (String) conn.get("from");
                String to = (String) conn.get("to");
                adjList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            }
        }

        // Collection to store outputs from terminal nodes (leafs)
        List<Map<String, Object>> terminalOutputs = new CopyOnWriteArrayList<>();

        // Find Start Nodes
        List<String> startNodeIds = nodes.stream()
            .filter(n -> "START".equals(n.get("type")))
            .map(n -> (String) n.get("id"))
            .collect(Collectors.toList());

        // Recursive Execution
        List<CompletableFuture<Void>> futures = startNodeIds.stream()
            .map(id -> processNodeRecursively(id, initialInput, nodeMap, adjList, terminalOutputs))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                if (!terminalOutputs.isEmpty()) {
                    // Return the last terminal output, or merge them. 
                    // For this simple chat flow, the last leaf is the Output node.
                    // We return the map from it.
                    return terminalOutputs.get(terminalOutputs.size() - 1);
                }
                return Map.of("status", "success", "message", "Flow executed (No Output)");
            });
    }

    private CompletableFuture<Void> processNodeRecursively(
        String nodeId, 
        Map<String, Object> input,
        Map<String, Map<String, Object>> nodeMap,
        Map<String, List<String>> adjList,
        List<Map<String, Object>> terminalOutputs
    ) {
        Map<String, Object> node = nodeMap.get(nodeId);
        if (node == null) return CompletableFuture.completedFuture(null);

        String type = (String) node.get("type");
        NodeProcessor processor = processors.get(type);

        CompletableFuture<Map<String, Object>> execution;
        if (processor != null) {
            execution = processor.process(node, input);
        } else {
            // Default pass-through for unknown nodes or START nodes without specific logic
            execution = CompletableFuture.completedFuture(input);
        }

        return execution.thenCompose(result -> {
            List<String> children = adjList.getOrDefault(nodeId, Collections.emptyList());
            
            // If leaf node, capture result
            if (children.isEmpty()) { 
                terminalOutputs.add(result);
                return CompletableFuture.completedFuture(null);
            }

            List<CompletableFuture<Void>> childFutures = children.stream()
                .map(childId -> processNodeRecursively(childId, result, nodeMap, adjList, terminalOutputs))
                .collect(Collectors.toList());
            
            return CompletableFuture.allOf(childFutures.toArray(new CompletableFuture[0]));
        });
    }
}
