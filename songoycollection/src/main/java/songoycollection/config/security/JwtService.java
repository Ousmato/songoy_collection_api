package songoycollection.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
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

    public String extractAccessCode(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractClaims(token);
        return issuer.equals(claims.getIssuer())
                && userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date());
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
}
