package com.insurance.mcp.model.campaign;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

/**
 * Campaign content recommendation model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignContent {
    
    @NotBlank
    private String contentId;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String description;
    
    @NotBlank
    private String contentType; // email, sms, push, social_media, etc.
    
    private String subject; // for email campaigns
    
    @NotBlank
    private String message;
    
    private String callToAction;
    private String ctaButtonText;
    private String ctaUrl;
    
    // Visual elements
    private String imageUrl;
    private String videoUrl;
    private String logoUrl;
    
    // Personalization
    private Map<String, String> personalizationTokens;
    private List<String> recommendedSegments;
    
    // Campaign settings
    private String channel;
    private String frequency;
    private String timing;
    private Integer priority;
    
    // Insurance specific
    private String insuranceType;
    private String productCategory;
    private String benefitHighlight;
    private String riskCoverage;
    
    // Compliance and legal
    private String disclaimer;
    private String termsAndConditions;
    private Boolean requiresOptIn;
}
