package com.insurance.mcp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.mcp.model.McpStreamableRequest;
import com.insurance.mcp.model.McpStreamableResponse;
import com.insurance.mcp.model.McpTool;
import com.insurance.mcp.service.McpToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MCP Streamable HTTP Handler
 * Handles MCP protocol via HTTP POST + SSE with authentication support
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mcp.server.streamable-http.enabled", havingValue = "true", matchIfMissing = false)
public class McpStreamableHandler {

    @Autowired
    private McpToolService mcpToolService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, SseEmitter> activeStreams = new ConcurrentHashMap<>();
    private final Map<String, Authentication> clientAuth = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostConstruct
    public void initialize() {
        log.info("Initializing MCP Streamable HTTP Handler");
    }

    /**
     * Initialize MCP connection with authentication
     */
    public McpStreamableResponse initialize(
            McpStreamableRequest request,
            String clientId,
            String authorization,
            String apiKey,
            Map<String, String> headers,
            Jwt jwt) {
        
        log.info("Initializing MCP connection for client: {}", clientId);
        
        // Validate authentication
        Authentication auth = validateAuthentication(authorization, apiKey, headers, jwt);
        if (auth != null) {
            clientAuth.put(clientId, auth);
        }

        return McpStreamableResponse.builder()
            .jsonrpc("2.0")
            .id(request.getId())
            .result(Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                    "tools", Map.of(),
                    "streaming", true,
                    "authentication", Map.of(
                        "supported", Arrays.asList("oauth", "bearer", "api_key", "custom"),
                        "required", false
                    )
                ),
                "serverInfo", Map.of(
                    "name", "Insurance Campaign MCP Server",
                    "version", "1.0.0",
                    "transport", "streamable_http"
                ),
                "clientId", clientId
            ))
            .build();
    }

    /**
     * List available tools with enhanced Inspector support
     */
    public McpStreamableResponse listTools(
            McpStreamableRequest request,
            String clientId,
            String authorization,
            String apiKey,
            Map<String, String> headers,
            Jwt jwt) {
        
        log.info("Listing tools for client: {}", clientId);
        
        List<McpTool> tools = Arrays.asList(
            createAudienceTargetingTool(),
            createCampaignContentTool(),
            createInspectorTool(),
            createStreamingTool()
        );

        return McpStreamableResponse.builder()
            .jsonrpc("2.0")
            .id(request.getId())
            .result(Map.of(
                "tools", tools,
                "streaming", true,
                "clientId", clientId
            ))
            .build();
    }

    /**
     * Call tool with streaming support
     */
    public McpStreamableResponse callTool(
            McpStreamableRequest request,
            String clientId,
            String authorization,
            String apiKey,
            Map<String, String> headers,
            Jwt jwt) {
        
        String toolName = (String) request.getParams().get("name");
        Map<String, Object> arguments = (Map<String, Object>) request.getParams().get("arguments");
        Boolean stream = request.getStream();
        
        log.info("Calling tool: {} for client: {} (streaming: {})", toolName, clientId, stream);

        try {
            if (Boolean.TRUE.equals(stream)) {
                // Return streaming response
                return createStreamingResponse(request.getId(), toolName, arguments, clientId);
            } else {
                // Return immediate response
                return createImmediateResponse(request.getId(), toolName, arguments, clientId);
            }
        } catch (Exception e) {
            log.error("Error calling tool: {}", toolName, e);
            return McpStreamableResponse.builder()
                .jsonrpc("2.0")
                .id(request.getId())
                .error(McpStreamableResponse.McpError.builder()
                    .code(-32603)
                    .message("Tool execution error: " + e.getMessage())
                    .build())
                .build();
        }
    }

    /**
     * Create SSE stream for streaming responses
     */
    public SseEmitter createStream(String clientId, String authorization, String apiKey, 
                                   Map<String, String> headers, Jwt jwt) {
        
        log.info("Creating SSE stream for client: {}", clientId);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        activeStreams.put(clientId, emitter);
        
        emitter.onCompletion(() -> {
            log.info("SSE stream completed for client: {}", clientId);
            activeStreams.remove(clientId);
            clientAuth.remove(clientId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE stream timed out for client: {}", clientId);
            activeStreams.remove(clientId);
            clientAuth.remove(clientId);
        });
        
        emitter.onError(throwable -> {
            log.error("SSE stream error for client: {}: {}", clientId, throwable.getMessage());
            activeStreams.remove(clientId);
            clientAuth.remove(clientId);
        });

        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data(Map.of(
                    "clientId", clientId,
                    "status", "connected",
                    "timestamp", System.currentTimeMillis()
                )));
        } catch (IOException e) {
            log.error("Error sending initial SSE message", e);
        }

        return emitter;
    }

    /**
     * Send message to existing stream
     */
    public void sendStreamMessage(String clientId, McpStreamableRequest request, 
                                  String authorization, String apiKey, 
                                  Map<String, String> headers, Jwt jwt) {
        
        SseEmitter emitter = activeStreams.get(clientId);
        if (emitter == null) {
            throw new IllegalStateException("No active stream found for client: " + clientId);
        }

        executor.submit(() -> {
            try {
                String toolName = (String) request.getParams().get("name");
                Map<String, Object> arguments = (Map<String, Object>) request.getParams().get("arguments");
                
                log.info("Processing stream message for tool: {} on client: {}", toolName, clientId);
                
                // Process the tool call and stream results
                processStreamingToolCall(emitter, request.getId(), toolName, arguments, clientId);
                
            } catch (Exception e) {
                log.error("Error processing stream message", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of(
                            "error", e.getMessage(),
                            "clientId", clientId,
                            "timestamp", System.currentTimeMillis()
                        )));
                } catch (IOException ioException) {
                    log.error("Error sending error event", ioException);
                }
            }
        });
    }

    private McpStreamableResponse createImmediateResponse(String id, String toolName, 
                                                          Map<String, Object> arguments, String clientId) {
        Object result = executeToolCall(toolName, arguments);
        
        return McpStreamableResponse.builder()
            .jsonrpc("2.0")
            .id(id)
            .result(Map.of(
                "content", Arrays.asList(
                    McpStreamableResponse.ContentChunk.builder()
                        .type("text")
                        .text(result.toString())
                        .index(0)
                        .build()
                ),
                "clientId", clientId
            ))
            .build();
    }

    private McpStreamableResponse createStreamingResponse(String id, String toolName, 
                                                          Map<String, Object> arguments, String clientId) {
        return McpStreamableResponse.builder()
            .jsonrpc("2.0")
            .id(id)
            .result(Map.of(
                "streaming", true,
                "streamId", clientId,
                "message", "Tool execution started, check stream for results",
                "clientId", clientId
            ))
            .build();
    }

    private void processStreamingToolCall(SseEmitter emitter, String id, String toolName, 
                                         Map<String, Object> arguments, String clientId) throws IOException {
        
        log.info("Processing streaming tool call: {} for client: {}", toolName, clientId);
        
        // Send start event
        emitter.send(SseEmitter.event()
            .name("tool_start")
            .data(Map.of(
                "tool", toolName,
                "clientId", clientId,
                "timestamp", System.currentTimeMillis()
            )));

        // Simulate streaming response for long-running operations
        Object result = executeToolCall(toolName, arguments);
        
        if (result instanceof Map) {
            Map<String, Object> resultMap = (Map<String, Object>) result;
            if (resultMap.containsKey("streaming_content")) {
                // Handle streaming content
                List<Map<String, Object>> chunks = (List<Map<String, Object>>) resultMap.get("streaming_content");
                for (int i = 0; i < chunks.size(); i++) {
                    Map<String, Object> chunk = chunks.get(i);
                    
                    emitter.send(SseEmitter.event()
                        .name("chunk")
                        .data(Map.of(
                            "index", i,
                            "total", chunks.size(),
                            "isLast", i == chunks.size() - 1,
                            "content", chunk,
                            "clientId", clientId
                        )));
                    
                    // Add delay for demonstration
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Send completion event
        emitter.send(SseEmitter.event()
            .name("tool_complete")
            .data(Map.of(
                "tool", toolName,
                "result", result,
                "clientId", clientId,
                "timestamp", System.currentTimeMillis()
            )));
    }

    private Object executeToolCall(String toolName, Map<String, Object> arguments) {
        switch (toolName) {
            case "audience_targeting":
                return mcpToolService.callTool("1", "audience_targeting", arguments).getResult();
            case "campaign_content_recommendation":
                return mcpToolService.callTool("1", "campaign_content_recommendation", arguments).getResult();
            case "inspector_query":
                return mcpToolService.callTool("1", "inspector_query", arguments).getResult();
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private Authentication validateAuthentication(String authorization, String apiKey, 
                                                 Map<String, String> headers, Jwt jwt) {
        // Implementation depends on your authentication strategy
        // This is a placeholder for authentication validation
        return null;
    }

    private McpTool createAudienceTargetingTool() {
        return McpTool.builder()
            .name("audience_targeting")
            .description("Recommends audience targeting criteria for insurance marketing campaigns")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "requirements", Map.of("type", "string", "description", "Targeting requirements"),
                    "insurance_type", Map.of("type", "string", "description", "Type of insurance"),
                    "campaign_objective", Map.of("type", "string", "description", "Campaign objective")
                ),
                "required", Arrays.asList("requirements", "insurance_type")
            ))
            .build();
    }

    private McpTool createCampaignContentTool() {
        return McpTool.builder()
            .name("campaign_content_recommendation")
            .description("Recommends campaign content and messaging for insurance marketing")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "audience_criteria", Map.of("type", "string", "description", "Target audience criteria"),
                    "insurance_type", Map.of("type", "string", "description", "Type of insurance"),
                    "content_type", Map.of("type", "string", "description", "Type of content needed")
                ),
                "required", Arrays.asList("audience_criteria", "insurance_type")
            ))
            .build();
    }

    private McpTool createInspectorTool() {
        return McpTool.builder()
            .name("inspector_query")
            .description("Enhanced Claude Inspector tool integration for deep code analysis and querying")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "query", Map.of("type", "string", "description", "Query for Claude Inspector tool"),
                    "context", Map.of("type", "string", "description", "Additional context for the query"),
                    "depth", Map.of("type", "string", "enum", Arrays.asList("shallow", "deep"), "description", "Analysis depth")
                ),
                "required", Arrays.asList("query")
            ))
            .build();
    }

    private McpTool createStreamingTool() {
        return McpTool.builder()
            .name("streaming_analysis")
            .description("Provides streaming analysis capabilities with real-time updates")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "analysis_type", Map.of("type", "string", "description", "Type of analysis to perform"),
                    "stream_results", Map.of("type", "boolean", "description", "Whether to stream results")
                ),
                "required", Arrays.asList("analysis_type")
            ))
            .build();
    }
}
