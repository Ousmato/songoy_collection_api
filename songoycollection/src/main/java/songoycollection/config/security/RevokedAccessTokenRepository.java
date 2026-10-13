package songoycollection.config.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, Long> {

    boolean existsByTokenId(String tokenId);

    long deleteByExpiresAtBefore(Instant instant);
}
