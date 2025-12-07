package com.solser.api.controllers;

import com.solser.api.services.WorkspaceTemplatesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceTemplatesService templatesService;

    public WorkspaceController(WorkspaceTemplatesService templatesService) {
        this.templatesService = templatesService;
    }

    @GetMapping("/templates")
    public List<Map<String, Object>> getTemplates() {
        return templatesService.getTemplates();
    }
}
