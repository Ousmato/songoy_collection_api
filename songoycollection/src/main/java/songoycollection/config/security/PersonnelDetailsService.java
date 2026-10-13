package songoycollection.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import songoycollection.personne.repository.PersonnelRepository;

/**
 * Charge exclusivement les comptes actifs à partir de leur code d'accès.
 */
@Service
@RequiredArgsConstructor
public class PersonnelDetailsService implements UserDetailsService {

    private final PersonnelRepository personnelRepository;

    @Override
    public UserDetails loadUserByUsername(String accessCode) throws UsernameNotFoundException {
        String normalizedAccessCode = accessCode == null ? "" : accessCode.trim();

        return personnelRepository.findByAccessCodeAndActiveTrue(normalizedAccessCode)
                .map(PersonnelPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Code d'accès ou mot de passe incorrect."
                ));
    }
}
