package com.insurance.mcp.controller;

import com.insurance.mcp.model.campaign.AudienceCriteria;
import com.insurance.mcp.model.campaign.CampaignContent;
import com.insurance.mcp.service.campaign.AudienceTargetingService;
import com.insurance.mcp.service.campaign.CampaignContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for campaign management endpoints
 */
@Slf4j
@RestController
@RequestMapping("/campaigns")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "mcp.server.websocket.enabled", havingValue = "true", matchIfMissing = false)
public class CampaignController {

    @Autowired
    private AudienceTargetingService audienceTargetingService;

    @Autowired
    private CampaignContentService campaignContentService;

    @PostMapping("/audience/recommend")
    public ResponseEntity<Map<String, Object>> recommendAudienceCriteria(
            @RequestBody Map<String, Object> request) {
        try {
            log.info("Received audience recommendation request: {}", request);
            Map<String, Object> result = audienceTargetingService.recommendAudienceCriteria(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in audience recommendation", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate audience recommendations: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/content/recommend")
    public ResponseEntity<Map<String, Object>> recommendCampaignContent(
            @RequestBody Map<String, Object> request) {
        try {
            log.info("Received content recommendation request: {}", request);
            Map<String, Object> result = campaignContentService.recommendCampaignContent(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in content recommendation", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate content recommendations: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "MCP Campaign Server");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> listTools() {
        Map<String, Object> tools = new HashMap<>();
        tools.put("tools", new Object[]{
            Map.of(
                "name", "audience_targeting",
                "description", "Provides audience targeting criteria recommendations for insurance marketing campaigns",
                "input_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "requirements", Map.of("type", "string", "description", "User requirements for audience targeting"),
                        "insurance_type", Map.of("type", "string", "description", "Type of insurance product"),
                        "campaign_objective", Map.of("type", "string", "description", "Campaign objective")
                    ),
                    "required", new String[]{"requirements"}
                )
            ),
            Map.of(
                "name", "campaign_content_recommendation",
                "description", "Recommends personalized campaign content for insurance marketing",
                "input_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "audience_criteria", Map.of("type", "object", "description", "Target audience criteria"),
                        "insurance_type", Map.of("type", "string", "description", "Type of insurance product"),
                        "channel", Map.of("type", "string", "description", "Marketing channel"),
                        "campaign_goal", Map.of("type", "string", "description", "Campaign goal")
                    ),
                    "required", new String[]{"audience_criteria", "insurance_type"}
                )
            ),
            Map.of(
                "name", "inspector_query",
                "description", "Executes queries for Claude Inspector tool integration",
                "input_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "query", Map.of("type", "string", "description", "Query for Claude Inspector tool")
                    ),
                    "required", new String[]{"query"}
                )
            )
        });
        
        return ResponseEntity.ok(tools);
    }
}
