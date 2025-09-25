package com.insurance.mcp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.mcp.model.McpRequest;
import com.insurance.mcp.model.McpResponse;
import com.insurance.mcp.service.McpToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP Stream handler for MCP protocol communication
 * This handler supports MCP protocol via HTTP Server-Sent Events (SSE)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mcp.server.http-stream.enabled", havingValue = "true", matchIfMissing = false)
public class McpHttpStreamHandler {

    @Autowired
    private McpToolService mcpToolService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        log.info("Initializing MCP HTTP Stream Handler");
    }

    /**
     * Create SSE connection for MCP communication
     */
    public SseEmitter createConnection(String clientId) {
        log.info("Creating SSE connection for client: {}", clientId);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        activeConnections.put(clientId, emitter);
        
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for client: {}", clientId);
            activeConnections.remove(clientId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout for client: {}", clientId);
            activeConnections.remove(clientId);
        });
        
        emitter.onError(throwable -> {
            log.error("SSE connection error for client: {}", clientId, throwable);
            activeConnections.remove(clientId);
        });
        
        // Send initial connection message
        try {
            McpResponse initResponse = McpResponse.builder()
                    .jsonrpc("2.0")
                    .id("connection_init")
                    .result(Map.of(
                        "status", "connected",
                        "clientId", clientId,
                        "protocolVersion", "2024-11-05",
                        "capabilities", Map.of(
                            "tools", Map.of()
                        ),
                        "serverInfo", Map.of(
                            "name", "Insurance Campaign MCP Server (HTTP Stream)",
                            "version", "1.0.0"
                        )
                    ))
                    .build();
            
            sendMessage(clientId, initResponse);
        } catch (Exception e) {
            log.error("Error sending initial message to client: {}", clientId, e);
        }
        
        return emitter;
    }

    /**
     * Handle incoming MCP request via HTTP stream
     */
    public void handleRequest(String clientId, McpRequest request) {
        log.info("Handling MCP request from client: {}, method: {}", clientId, request.getMethod());
        
        try {
            McpResponse response = processMcpRequest(request);
            sendMessage(clientId, response);
        } catch (Exception e) {
            log.error("Error processing MCP request from client: {}", clientId, e);
            McpResponse errorResponse = McpResponse.builder()
                    .jsonrpc("2.0")
                    .id(request.getId())
                    .error(McpResponse.McpError.builder()
                            .code(-32603)
                            .message("Internal error: " + e.getMessage())
                            .build())
                    .build();
            
            try {
                sendMessage(clientId, errorResponse);
            } catch (Exception ex) {
                log.error("Error sending error response to client: {}", clientId, ex);
            }
        }
    }

    /**
     * Send message to specific client
     */
    public void sendMessage(String clientId, McpResponse response) throws IOException {
        SseEmitter emitter = activeConnections.get(clientId);
        if (emitter != null) {
            String messageJson = objectMapper.writeValueAsString(response);
            log.debug("Sending message to client {}: {}", clientId, messageJson);
            
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name("mcp-message")
                    .data(messageJson, MediaType.APPLICATION_JSON));
        } else {
            log.warn("No active connection found for client: {}", clientId);
        }
    }

    /**
     * Broadcast message to all connected clients
     */
    public void broadcastMessage(McpResponse response) {
        log.info("Broadcasting message to {} clients", activeConnections.size());
        
        activeConnections.forEach((clientId, emitter) -> {
            try {
                sendMessage(clientId, response);
            } catch (Exception e) {
                log.error("Error broadcasting message to client: {}", clientId, e);
            }
        });
    }

    /**
     * Close connection for specific client
     */
    public void closeConnection(String clientId) {
        log.info("Closing connection for client: {}", clientId);
        SseEmitter emitter = activeConnections.remove(clientId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("Error closing connection for client: {}", clientId, e);
            }
        }
    }

    /**
     * Get active connections count
     */
    public int getActiveConnectionsCount() {
        return activeConnections.size();
    }

    /**
     * Get list of active client IDs
     */
    public String[] getActiveClientIds() {
        return activeConnections.keySet().toArray(new String[0]);
    }

    private McpResponse processMcpRequest(McpRequest request) {
        String method = request.getMethod();
        String id = request.getId();
        
        switch (method) {
            case "tools/list":
                return mcpToolService.listTools(id);
                
            case "tools/call":
                Map<String, Object> params = request.getParams();
                String toolName = (String) params.get("name");
                Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                return mcpToolService.callTool(id, toolName, arguments);
                
            case "initialize":
                return McpResponse.builder()
                        .jsonrpc("2.0")
                        .id(id)
                        .result(Map.of(
                            "protocolVersion", "2024-11-05",
                            "capabilities", Map.of(
                                "tools", Map.of()
                            ),
                            "serverInfo", Map.of(
                                "name", "Insurance Campaign MCP Server (HTTP Stream)",
                                "version", "1.0.0"
                            )
                        ))
                        .build();
                        
            case "ping":
                return McpResponse.builder()
                        .jsonrpc("2.0")
                        .id(id)
                        .result(Map.of("pong", System.currentTimeMillis()))
                        .build();
                        
            default:
                return McpResponse.builder()
                        .jsonrpc("2.0")
                        .id(id)
                        .error(McpResponse.McpError.builder()
                                .code(-32601)
                                .message("Method not found")
                                .build())
                        .build();
        }
    }
}
