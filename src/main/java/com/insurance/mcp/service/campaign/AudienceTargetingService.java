package com.insurance.mcp.service.campaign;

import com.insurance.mcp.model.campaign.AudienceCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for audience targeting recommendations in insurance marketing campaigns
 */
@Slf4j
@Service
public class AudienceTargetingService {

    private static final Map<String, List<String>> INSURANCE_TYPE_SEGMENTS = Map.of(
        "life_insurance", Arrays.asList("young_professionals", "families", "seniors"),
        "health_insurance", Arrays.asList("young_adults", "families", "seniors", "self_employed"),
        "auto_insurance", Arrays.asList("new_drivers", "experienced_drivers", "commercial_drivers"),
        "property_insurance", Arrays.asList("homeowners", "renters", "business_owners"),
        "travel_insurance", Arrays.asList("frequent_travelers", "vacationers", "business_travelers")
    );

    private static final Map<String, AudienceCriteria> SEGMENT_TEMPLATES = Map.of(
        "young_professionals", AudienceCriteria.builder()
                .minAge(25)
                .maxAge(35)
                .incomeRange(Arrays.asList("middle_income", "high_income"))
                .occupation(Arrays.asList("professional", "manager", "executive"))
                .educationLevel(Arrays.asList("bachelor", "master", "phd"))
                .maritalStatus(Arrays.asList("single", "married"))
                .preferredCommunicationChannel(Arrays.asList("email", "mobile_app", "social_media"))
                .interests(Arrays.asList("career_development", "investment", "technology"))
                .build(),
                
        "families", AudienceCriteria.builder()
                .minAge(30)
                .maxAge(50)
                .incomeRange(Arrays.asList("middle_income", "high_income"))
                .maritalStatus(Arrays.asList("married"))
                .preferredCommunicationChannel(Arrays.asList("email", "phone", "mobile_app"))
                .interests(Arrays.asList("family_security", "education", "health"))
                .build(),
                
        "seniors", AudienceCriteria.builder()
                .minAge(55)
                .maxAge(75)
                .incomeRange(Arrays.asList("middle_income", "high_income"))
                .preferredCommunicationChannel(Arrays.asList("phone", "mail", "email"))
                .interests(Arrays.asList("retirement_planning", "health", "estate_planning"))
                .build()
    );

    public Map<String, Object> recommendAudienceCriteria(Map<String, Object> request) {
        String requirements = (String) request.get("requirements");
        String insuranceType = (String) request.getOrDefault("insurance_type", "general");
        String campaignObjective = (String) request.getOrDefault("campaign_objective", "acquisition");

        log.info("Generating audience criteria for requirements: {}, insurance: {}, objective: {}", 
                requirements, insuranceType, campaignObjective);

        // Analyze requirements and generate criteria
        List<AudienceCriteria> criteriaList = generateCriteriaFromRequirements(requirements, insuranceType, campaignObjective);
        
        // Get recommended segments
        List<String> recommendedSegments = INSURANCE_TYPE_SEGMENTS.getOrDefault(insuranceType, Arrays.asList("general"));
        
        // Generate targeting suggestions
        List<Map<String, Object>> targetingSuggestions = generateTargetingSuggestions(criteriaList, campaignObjective);

        return Map.of(
            "recommended_criteria", criteriaList,
            "target_segments", recommendedSegments,
            "targeting_suggestions", targetingSuggestions,
            "insights", generateInsights(requirements, insuranceType, campaignObjective),
            "estimated_audience_size", estimateAudienceSize(criteriaList),
            "confidence_score", calculateConfidenceScore(requirements, insuranceType)
        );
    }

    private List<AudienceCriteria> generateCriteriaFromRequirements(String requirements, String insuranceType, String campaignObjective) {
        List<AudienceCriteria> criteriaList = new ArrayList<>();
        
        // Basic criteria based on insurance type
        AudienceCriteria baseCriteria = createBaseCriteria(insuranceType, campaignObjective);
        criteriaList.add(baseCriteria);

        // Analyze requirements for additional criteria
        if (requirements != null) {
            String lowerRequirements = requirements.toLowerCase();
            
            // Age-based targeting
            if (lowerRequirements.contains("young") || lowerRequirements.contains("millennial")) {
                criteriaList.add(baseCriteria.toBuilder()
                        .minAge(25)
                        .maxAge(35)
                        .preferredCommunicationChannel(Arrays.asList("social_media", "mobile_app", "email"))
                        .build());
            }
            
            if (lowerRequirements.contains("senior") || lowerRequirements.contains("retirement")) {
                criteriaList.add(baseCriteria.toBuilder()
                        .minAge(55)
                        .maxAge(75)
                        .preferredCommunicationChannel(Arrays.asList("phone", "mail", "email"))
                        .build());
            }

            // Income-based targeting
            if (lowerRequirements.contains("affordable") || lowerRequirements.contains("budget")) {
                criteriaList.add(baseCriteria.toBuilder()
                        .incomeRange(Arrays.asList("low_income", "middle_income"))
                        .build());
            }
            
            if (lowerRequirements.contains("premium") || lowerRequirements.contains("high-end")) {
                criteriaList.add(baseCriteria.toBuilder()
                        .incomeRange(Arrays.asList("high_income", "very_high_income"))
                        .build());
            }
        }

        return criteriaList;
    }

    private AudienceCriteria createBaseCriteria(String insuranceType, String campaignObjective) {
        switch (insuranceType) {
            case "life_insurance":
                return AudienceCriteria.builder()
                        .minAge(25)
                        .maxAge(65)
                        .maritalStatus(Arrays.asList("single", "married"))
                        .riskProfile(Arrays.asList("low_risk", "medium_risk"))
                        .campaignObjective(campaignObjective)
                        .build();
                        
            case "health_insurance":
                return AudienceCriteria.builder()
                        .minAge(18)
                        .maxAge(65)
                        .riskProfile(Arrays.asList("medium_risk", "high_risk"))
                        .campaignObjective(campaignObjective)
                        .build();
                        
            case "auto_insurance":
                return AudienceCriteria.builder()
                        .minAge(18)
                        .maxAge(75)
                        .occupation(Arrays.asList("employed", "self_employed"))
                        .campaignObjective(campaignObjective)
                        .build();
                        
            default:
                return AudienceCriteria.builder()
                        .minAge(18)
                        .maxAge(65)
                        .campaignObjective(campaignObjective)
                        .build();
        }
    }

    private List<Map<String, Object>> generateTargetingSuggestions(List<AudienceCriteria> criteriaList, String campaignObjective) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        for (AudienceCriteria criteria : criteriaList) {
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("criteria", criteria);
            suggestion.put("priority", "high");
            suggestion.put("reasoning", generateReasoning(criteria, campaignObjective));
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }

    private String generateReasoning(AudienceCriteria criteria, String campaignObjective) {
        StringBuilder reasoning = new StringBuilder();
        
        if (criteria.getMinAge() != null && criteria.getMaxAge() != null) {
            reasoning.append(String.format("Age range %d-%d is optimal for %s campaigns. ", 
                    criteria.getMinAge(), criteria.getMaxAge(), campaignObjective));
        }
        
        if (criteria.getIncomeRange() != null && !criteria.getIncomeRange().isEmpty()) {
            reasoning.append(String.format("Targeting %s income segments aligns with campaign objectives. ", 
                    String.join(", ", criteria.getIncomeRange())));
        }
        
        if (criteria.getPreferredCommunicationChannel() != null && !criteria.getPreferredCommunicationChannel().isEmpty()) {
            reasoning.append(String.format("Recommended channels: %s for better engagement. ", 
                    String.join(", ", criteria.getPreferredCommunicationChannel())));
        }
        
        return reasoning.toString();
    }

    private Map<String, Object> generateInsights(String requirements, String insuranceType, String campaignObjective) {
        Map<String, Object> insights = new HashMap<>();
        
        insights.put("market_opportunity", String.format("Strong potential in %s market segment", insuranceType));
        insights.put("competitive_advantage", "Focus on personalized messaging and digital channels");
        insights.put("risk_factors", Arrays.asList("Market saturation", "Regulatory compliance"));
        insights.put("success_metrics", Arrays.asList("Click-through rate", "Conversion rate", "Customer acquisition cost"));
        
        return insights;
    }

    private String estimateAudienceSize(List<AudienceCriteria> criteriaList) {
        // Simplified estimation logic
        int baseSize = 100000;
        int criteriaCount = criteriaList.size();
        
        if (criteriaCount > 3) {
            return "Small (1,000 - 10,000)";
        } else if (criteriaCount > 1) {
            return "Medium (10,000 - 100,000)";
        } else {
            return "Large (100,000+)";
        }
    }

    private double calculateConfidenceScore(String requirements, String insuranceType) {
        double score = 0.7; // Base score
        
        if (requirements != null && requirements.length() > 50) {
            score += 0.1; // More detailed requirements
        }
        
        if (INSURANCE_TYPE_SEGMENTS.containsKey(insuranceType)) {
            score += 0.2; // Known insurance type
        }
        
        return Math.min(score, 1.0);
    }
}
