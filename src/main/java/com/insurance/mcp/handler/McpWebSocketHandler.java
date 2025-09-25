package com.insurance.mcp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.mcp.model.McpRequest;
import com.insurance.mcp.model.McpResponse;
import com.insurance.mcp.service.McpToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;

/**
 * WebSocket handler for MCP protocol communication
 */
@Slf4j
@Component
public class McpWebSocketHandler implements WebSocketHandler {

    @Autowired
    private McpToolService mcpToolService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("MCP WebSocket connection established: {}", session.getId());
        
        // Send initialization message
        McpResponse response = McpResponse.builder()
                .jsonrpc("2.0")
                .id("init")
                .result(Map.of(
                    "protocolVersion", "2024-11-05",
                    "capabilities", Map.of(
                        "tools", Map.of()
                    ),
                    "serverInfo", Map.of(
                        "name", "Insurance Campaign MCP Server",
                        "version", "1.0.0"
                    )
                ))
                .build();
        
        sendMessage(session, response);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            log.info("Received MCP message: {}", payload);
            
            try {
                McpRequest request = objectMapper.readValue(payload, McpRequest.class);
                McpResponse response = handleMcpRequest(request);
                sendMessage(session, response);
            } catch (Exception e) {
                log.error("Error processing MCP request", e);
                McpResponse errorResponse = McpResponse.builder()
                        .jsonrpc("2.0")
                        .id("error")
                        .error(McpResponse.McpError.builder()
                                .code(-32603)
                                .message("Internal error")
                                .build())
                        .build();
                sendMessage(session, errorResponse);
            }
        }
    }

    private McpResponse handleMcpRequest(McpRequest request) {
        String method = request.getMethod();
        String id = request.getId();
        
        try {
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
                                    "name", "Insurance Campaign MCP Server",
                                    "version", "1.0.0"
                                )
                            ))
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
        } catch (Exception e) {
            log.error("Error handling MCP request: {}", method, e);
            return McpResponse.builder()
                    .jsonrpc("2.0")
                    .id(id)
                    .error(McpResponse.McpError.builder()
                            .code(-32603)
                            .message("Internal error: " + e.getMessage())
                            .build())
                    .build();
        }
    }

    private void sendMessage(WebSocketSession session, McpResponse response) {
        try {
            String message = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(message));
            log.info("Sent MCP response: {}", message);
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("MCP WebSocket connection closed: {} - {}", session.getId(), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
