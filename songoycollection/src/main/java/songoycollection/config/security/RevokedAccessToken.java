package songoycollection.config.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Identifiant d'un JWT rendu inutilisable avant son expiration naturelle.
 * Le JWT complet n'est volontairement jamais persisté.
 */
@Entity
@Table(
        name = "revoked_access_tokens",
        indexes = {
                @Index(name = "idx_revoked_access_token_expires_at", columnList = "expires_at")
        }
)
@Getter
@NoArgsConstructor
public class RevokedAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_id", nullable = false, unique = true, length = 36)
    private String tokenId;

    @Column(name = "personnel_id", nullable = false)
    private Long personnelId;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public RevokedAccessToken(String tokenId, Long personnelId, Instant expiresAt) {
        this.tokenId = tokenId;
        this.personnelId = personnelId;
        this.expiresAt = expiresAt;
        this.revokedAt = Instant.now();
    }
}
