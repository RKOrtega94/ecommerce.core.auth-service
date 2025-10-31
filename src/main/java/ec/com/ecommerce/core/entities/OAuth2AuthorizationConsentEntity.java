package ec.com.ecommerce.core.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity for oauth2_authorization_consent table
 * This maps to the table expected by Spring Security OAuth2 Authorization Server
 */
@Entity
@Table(name = "oauth2_authorization_consent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(OAuth2AuthorizationConsentEntity.OAuth2AuthorizationConsentId.class)
public class OAuth2AuthorizationConsentEntity {

    @Id
    @Column(name = "registered_client_id", length = 100)
    private String registeredClientId;

    @Id
    @Column(name = "principal_name", length = 200)
    private String principalName;

    @Column(name = "authorities", length = 1000, nullable = false)
    private String authorities;

    /**
     * Composite primary key class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OAuth2AuthorizationConsentId implements java.io.Serializable {
        private String registeredClientId;
        private String principalName;
    }
}