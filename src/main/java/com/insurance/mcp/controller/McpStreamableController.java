package com.insurance.mcp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.mcp.handler.McpStreamableHandler;
import com.insurance.mcp.model.McpStreamableRequest;
import com.insurance.mcp.model.McpStreamableResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Streamable HTTP Transport Controller
 * Implements HTTP POST for client-to-server messages with optional SSE for streaming
 * Supports OAuth, bearer tokens, API keys, and custom headers
 */
@Slf4j
@RestController
@RequestMapping("/mcp/v1")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "mcp.server.streamable-http.enabled", havingValue = "true", matchIfMissing = false)
public class McpStreamableController {

    @Autowired
    private McpStreamableHandler mcpStreamableHandler;

    @Autowired
    private ObjectMapper objectMapper;
    
    // 请求去重缓存（防止重复请求）
    private final Map<String, Long> requestCache = new ConcurrentHashMap<>();
    private static final long CACHE_TIMEOUT = 5000; // 5秒缓存

    /**
     * Initialize MCP connection
     * POST /mcp/v1/initialize
     */
    @PostMapping(value = "/initialize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<McpStreamableResponse> initialize(
            @RequestBody McpStreamableRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader Map<String, String> headers,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        
        String clientId = generateClientId();
        
        // 请求去重检查
        String requestKey = generateRequestKey(request, headers);
        if (isDuplicateRequest(requestKey)) {
            log.warn("Duplicate initialize request detected, returning cached response: {}", requestKey);
            return ResponseEntity.ok(createCachedInitializeResponse(request.getId(), clientId));
        }
        
        // 详细日志记录
        log.info("MCP Initialize request - Client: {}, Request ID: {}, Origin: {}, User-Agent: {}", 
            clientId, request.getId(), 
            httpRequest.getHeader("Origin"), 
            httpRequest.getHeader("User-Agent"));
        
        try {
            McpStreamableResponse response = mcpStreamableHandler.initialize(
                request, clientId, authorization, apiKey, headers, jwt);
            
            // 添加缓存头以减少客户端重试
            return ResponseEntity.ok()
                .header("Cache-Control", "private, max-age=300") // 5分钟缓存
                .header("X-Request-ID", request.getId())
                .header("X-Client-ID", clientId)
                .body(response);
        } catch (Exception e) {
            log.error("Error initializing MCP connection", e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse(request.getId(), -32603, "Internal error: " + e.getMessage()));
        }
    }

    /**
     * List available MCP tools
     * POST /mcp/v1/tools/list
     */
    @PostMapping(value = "/tools/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<McpStreamableResponse> listTools(
            @RequestBody McpStreamableRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader Map<String, String> headers,
            @AuthenticationPrincipal Jwt jwt) {
        
        String clientId = extractClientId(headers);
        log.info("MCP List Tools request from client: {}", clientId);
        
        try {
            McpStreamableResponse response = mcpStreamableHandler.listTools(
                request, clientId, authorization, apiKey, headers, jwt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing tools", e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse(request.getId(), -32603, "Internal error: " + e.getMessage()));
        }
    }

    /**
     * Call MCP tool
     * POST /mcp/v1/tools/call
     */
    @PostMapping(value = "/tools/call", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<McpStreamableResponse> callTool(
            @RequestBody McpStreamableRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader Map<String, String> headers,
            @AuthenticationPrincipal Jwt jwt) {
        
        String clientId = extractClientId(headers);
        log.info("MCP Call Tool request from client: {}", clientId);
        
        try {
            McpStreamableResponse response = mcpStreamableHandler.callTool(
                request, clientId, authorization, apiKey, headers, jwt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calling tool", e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse(request.getId(), -32603, "Internal error: " + e.getMessage()));
        }
    }

    /**
     * Create SSE connection for streaming responses
     * GET /mcp/v1/stream/{clientId}
     */
    @GetMapping(value = "/stream/{clientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter createStream(
            @PathVariable String clientId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader Map<String, String> headers,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Creating SSE stream for client: {}", clientId);
        return mcpStreamableHandler.createStream(clientId, authorization, apiKey, headers, jwt);
    }

    /**
     * Send message to existing stream
     * POST /mcp/v1/stream/{clientId}/message
     */
    @PostMapping(value = "/stream/{clientId}/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sendStreamMessage(
            @PathVariable String clientId,
            @RequestBody McpStreamableRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestHeader Map<String, String> headers,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Sending message to stream for client: {}", clientId);
        
        try {
            mcpStreamableHandler.sendStreamMessage(clientId, request, authorization, apiKey, headers, jwt);
            return ResponseEntity.ok(Map.of("status", "message_sent", "clientId", clientId));
        } catch (Exception e) {
            log.error("Error sending stream message", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * Handle CORS preflight requests explicitly
     * OPTIONS /mcp/v1/initialize
     */
    @RequestMapping(value = "/initialize", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handlePreflight(HttpServletRequest request) {
        log.info("Handling CORS preflight request for initialize endpoint");
        
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", request.getHeader("Origin"))
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Key, X-Client-ID, x-custom-auth-headers")
            .header("Access-Control-Allow-Credentials", "true")
            .header("Access-Control-Max-Age", "7200")
            .build();
    }

    /**
     * Health check endpoint
     * GET /mcp/v1/health
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "MCP Streamable HTTP Transport",
            "version", "1.0.0",
            "protocol", "MCP 2024-11-05",
            "timestamp", System.currentTimeMillis()
        ));
    }

    private String generateClientId() {
        return "client_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String extractClientId(Map<String, String> headers) {
        return headers.getOrDefault("X-Client-ID", generateClientId());
    }

    private McpStreamableResponse createErrorResponse(String id, int code, String message) {
        return McpStreamableResponse.builder()
            .jsonrpc("2.0")
            .id(id)
            .error(McpStreamableResponse.McpError.builder()
                .code(code)
                .message(message)
                .build())
            .build();
    }
    
    private String generateRequestKey(McpStreamableRequest request, Map<String, String> headers) {
        String authKey = headers.getOrDefault("Authorization", headers.getOrDefault("X-API-Key", ""));
        return request.getId() + "_" + request.getMethod() + "_" + authKey.hashCode();
    }
    
    private boolean isDuplicateRequest(String requestKey) {
        Long lastRequestTime = requestCache.get(requestKey);
        long currentTime = System.currentTimeMillis();
        
        if (lastRequestTime != null && (currentTime - lastRequestTime) < CACHE_TIMEOUT) {
            return true;
        }
        
        requestCache.put(requestKey, currentTime);
        return false;
    }
    
    private McpStreamableResponse createCachedInitializeResponse(String requestId, String clientId) {
        return McpStreamableResponse.builder()
            .jsonrpc("2.0")
            .id(requestId)
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
                "clientId", clientId,
                "cached", true
            ))
            .build();
    }
}
