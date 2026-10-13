package songoycollection.personne.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import songoycollection.config.HttpCustumMessage;
import songoycollection.config.security.JwtService;
import songoycollection.config.security.PersonnelPrincipal;
import songoycollection.personne.dtos.PersonnelDto;
import songoycollection.personne.dtos.LoginRequestDto;
import songoycollection.personne.dtos.LoginResponseDto;
import songoycollection.personne.dtos.ClientResponseDto;
import songoycollection.personne.dtos.PersonnelMenuStatsDto;
import songoycollection.personne.dtos.SimplePersonnelResponseDto;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.RepositoryService;
import songoycollection.shared.EnumShared;

import java.util.Set;
import java.util.List;
import java.security.SecureRandom;
import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class AdminService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RepositoryService repo;
    private final PersonnelUtiliShared personnelUtiliShared;

    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Object addPersonnel(PersonnelDto requestDto, Long idAdmin) {
        Personnel creator = personnelUtiliShared.getPersonnelById(idAdmin);
        Personnel personnel = PersonnelDto.toPersonnel(requestDto);
        EnumShared.PersonnelRole requestedRole = personnel.getRole();
        EnumShared.Entite requestedEntite = personnel.getEntite();
        if (requestedRole == null) {
            throw new IllegalArgumentException("Le rôle est obligatoire");
        }
        if (creator.getRole() == EnumShared.PersonnelRole.SUPER_ADMIN) {
            if (requestedRole != EnumShared.PersonnelRole.ADMIN) {
                throw new IllegalArgumentException("Le Super Admin peut créer uniquement des Admins");
            }
            personnel.setEntite(EnumShared.Entite.GLOBAL);
        } else if (creator.getRole() == EnumShared.PersonnelRole.ADMIN) {
            if (requestedRole == EnumShared.PersonnelRole.ADMIN || requestedRole == EnumShared.PersonnelRole.SUPER_ADMIN) {
                throw new IllegalArgumentException("Un Admin ne peut pas créer un Admin ou un Super Admin");
            }
            if (requestedEntite == null || requestedEntite == EnumShared.Entite.GLOBAL) {
                throw new IllegalArgumentException("L'entité est obligatoire pour ce personnel");
            }
        } else {
            throw new IllegalArgumentException("Personnel non autorisé à créer un compte");
        }
        List<EnumShared.PersonnelRole> rolesExclus = List.of(
                EnumShared.PersonnelRole.CLIENT,
                EnumShared.PersonnelRole.APPRENANT,
                EnumShared.PersonnelRole.LIVREUR,
                EnumShared.PersonnelRole.JUNIOR,
                EnumShared.PersonnelRole.MENTOR
        );
        if (!rolesExclus.contains(requestDto.getRole())){
            String accessCode;
            do {
                accessCode = "SON-" + String.format("%06d", RANDOM.nextInt(1_000_000));
            } while (repo.getPersonnelRepository().existsByAccessCode(accessCode));
            personnel.setAccessCode(accessCode);
            personnel.setPassword(passwordEncoder.encode(accessCode));

        }

        personnel.setActive(true);
        Set<ConstraintViolation<Personnel>> violations = validator.validate(personnel);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        repo.getPersonnelRepository().save(personnel);
        return HttpCustumMessage.addMessage();
    }

    public List<SimplePersonnelResponseDto> loadPersonnel(Long idAdmin) throws AccessDeniedException {
        Personnel requester = personnelUtiliShared.getPersonnelById(idAdmin);
        List<Personnel> personnel;
        List<EnumShared.PersonnelRole> rolesExclus = List.of(
                EnumShared.PersonnelRole.CLIENT,
                EnumShared.PersonnelRole.ADMIN,
                EnumShared.PersonnelRole.SUPER_ADMIN
        );

        if (requester.getRole() == EnumShared.PersonnelRole.SUPER_ADMIN) {
            personnel = repo.getPersonnelRepository().findAllByActiveTrueAndRoleAndEntiteOrderByNomAscPrenomAsc(
                    EnumShared.PersonnelRole.ADMIN,
                    EnumShared.Entite.GLOBAL
            );
        } else if (requester.getRole() == EnumShared.PersonnelRole.ADMIN) {
            if (requester.getEntite() == EnumShared.Entite.GLOBAL) {
                personnel = repo.getPersonnelRepository()
                        .findAllByEntiteInAndActiveTrueAndRoleNotInOrderByNomAscPrenomAsc(
                                List.of(EnumShared.Entite.BOUTIQUE, EnumShared.Entite.ATELIER),
                                rolesExclus
                        );
            } else if (requester.getEntite() == EnumShared.Entite.BOUTIQUE
                    || requester.getEntite() == EnumShared.Entite.ATELIER) {
                personnel = repo.getPersonnelRepository()
                        .findAllByEntiteInAndActiveTrueAndRoleNotInOrderByNomAscPrenomAsc(
                                List.of(requester.getEntite()),
                                rolesExclus
                        );
            } else {
                throw new AccessDeniedException("L'entité de cet Admin ne permet pas de gérer le personnel.");
            }
        } else {
            throw new AccessDeniedException("Seul un Admin peut consulter la liste du personnel.");
        }

        return personnel.stream()
                .map(SimplePersonnelResponseDto::fromPersonnel)
                .toList();
    }

    public List<ClientResponseDto> loadClients(Long idAdmin) throws AccessDeniedException {
        Personnel requester = personnelUtiliShared.getPersonnelById(idAdmin);
        if (requester.getRole() != EnumShared.PersonnelRole.ADMIN
                && requester.getRole() != EnumShared.PersonnelRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Seul un Admin peut consulter la liste des clients.");
        }

        return repo.getPersonnelRepository()
                .findAllByActiveTrueAndRoleOrderByNomAscPrenomAsc(EnumShared.PersonnelRole.CLIENT)
                .stream()
                .map(ClientResponseDto::fromPersonnel)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonnelMenuStatsDto getPersonnelMenuStats(Long idAdmin) throws AccessDeniedException {
        Personnel requester = personnelUtiliShared.getPersonnelById(idAdmin);
        if (requester.getRole() != EnumShared.PersonnelRole.ADMIN
                && requester.getRole() != EnumShared.PersonnelRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Seul un Admin peut consulter les statistiques du personnel.");
        }

        List<EnumShared.PersonnelRole> rolesExclus = List.of(
                EnumShared.PersonnelRole.CLIENT,
                EnumShared.PersonnelRole.ADMIN,
                EnumShared.PersonnelRole.SUPER_ADMIN
        );

        long nombrePersonnel;
        if (requester.getRole() == EnumShared.PersonnelRole.SUPER_ADMIN) {
            nombrePersonnel = repo.getPersonnelRepository().countByActiveTrueAndRoleAndEntite(
                    EnumShared.PersonnelRole.ADMIN,
                    EnumShared.Entite.GLOBAL
            );
        } else if (requester.getEntite() == EnumShared.Entite.GLOBAL) {
            nombrePersonnel = repo.getPersonnelRepository().countByEntiteInAndActiveTrueAndRoleNotIn(
                    List.of(EnumShared.Entite.BOUTIQUE, EnumShared.Entite.ATELIER),
                    rolesExclus
            );
        } else if (requester.getEntite() == EnumShared.Entite.BOUTIQUE
                || requester.getEntite() == EnumShared.Entite.ATELIER) {
            nombrePersonnel = repo.getPersonnelRepository().countByEntiteInAndActiveTrueAndRoleNotIn(
                    List.of(requester.getEntite()),
                    rolesExclus
            );
        } else {
            throw new AccessDeniedException("L'entité de cet Admin ne permet pas de gérer le personnel.");
        }

        PersonnelMenuStatsDto dto = new PersonnelMenuStatsDto();
        dto.setNombreClients(repo.getPersonnelRepository()
                .countByActiveTrueAndRole(EnumShared.PersonnelRole.CLIENT));
        dto.setNombreFournisseurs(repo.getFourniseurRepository().count());
        dto.setNombrePersonnel(nombrePersonnel);
        return dto;
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        if (requestDto == null || requestDto.getAccessCode() == null
                || requestDto.getPassword() == null) {
            throw new IllegalArgumentException("Code d'accès et mot de passe obligatoires");
        }

        Personnel personnel = repo.getPersonnelRepository()
                .findByAccessCodeAndActiveTrue(requestDto.getAccessCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Code d'accès ou mot de passe incorrect"));

        if (!passwordEncoder.matches(requestDto.getPassword(), personnel.getPassword())) {
            throw new IllegalArgumentException("Code d'accès ou mot de passe incorrect");
        }

        PersonnelPrincipal principal = new PersonnelPrincipal(personnel);
        LoginResponseDto response = LoginResponseDto.fromPersonnel(personnel);
        response.setAccessToken(jwtService.generateToken(principal));
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtService.getExpirationTime());
        return response;
    }


}
