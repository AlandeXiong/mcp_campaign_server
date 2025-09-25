package com.insurance.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP Streamable HTTP Response Model
 * Supports streaming responses via Server-Sent Events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpStreamableResponse {
    
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("result")
    private Map<String, Object> result;
    
    @JsonProperty("error")
    private McpError error;
    
    @JsonProperty("stream")
    private Boolean stream;
    
    @JsonProperty("chunk")
    private ChunkInfo chunk;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpError {
        @JsonProperty("code")
        private Integer code;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("data")
        private Map<String, Object> data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkInfo {
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("total")
        private Integer total;
        
        @JsonProperty("is_last")
        private Boolean isLast;
        
        @JsonProperty("content")
        private String content;
    }
    
    // Streamable response types
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamableResult {
        @JsonProperty("content")
        private List<ContentChunk> content;
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        
        @JsonProperty("streaming")
        private Boolean streaming;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentChunk {
        @JsonProperty("type")
        private String type; // "text", "image", "data"
        
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("data")
        private String data;
        
        @JsonProperty("mimeType")
        private String mimeType;
        
        @JsonProperty("index")
        private Integer index;
    }
}
