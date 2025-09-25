package com.insurance.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * MCP Protocol Request Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class McpRequest {
    
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";
    
    private String id;
    private String method;
    private Map<String, Object> params;
}
