package com.insurance.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.mcp.model.McpRequest;
import com.insurance.mcp.model.McpResponse;
import com.insurance.mcp.service.campaign.AudienceTargetingService;
import com.insurance.mcp.service.campaign.CampaignContentService;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Standalone MCP Server for HTTP Stream communication
 * This class provides a lightweight MCP server that communicates via HTTP streams
 */
@Slf4j
public class McpHttpStreamApplication {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AudienceTargetingService audienceTargetingService = new AudienceTargetingService();
    private static final CampaignContentService campaignContentService = new CampaignContentService();
    private static final Map<String, PrintWriter> activeConnections = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    
    private static int port = 8081;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        // Parse command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.warn("Invalid port number: {}. Using default port: {}", args[0], port);
            }
        }
        
        log.info("Starting MCP HTTP Stream Server on port {}", port);
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("MCP HTTP Stream Server listening on port {}", port);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        log.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error starting HTTP Stream server", e);
        } finally {
            executorService.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final String clientId;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.clientId = UUID.randomUUID().toString();
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                log.info("Client connected: {}", clientId);
                activeConnections.put(clientId, writer);
                
                // Send HTTP headers
                writer.println("HTTP/1.1 200 OK");
                writer.println("Content-Type: text/event-stream");
                writer.println("Cache-Control: no-cache");
                writer.println("Connection: keep-alive");
                writer.println("Access-Control-Allow-Origin: *");
                writer.println("Access-Control-Allow-Headers: Cache-Control");
                writer.println();
                writer.flush();
                
                // Send initial connection message
                sendEvent(writer, "connection", Map.of(
                    "status", "connected",
                    "clientId", clientId,
                    "protocolVersion", "2024-11-05"
                ));
                
                String line;
                StringBuilder requestBuilder = new StringBuilder();
                boolean inBody = false;
                int contentLength = 0;
                
                while ((line = reader.readLine()) != null && running) {
                    if (line.isEmpty() && !inBody) {
                        inBody = true;
                        continue;
                    }
                    
                    if (inBody) {
                        requestBuilder.append(line);
                        if (requestBuilder.length() >= contentLength) {
                            String requestBody = requestBuilder.toString();
                            if (!requestBody.trim().isEmpty()) {
                                handleRequest(requestBody, writer);
                            }
                            requestBuilder.setLength(0);
                            inBody = false;
                            contentLength = 0;
                        }
                    } else if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.substring(15).trim());
                    }
                }
                
            } catch (IOException e) {
                log.error("Error handling client connection: {}", clientId, e);
            } finally {
                log.info("Client disconnected: {}", clientId);
                activeConnections.remove(clientId);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log.error("Error closing client socket", e);
                }
            }
        }
        
        private void handleRequest(String requestBody, PrintWriter writer) {
            try {
                log.debug("Received request from {}: {}", clientId, requestBody);
                
                McpRequest request = objectMapper.readValue(requestBody, McpRequest.class);
                McpResponse response = processRequest(request);
                
                String responseJson = objectMapper.writeValueAsString(response);
                sendEvent(writer, "mcp-response", responseJson);
                
            } catch (Exception e) {
                log.error("Error processing request from client: {}", clientId, e);
                
                try {
                    McpResponse errorResponse = McpResponse.builder()
                            .jsonrpc("2.0")
                            .id("error")
                            .error(McpResponse.McpError.builder()
                                    .code(-32603)
                                    .message("Internal error: " + e.getMessage())
                                    .build())
                            .build();
                    
                    String errorJson = objectMapper.writeValueAsString(errorResponse);
                    sendEvent(writer, "mcp-error", errorJson);
                } catch (Exception ex) {
                    log.error("Error sending error response", ex);
                }
            }
        }
        
        private void sendEvent(PrintWriter writer, String eventType, Object data) {
            try {
                String dataJson = data instanceof String ? (String) data : objectMapper.writeValueAsString(data);
                
                writer.println("event: " + eventType);
                writer.println("data: " + dataJson);
                writer.println();
                writer.flush();
                
                log.debug("Sent event to {}: {} - {}", clientId, eventType, dataJson);
            } catch (Exception e) {
                log.error("Error sending event to client: {}", clientId, e);
            }
        }
    }

    private static McpResponse processRequest(McpRequest request) {
        String method = request.getMethod();
        String id = request.getId();
        
        switch (method) {
            case "tools/list":
                return listTools(id);
                
            case "tools/call":
                Map<String, Object> params = request.getParams();
                String toolName = (String) params.get("name");
                Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                return callTool(id, toolName, arguments);
                
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

    private static McpResponse listTools(String id) {
        List<Map<String, Object>> tools = Arrays.asList(
            Map.of(
                "name", "audience_targeting",
                "description", "Provides audience targeting criteria recommendations for insurance marketing campaigns",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "requirements", Map.of("type", "string", "description", "User requirements for audience targeting"),
                        "insurance_type", Map.of("type", "string", "description", "Type of insurance product"),
                        "campaign_objective", Map.of("type", "string", "description", "Campaign objective")
                    ),
                    "required", Arrays.asList("requirements")
                )
            ),
            Map.of(
                "name", "campaign_content_recommendation",
                "description", "Recommends personalized campaign content for insurance marketing",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "audience_criteria", Map.of("type", "object", "description", "Target audience criteria"),
                        "insurance_type", Map.of("type", "string", "description", "Type of insurance product"),
                        "channel", Map.of("type", "string", "description", "Marketing channel"),
                        "campaign_goal", Map.of("type", "string", "description", "Campaign goal")
                    ),
                    "required", Arrays.asList("audience_criteria", "insurance_type")
                )
            ),
            Map.of(
                "name", "inspector_query",
                "description", "Executes queries for Claude Inspector tool integration",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "query", Map.of("type", "string", "description", "Query for Claude Inspector tool")
                    ),
                    "required", Arrays.asList("query")
                )
            )
        );

        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(Map.of("tools", tools))
                .build();
    }

    private static McpResponse callTool(String id, String toolName, Map<String, Object> arguments) {
        try {
            Object result;
            
            switch (toolName) {
                case "audience_targeting":
                    result = audienceTargetingService.recommendAudienceCriteria(arguments);
                    break;
                    
                case "campaign_content_recommendation":
                    result = campaignContentService.recommendCampaignContent(arguments);
                    break;
                    
                case "inspector_query":
                    result = handleInspectorQuery(arguments);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown tool: " + toolName);
            }

            return McpResponse.builder()
                    .jsonrpc("2.0")
                    .id(id)
                    .result(Map.of("content", result))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error calling tool: {}", toolName, e);
            return McpResponse.builder()
                    .jsonrpc("2.0")
                    .id(id)
                    .error(McpResponse.McpError.builder()
                            .code(-32603)
                            .message("Tool execution error: " + e.getMessage())
                            .build())
                    .build();
        }
    }

    private static Object handleInspectorQuery(Map<String, Object> arguments) {
        String query = (String) arguments.get("query");
        log.info("Processing Inspector query: {}", query);
        
        return Map.of(
            "query", query,
            "status", "processed",
            "message", "Inspector query processed successfully",
            "timestamp", System.currentTimeMillis()
        );
    }
}
