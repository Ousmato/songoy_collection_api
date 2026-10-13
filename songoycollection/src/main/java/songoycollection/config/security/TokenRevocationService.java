package songoycollection.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final JwtService jwtService;
    private final RevokedAccessTokenRepository revokedAccessTokenRepository;

    public boolean isRevoked(String tokenId) {
        return revokedAccessTokenRepository.existsByTokenId(tokenId);
    }

    @Transactional
    public void revoke(String token, PersonnelPrincipal connectedPersonnel) {
        JwtService.JwtTokenDetails tokenDetails = jwtService.getTokenDetails(token);

        if (!Objects.equals(tokenDetails.personnelId(), connectedPersonnel.getId())) {
            throw new IllegalArgumentException("Le jeton ne correspond pas au compte connectÃ©.");
        }

        // Les entrÃ©es expirÃ©es ne sont plus utiles et sont supprimÃ©es lors des dÃ©connexions.
        revokedAccessTokenRepository.deleteByExpiresAtBefore(Instant.now());

        // Un double clic sur "DÃ©connexion" reste sans effet et ne produit pas d'erreur.
        if (revokedAccessTokenRepository.existsByTokenId(tokenDetails.tokenId())) {
            return;
        }

        revokedAccessTokenRepository.save(new RevokedAccessToken(
                tokenDetails.tokenId(),
                connectedPersonnel.getId(),
                tokenDetails.expiresAt()
        ));
    }
}
