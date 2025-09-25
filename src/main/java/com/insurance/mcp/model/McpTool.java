package com.insurance.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool Definition Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McpTool {
    
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToolSchema {
        @Builder.Default
        private String type = "object";
        private Map<String, Object> properties;
        private List<String> required;
    }
}
