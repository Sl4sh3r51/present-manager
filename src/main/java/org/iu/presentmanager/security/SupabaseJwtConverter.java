package org.iu.presentmanager.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SupabaseJwtConverter implements Converter<Jwt, JwtAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        // "sub"-Claim aus dem Supabase-JWT als UUID extrahieren
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);

        // UUID direkt als Principal setzen
        return new JwtAuthenticationToken(jwt, List.of(), userId.toString()) {
            @Override
            public Object getPrincipal() {
                return userId;
            }
        };
    }
}