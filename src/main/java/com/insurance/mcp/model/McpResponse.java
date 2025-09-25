package com.insurance.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * MCP Protocol Response Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McpResponse {
    
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";
    
    private String id;
    private Object result;
    private McpError error;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class McpError {
        private int code;
        private String message;
        private Object data;
    }
}
