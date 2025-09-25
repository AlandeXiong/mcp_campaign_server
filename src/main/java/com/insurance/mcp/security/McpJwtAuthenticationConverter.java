/*
package com.insurance.mcp.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

*/
/**
 * JWT Authentication Converter for MCP Streamable HTTP transport
 * Converts JWT tokens to MCP-specific authentication
 *//*

@Component
public class McpJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    */
/**
     * Extract authorities from JWT token
     *//*

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract roles from JWT token
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            roles = Collections.singletonList("ROLE_MCP_CLIENT");
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}
*/
