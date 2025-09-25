package com.insurance.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.mcp.handler.McpStdioHandler;
import com.insurance.mcp.model.McpRequest;
import com.insurance.mcp.model.McpResponse;
import com.insurance.mcp.service.McpToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * MCP Server for Stdio Transport
 * Supports direct stdio communication for MCP clients like Cline
 */
@Slf4j
@SpringBootApplication
@Profile("stdio")
public class McpStdioApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "stdio");
        
        // Start Spring context
        var context = SpringApplication.run(McpStdioApplication.class, args);
        
        // Get required beans
        var mcpStdioHandler = context.getBean(McpStdioHandler.class);
        var objectMapper = context.getBean(ObjectMapper.class);
        
        // Start stdio server
        startStdioServer(mcpStdioHandler, objectMapper);
    }
    
    private static void startStdioServer(McpStdioHandler mcpStdioHandler, ObjectMapper objectMapper) {
        log.info("Starting MCP Server in Stdio mode...");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter writer = new PrintWriter(System.out, true);
        
        String line;
        try {
            while ((line = reader.readLine()) != null) {
            try {
                // Parse incoming MCP request
                McpRequest request = objectMapper.readValue(line, McpRequest.class);
                log.debug("Received MCP request: {}", request.getMethod());
                
                // Process request
                McpResponse response = mcpStdioHandler.handleRequest(request);
                
                // Send response
                String responseJson = objectMapper.writeValueAsString(response);
                writer.println(responseJson);
                writer.flush();
                
                log.debug("Sent MCP response: {}", response.getResult() != null ? "success" : "error");
                
            } catch (Exception e) {
                log.error("Error processing MCP request: {}", e.getMessage(), e);
                
                // Send error response
                McpResponse errorResponse = McpResponse.builder()
                    .jsonrpc("2.0")
                    .id("error")
                    .error(McpResponse.McpError.builder()
                        .code(-32603)
                        .message("Internal error: " + e.getMessage())
                        .build())
                    .build();
                
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                writer.println(errorJson);
                writer.flush();
            }
        }
        } catch (IOException e) {
            log.error("IO error in stdio server", e);
        }
        
        log.info("MCP Stdio server stopped");
    }
}
