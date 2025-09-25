package com.insurance.mcp.service.campaign;

import com.insurance.mcp.model.campaign.CampaignContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for campaign content recommendations in insurance marketing
 */
@Slf4j
@Service
public class CampaignContentService {

    private static final Map<String, List<String>> CONTENT_TEMPLATES = Map.of(
        "life_insurance", Arrays.asList(
            "Protect Your Family's Future",
            "Life Insurance Made Simple",
            "Secure Tomorrow, Today"
        ),
        "health_insurance", Arrays.asList(
            "Your Health, Our Priority",
            "Comprehensive Coverage for You",
            "Stay Healthy, Stay Protected"
        ),
        "auto_insurance", Arrays.asList(
            "Drive with Confidence",
            "Complete Auto Protection",
            "Your Road to Peace of Mind"
        ),
        "property_insurance", Arrays.asList(
            "Protect What Matters Most",
            "Home Sweet Home Protection",
            "Secure Your Investments"
        )
    );

    private static final Map<String, List<String>> BENEFIT_HIGHLIGHTS = Map.of(
        "life_insurance", Arrays.asList(
            "Financial security for your family",
            "Tax-free death benefit",
            "Cash value accumulation",
            "Flexible payment options"
        ),
        "health_insurance", Arrays.asList(
            "Comprehensive medical coverage",
            "Preventive care included",
            "Network of top doctors",
            "Emergency coverage worldwide"
        ),
        "auto_insurance", Arrays.asList(
            "24/7 roadside assistance",
            "Accident forgiveness",
            "New car replacement",
            "Comprehensive coverage"
        ),
        "property_insurance", Arrays.asList(
            "Full replacement cost coverage",
            "Natural disaster protection",
            "Personal property coverage",
            "Liability protection"
        )
    );

    public Map<String, Object> recommendCampaignContent(Map<String, Object> request) {
        Map<String, Object> audienceCriteria = (Map<String, Object>) request.get("audience_criteria");
        String insuranceType = (String) request.get("insurance_type");
        String channel = (String) request.getOrDefault("channel", "email");
        String campaignGoal = (String) request.getOrDefault("campaign_goal", "awareness");

        log.info("Generating campaign content for insurance: {}, channel: {}, goal: {}", 
                insuranceType, channel, campaignGoal);

        List<CampaignContent> contentList = generateContentRecommendations(audienceCriteria, insuranceType, channel, campaignGoal);
        
        return Map.of(
            "recommended_content", contentList,
            "content_strategy", generateContentStrategy(insuranceType, channel, campaignGoal),
            "personalization_tips", generatePersonalizationTips(audienceCriteria),
            "optimization_suggestions", generateOptimizationSuggestions(channel, campaignGoal),
            "compliance_notes", generateComplianceNotes(insuranceType),
            "performance_expectations", generatePerformanceExpectations(channel, campaignGoal)
        );
    }

    private List<CampaignContent> generateContentRecommendations(Map<String, Object> audienceCriteria, 
                                                               String insuranceType, String channel, String campaignGoal) {
        List<CampaignContent> contentList = new ArrayList<>();
        
        // Generate multiple content variations
        contentList.add(createMainContent(audienceCriteria, insuranceType, channel, campaignGoal));
        contentList.add(createSecondaryContent(audienceCriteria, insuranceType, channel, campaignGoal));
        contentList.add(createFollowUpContent(audienceCriteria, insuranceType, channel, campaignGoal));

        return contentList;
    }

    private CampaignContent createMainContent(Map<String, Object> audienceCriteria, String insuranceType, 
                                            String channel, String campaignGoal) {
        List<String> titles = CONTENT_TEMPLATES.getOrDefault(insuranceType, Arrays.asList("Insurance Protection"));
        List<String> benefits = BENEFIT_HIGHLIGHTS.getOrDefault(insuranceType, Arrays.asList("Comprehensive coverage"));
        
        String title = titles.get(0);
        String benefit = benefits.get(0);
        
        return CampaignContent.builder()
                .contentId("main_" + insuranceType + "_" + channel)
                .title(title)
                .description(generateDescription(insuranceType, audienceCriteria))
                .contentType(channel)
                .subject(title)
                .message(generateMessage(insuranceType, benefit, audienceCriteria, campaignGoal))
                .callToAction("Get Quote Now")
                .ctaButtonText("Start Protection")
                .ctaUrl("https://insurance.com/get-quote")
                .channel(channel)
                .insuranceType(insuranceType)
                .productCategory(insuranceType)
                .benefitHighlight(benefit)
                .personalizationTokens(generatePersonalizationTokens(audienceCriteria))
                .recommendedSegments(getRecommendedSegments(audienceCriteria))
                .disclaimer("Terms and conditions apply. Coverage subject to underwriting approval.")
                .requiresOptIn(true)
                .build();
    }

    private CampaignContent createSecondaryContent(Map<String, Object> audienceCriteria, String insuranceType, 
                                                 String channel, String campaignGoal) {
        List<String> titles = CONTENT_TEMPLATES.getOrDefault(insuranceType, Arrays.asList("Insurance Protection"));
        List<String> benefits = BENEFIT_HIGHLIGHTS.getOrDefault(insuranceType, Arrays.asList("Comprehensive coverage"));
        
        String title = titles.size() > 1 ? titles.get(1) : titles.get(0);
        String benefit = benefits.size() > 1 ? benefits.get(1) : benefits.get(0);
        
        return CampaignContent.builder()
                .contentId("secondary_" + insuranceType + "_" + channel)
                .title(title)
                .description(generateDescription(insuranceType, audienceCriteria))
                .contentType(channel)
                .subject(title)
                .message(generateMessage(insuranceType, benefit, audienceCriteria, campaignGoal))
                .callToAction("Learn More")
                .ctaButtonText("Explore Options")
                .ctaUrl("https://insurance.com/learn-more")
                .channel(channel)
                .insuranceType(insuranceType)
                .productCategory(insuranceType)
                .benefitHighlight(benefit)
                .personalizationTokens(generatePersonalizationTokens(audienceCriteria))
                .recommendedSegments(getRecommendedSegments(audienceCriteria))
                .disclaimer("Terms and conditions apply. Coverage subject to underwriting approval.")
                .requiresOptIn(true)
                .priority(2)
                .build();
    }

    private CampaignContent createFollowUpContent(Map<String, Object> audienceCriteria, String insuranceType, 
                                                String channel, String campaignGoal) {
        return CampaignContent.builder()
                .contentId("followup_" + insuranceType + "_" + channel)
                .title("Don't Miss Out on Protection")
                .description("Follow-up message to re-engage prospects")
                .contentType(channel)
                .subject("Last Chance: Secure Your Protection")
                .message("You showed interest in " + insuranceType + " protection. Don't wait - secure your coverage today and protect what matters most.")
                .callToAction("Get Protected Now")
                .ctaButtonText("Secure Coverage")
                .ctaUrl("https://insurance.com/get-quote")
                .channel(channel)
                .insuranceType(insuranceType)
                .productCategory(insuranceType)
                .personalizationTokens(generatePersonalizationTokens(audienceCriteria))
                .recommendedSegments(getRecommendedSegments(audienceCriteria))
                .disclaimer("Terms and conditions apply. Coverage subject to underwriting approval.")
                .requiresOptIn(true)
                .priority(3)
                .frequency("weekly")
                .build();
    }

    private String generateDescription(String insuranceType, Map<String, Object> audienceCriteria) {
        return String.format("Comprehensive %s coverage tailored for your needs. Get personalized protection that fits your lifestyle and budget.", 
                insuranceType.replace("_", " "));
    }

    private String generateMessage(String insuranceType, String benefit, Map<String, Object> audienceCriteria, String campaignGoal) {
        StringBuilder message = new StringBuilder();
        
        message.append(String.format("Protect yourself and your loved ones with our comprehensive %s coverage. ", 
                insuranceType.replace("_", " ")));
        
        message.append(String.format("Key benefits include %s. ", benefit));
        
        if (campaignGoal.equals("conversion")) {
            message.append("Limited time offer - get your personalized quote today and save up to 20% on your premium. ");
        } else {
            message.append("Learn more about how we can help protect what matters most to you. ");
        }
        
        message.append("Our experienced team is ready to help you find the perfect coverage for your needs.");
        
        return message.toString();
    }

    private Map<String, String> generatePersonalizationTokens(Map<String, Object> audienceCriteria) {
        Map<String, String> tokens = new HashMap<>();
        
        if (audienceCriteria != null) {
            Object minAge = audienceCriteria.get("minAge");
            Object maxAge = audienceCriteria.get("maxAge");
            
            if (minAge != null && maxAge != null) {
                tokens.put("AGE_RANGE", minAge + "-" + maxAge);
            }
            
            Object incomeRange = audienceCriteria.get("incomeRange");
            if (incomeRange instanceof List && !((List<?>) incomeRange).isEmpty()) {
                tokens.put("INCOME_LEVEL", ((List<?>) incomeRange).get(0).toString());
            }
            
            Object location = audienceCriteria.get("location");
            if (location instanceof List && !((List<?>) location).isEmpty()) {
                tokens.put("LOCATION", ((List<?>) location).get(0).toString());
            }
        }
        
        return tokens;
    }

    private List<String> getRecommendedSegments(Map<String, Object> audienceCriteria) {
        List<String> segments = new ArrayList<>();
        
        if (audienceCriteria != null) {
            Object minAge = audienceCriteria.get("minAge");
            if (minAge instanceof Number) {
                int age = ((Number) minAge).intValue();
                if (age < 30) {
                    segments.add("young_adults");
                } else if (age < 50) {
                    segments.add("middle_aged");
                } else {
                    segments.add("seniors");
                }
            }
        }
        
        return segments.isEmpty() ? Arrays.asList("general") : segments;
    }

    private Map<String, Object> generateContentStrategy(String insuranceType, String channel, String campaignGoal) {
        Map<String, Object> strategy = new HashMap<>();
        
        strategy.put("primary_focus", "Value proposition and benefits");
        strategy.put("tone", "Professional yet approachable");
        strategy.put("key_messages", Arrays.asList(
            "Protection and security",
            "Financial peace of mind",
            "Personalized coverage options"
        ));
        strategy.put("content_pillars", Arrays.asList(
            "Education and awareness",
            "Risk mitigation",
            "Financial planning"
        ));
        
        return strategy;
    }

    private List<String> generatePersonalizationTips(Map<String, Object> audienceCriteria) {
        List<String> tips = new ArrayList<>();
        
        tips.add("Use demographic data for age-appropriate messaging");
        tips.add("Leverage income information for premium positioning");
        tips.add("Include location-specific benefits and coverage");
        tips.add("Personalize call-to-action based on engagement level");
        
        return tips;
    }

    private List<String> generateOptimizationSuggestions(String channel, String campaignGoal) {
        List<String> suggestions = new ArrayList<>();
        
        suggestions.add("A/B test subject lines for email campaigns");
        suggestions.add("Optimize send times based on audience behavior");
        suggestions.add("Use dynamic content based on user preferences");
        suggestions.add("Implement progressive profiling for lead nurturing");
        
        return suggestions;
    }

    private List<String> generateComplianceNotes(String insuranceType) {
        List<String> notes = new ArrayList<>();
        
        notes.add("Ensure all claims are substantiated and comply with insurance regulations");
        notes.add("Include required disclaimers and terms of service");
        notes.add("Obtain proper consent for data processing and marketing communications");
        notes.add("Follow GDPR and local privacy regulations");
        
        return notes;
    }

    private Map<String, Object> generatePerformanceExpectations(String channel, String campaignGoal) {
        Map<String, Object> expectations = new HashMap<>();
        
        expectations.put("expected_open_rate", "15-25%");
        expectations.put("expected_click_rate", "2-5%");
        expectations.put("expected_conversion_rate", "1-3%");
        expectations.put("optimization_timeline", "2-4 weeks");
        
        return expectations;
    }
}
