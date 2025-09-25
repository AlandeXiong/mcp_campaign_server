package com.insurance.mcp.model.campaign;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Audience targeting criteria for insurance marketing campaigns
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AudienceCriteria {
    
    @Min(18)
    @Max(100)
    private Integer minAge;
    
    @Min(18)
    @Max(100)
    private Integer maxAge;
    
    private List<String> gender;
    private List<String> incomeRange;
    private List<String> occupation;
    private List<String> location;
    private List<String> maritalStatus;
    private List<String> educationLevel;
    
    // Insurance specific criteria
    private List<String> existingInsuranceTypes;
    private List<String> riskProfile;
    private Boolean hasExistingPolicy;
    private List<String> preferredCommunicationChannel;
    
    // Behavioral criteria
    private List<String> purchaseHistory;
    private List<String> onlineBehavior;
    private List<String> interests;
    
    // Campaign specific
    private String campaignObjective;
    private String targetSegment;
}
