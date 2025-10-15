package ec.com.ecommerce.core.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKMatcher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller to expose JWKS (JSON Web Key Set) endpoint
 */
@RestController
public class JwksController {

    private final JWKSource<SecurityContext> jwkSource;

    public JwksController(JWKSource<SecurityContext> jwkSource) {
        this.jwkSource = jwkSource;
    }

    /**
     * Expose JWKS at the standard .well-known location
     *
     * @return JWKS as a JSON map
     */
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getJwks() {
        try {
            // Use JWKSelector with an empty matcher to get all keys
            JWKSelector jwkSelector = new JWKSelector(new JWKMatcher.Builder().build());
            JWKSet jwkSet = new JWKSet(jwkSource.get(jwkSelector, null));
            return jwkSet.toJSONObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve JWKS", e);
        }
    }
}
