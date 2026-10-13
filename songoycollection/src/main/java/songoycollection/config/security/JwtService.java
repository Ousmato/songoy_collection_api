package songoycollection.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Crée et vérifie les jetons d'accès signés de Songoy Collection.
 */
@Service
public class JwtService {

    private static final int MINIMUM_HS256_KEY_LENGTH_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationTime;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long expirationTime,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        byte[] keyBytes = decodeAndValidateKey(secretKey);
        if (expirationTime <= 0) {
            throw new IllegalStateException("La durée de validité JWT doit être positive.");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("L'émetteur JWT est obligatoire.");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = expirationTime;
        this.issuer = issuer;
    }

    public String generateToken(PersonnelPrincipal personnel) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationTime);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(personnel.getUsername())
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("uid", personnel.getId())
                .claim("role", personnel.getRole() == null ? null : personnel.getRole().name())
                .signWith(signingKey)
                .compact();
    }

    public JwtTokenDetails getTokenDetails(String token) {
        Claims claims = extractClaims(token);
        String tokenId = claims.getId();
        String accessCode = claims.getSubject();
        Date expiration = claims.getExpiration();
        Object personnelIdClaim = claims.get("uid");

        if (tokenId == null || tokenId.isBlank()
                || accessCode == null || accessCode.isBlank()
                || expiration == null
                || !(personnelIdClaim instanceof Number personnelId)) {
            throw new IllegalArgumentException("Le jeton d'accÃ¨s est incomplet.");
        }

        return new JwtTokenDetails(
                tokenId,
                accessCode,
                personnelId.longValue(),
                expiration.toInstant(),
                claims.getIssuer()
        );
    }

    public boolean isTokenValid(JwtTokenDetails tokenDetails, UserDetails userDetails) {
        return issuer.equals(tokenDetails.issuer())
                && userDetails.getUsername().equals(tokenDetails.accessCode())
                && tokenDetails.expiresAt().isAfter(Instant.now());
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] decodeAndValidateKey(String secretKey) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            if (keyBytes.length < MINIMUM_HS256_KEY_LENGTH_BYTES) {
                throw new IllegalStateException(
                        "JWT_SECRET_KEY doit contenir au moins 32 octets une fois décodée en Base64."
                );
            }
            return keyBytes;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET_KEY doit être une valeur Base64 valide.", exception);
        }
    }

    public record JwtTokenDetails(
            String tokenId,
            String accessCode,
            Long personnelId,
            Instant expiresAt,
            String issuer
    ) {
    }
}
