package com.insurance.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * MCP Streamable HTTP Transport Application
 * Implements HTTP POST + SSE for MCP protocol communication
 * Supports OAuth2, bearer tokens, API keys, and custom headers
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class McpStreamableHttpApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpStreamableHttpApplication.class, args);
    }
}
