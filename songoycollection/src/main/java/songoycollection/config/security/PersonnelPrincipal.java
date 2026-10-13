package songoycollection.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared.PersonnelRole;

import java.util.Collection;
import java.util.List;

/**
 * Représentation du personnel authentifié utilisée exclusivement par Spring Security.
 */
public final class PersonnelPrincipal implements UserDetails {

    @Getter
    private final Long id;
    private final String accessCode;
    private final String passwordHash;
    private final boolean active;
    @Getter
    private final PersonnelRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public PersonnelPrincipal(Personnel personnel) {
        this.id = personnel.getId();
        this.accessCode = personnel.getAccessCode();
        this.passwordHash = personnel.getPassword();
        this.active = personnel.isActive();
        this.role = personnel.getRole();
        this.authorities = role == null
                ? List.of()
                : List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return accessCode;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
