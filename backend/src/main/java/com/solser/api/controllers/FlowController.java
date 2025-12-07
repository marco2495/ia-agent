package com.solser.api.controllers;

import com.solser.api.services.FlowService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces")
public class FlowController {

    private final FlowService flowService;

    public FlowController(FlowService flowService) {
        this.flowService = flowService;
    }

    @PostMapping("/{workspaceId}/trigger")
    public Map<String, Object> triggerFlow(
        @PathVariable String workspaceId, 
        @RequestBody Map<String, Object> payload
    ) {
        return flowService.executeFlow(workspaceId, payload);
    }
}
