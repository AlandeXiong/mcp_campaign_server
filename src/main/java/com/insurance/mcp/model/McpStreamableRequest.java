package com.insurance.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP Streamable HTTP Request Model
 * Supports HTTP POST for client-to-server messages with optional SSE for streaming
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpStreamableRequest {
    
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("stream")
    private Boolean stream;
    
    // Authentication headers
    @JsonProperty("auth")
    private AuthInfo auth;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthInfo {
        @JsonProperty("type")
        private String type; // "oauth", "bearer", "api_key", "custom"
        
        @JsonProperty("token")
        private String token;
        
        @JsonProperty("api_key")
        private String apiKey;
        
        @JsonProperty("custom_headers")
        private Map<String, String> customHeaders;
    }
}
