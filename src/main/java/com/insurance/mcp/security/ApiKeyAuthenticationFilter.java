/*
package com.insurance.mcp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

*/
/**
 * API Key Authentication Filter for MCP Streamable HTTP transport
 * Validates API keys from X-API-Key header or Authorization header
 *//*

@Slf4j
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    // In production, store API keys securely (database, environment variables, etc.)
    private static final List<String> VALID_API_KEYS = List.of(
            "mcp-campaign-api-key-12345",
            "mcp-inspector-api-key-67890",
            "mcp-development-key-abcdef"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = extractApiKey(request);
        
        if (apiKey != null && isValidApiKey(apiKey)) {
            Authentication authentication = createAuthentication(apiKey);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("API key authentication successful for key: {}", maskApiKey(apiKey));
        }

        filterChain.doFilter(request, response);
    }

    */
/**
     * Extract API key from request headers
     *//*

    private String extractApiKey(HttpServletRequest request) {
        // Check X-API-Key header first
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }

        // Check Authorization header for API key format
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("ApiKey ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    */
/**
     * Validate API key
     *//*

    private boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        // In production, implement proper API key validation
        // This could include database lookup, expiration check, etc.
        return VALID_API_KEYS.contains(apiKey);
    }

    */
/**
     * Create authentication object for valid API key
     *//*

    private Authentication createAuthentication(String apiKey) {
        return new UsernamePasswordAuthenticationToken(
                "api-key-user",
                apiKey,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MCP_CLIENT"))
        );
    }

    */
/**
     * Mask API key for logging
     *//*

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
*/
