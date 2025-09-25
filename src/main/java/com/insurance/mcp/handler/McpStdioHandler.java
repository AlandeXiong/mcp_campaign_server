package com.insurance.mcp.handler;

import com.insurance.mcp.model.McpRequest;
import com.insurance.mcp.model.McpResponse;
import com.insurance.mcp.service.McpToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for MCP Stdio Transport requests
 * Processes MCP requests and delegates to appropriate services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpStdioHandler {

    private final McpToolService mcpToolService;

    public McpResponse handleRequest(McpRequest request) {
        log.info("Processing MCP request: method={}, id={}", request.getMethod(), request.getId());

        try {
            switch (request.getMethod()) {
                case "initialize":
                    return handleInitialize(request);
                case "tools/list":
                    return handleToolsList(request);
                case "tools/call":
                    return handleToolsCall(request);
                case "notifications/initialized":
                    return handleInitialized(request);
                default:
                    return createErrorResponse(request.getId(), -32601, 
                        "Method not found: " + request.getMethod());
            }
        } catch (Exception e) {
            log.error("Error handling MCP request", e);
            return createErrorResponse(request.getId(), -32603, 
                "Internal error: " + e.getMessage());
        }
    }

    private McpResponse handleInitialize(McpRequest request) {
        log.info("Handling initialize request");
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", new HashMap<>());
        capabilities.put("streaming", true);
        capabilities.put("authentication", Map.of(
            "supported", new String[]{"oauth", "bearer", "api_key", "custom"},
            "required", false
        ));

        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "Insurance Campaign MCP Server");
        serverInfo.put("version", "1.0.0");
        serverInfo.put("transport", "stdio");

        Map<String, Object> result = new HashMap<>();
        result.put("capabilities", capabilities);
        result.put("serverInfo", serverInfo);
        result.put("protocolVersion", "2024-11-05");

        return McpResponse.builder()
            .jsonrpc("2.0")
            .id(request.getId())
            .result(result)
            .build();
    }

    private McpResponse handleToolsList(McpRequest request) {
        log.info("Handling tools/list request");
        
        try {
            var response = mcpToolService.listTools(request.getId());
            return response;
        } catch (Exception e) {
            log.error("Error listing tools", e);
            return createErrorResponse(request.getId(), -32603, 
                "Error listing tools: " + e.getMessage());
        }
    }

    private McpResponse handleToolsCall(McpRequest request) {
        log.info("Handling tools/call request");
        
        try {
            Map<String, Object> params = (Map<String, Object>) request.getParams();
            String toolName = (String) params.get("name");
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            var response = mcpToolService.callTool(request.getId(), toolName, arguments);
            return response;
        } catch (Exception e) {
            log.error("Error calling tool", e);
            return createErrorResponse(request.getId(), -32603, 
                "Error calling tool: " + e.getMessage());
        }
    }

    private McpResponse handleInitialized(McpRequest request) {
        log.info("Handling notifications/initialized");
        
        // Just acknowledge the notification
        return McpResponse.builder()
            .jsonrpc("2.0")
            .id(request.getId())
            .result(new HashMap<>())
            .build();
    }

    private McpResponse createErrorResponse(String id, int code, String message) {
        return McpResponse.builder()
            .jsonrpc("2.0")
            .id(id)
            .error(McpResponse.McpError.builder()
                .code(code)
                .message(message)
                .build())
            .build();
    }
}