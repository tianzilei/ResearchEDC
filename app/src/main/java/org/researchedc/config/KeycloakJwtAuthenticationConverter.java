package org.researchedc.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> scopes = scopesConverter.convert(jwt);
        List<GrantedAuthority> realmRoles = extractRealmRoles(jwt);
        List<GrantedAuthority> clientRoles = extractClientRoles(jwt);

        List<GrantedAuthority> all = new ArrayList<>();
        if (scopes != null) all.addAll(scopes);
        all.addAll(realmRoles);
        all.addAll(clientRoles);

        return new JwtAuthenticationToken(jwt, all);
    }

    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) return Collections.emptyList();
        List<String> roles = (List<String>) realmAccess.getOrDefault("roles", Collections.emptyList());
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null) return Collections.emptyList();
        return resourceAccess.values().stream()
                .filter(obj -> obj instanceof Map)
                .map(obj -> (Map<String, Object>) obj)
                .flatMap(m -> ((List<String>) m.getOrDefault("roles", Collections.emptyList())).stream())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
