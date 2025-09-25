package com.insurance.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.mcp.model.McpResponse;
import com.insurance.mcp.model.McpTool;
import com.insurance.mcp.service.campaign.AudienceTargetingService;
import com.insurance.mcp.service.campaign.CampaignContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for handling MCP tool operations
 */
@Slf4j
@Service
public class McpToolService {

    @Autowired
    private AudienceTargetingService audienceTargetingService;

    @Autowired
    private CampaignContentService campaignContentService;

    @Autowired
    private InspectorService inspectorService;

    @Autowired
    private ObjectMapper objectMapper;

    public McpResponse listTools(String id) {
        List<McpTool> tools = Arrays.asList(
            createAudienceTargetingTool(),
            createCampaignContentTool(),
            createInspectorTool()
        );

        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(Map.of("tools", tools))
                .build();
    }

    public McpResponse callTool(String id, String toolName, Map<String, Object> arguments) {
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
                    result = inspectorService.processInspectorQuery(arguments);
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

    private McpTool createAudienceTargetingTool() {
        Map<String, Object> properties = Map.of(
            "requirements", Map.of(
                "type", "string",
                "description", "User requirements for audience targeting"
            ),
            "insurance_type", Map.of(
                "type", "string",
                "description", "Type of insurance (life, health, auto, property, etc.)"
            ),
            "campaign_objective", Map.of(
                "type", "string",
                "description", "Campaign objective (acquisition, retention, upselling, etc.)"
            )
        );

        return McpTool.builder()
                .name("audience_targeting")
                .description("Provides audience targeting criteria recommendations for insurance marketing campaigns based on user requirements")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", properties,
                    "required", Arrays.asList("requirements")
                ))
                .build();
    }

    private McpTool createCampaignContentTool() {
        Map<String, Object> properties = Map.of(
            "audience_criteria", Map.of(
                "type", "object",
                "description", "Target audience criteria"
            ),
            "insurance_type", Map.of(
                "type", "string",
                "description", "Type of insurance product"
            ),
            "channel", Map.of(
                "type", "string",
                "description", "Marketing channel (email, sms, social_media, etc.)"
            ),
            "campaign_goal", Map.of(
                "type", "string",
                "description", "Campaign goal (awareness, conversion, retention, etc.)"
            )
        );

        return McpTool.builder()
                .name("campaign_content_recommendation")
                .description("Recommends personalized campaign content for insurance marketing based on audience criteria and campaign objectives")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", properties,
                    "required", Arrays.asList("audience_criteria", "insurance_type")
                ))
                .build();
    }

    private McpTool createInspectorTool() {
        Map<String, Object> properties = Map.of(
            "query", Map.of(
                "type", "string",
                "description", "Query for Claude Inspector tool"
            )
        );

        return McpTool.builder()
                .name("inspector_query")
                .description("Executes queries for Claude Inspector tool integration")
                .inputSchema(Map.of(
                    "type", "object",
                    "properties", properties,
                    "required", Arrays.asList("query")
                ))
                .build();
    }

    private Object handleInspectorQuery(Map<String, Object> arguments) {
        String query = (String) arguments.get("query");
        log.info("Processing Inspector query: {}", query);
        
        // Enhanced Inspector integration for Claude MCP Inspector tool
        return Map.of(
            "query", query,
            "status", "processed",
            "message", "Inspector query processed successfully",
            "timestamp", System.currentTimeMillis(),
            "inspectorCapabilities", Map.of(
                "codeAnalysis", true,
                "documentationLookup", true,
                "dependencyAnalysis", true,
                "securityScanning", true,
                "performanceAnalysis", true
            ),
            "availableInspections", List.of(
                "audit_code_quality",
                "analyze_dependencies",
                "check_security_vulnerabilities",
                "review_performance_metrics",
                "validate_architecture_patterns",
                "examine_test_coverage",
                "assess_documentation_completeness"
            ),
            "suggestions", generateInspectorSuggestions(query)
        );
    }
    
    private List<String> generateInspectorSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        
        if (query.toLowerCase().contains("security")) {
            suggestions.add("Run security vulnerability scan on campaign targeting algorithms");
            suggestions.add("Review API key handling and authentication mechanisms");
            suggestions.add("Audit user data privacy compliance in audience targeting");
        }
        
        if (query.toLowerCase().contains("performance")) {
            suggestions.add("Analyze database query performance for large audience datasets");
            suggestions.add("Review caching strategies for campaign content recommendations");
            suggestions.add("Profile memory usage during batch audience processing");
        }
        
        if (query.toLowerCase().contains("code")) {
            suggestions.add("Review code quality metrics and technical debt");
            suggestions.add("Analyze test coverage for campaign recommendation algorithms");
            suggestions.add("Examine error handling and logging patterns");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Perform comprehensive code quality audit");
            suggestions.add("Review system architecture and design patterns");
            suggestions.add("Analyze integration points with external services");
        }
        
        return suggestions;
    }
}
