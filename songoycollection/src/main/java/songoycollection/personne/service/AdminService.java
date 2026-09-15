package songoycollection.personne.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import songoycollection.config.HttpCustumMessage;
import songoycollection.personne.dtos.PersonnelDto;
import songoycollection.personne.dtos.LoginRequestDto;
import songoycollection.personne.dtos.LoginResponseDto;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.RepositoryService;
import songoycollection.shared.EnumShared;

import java.util.Set;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AdminService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RepositoryService repo;
    private final PersonnelUtiliShared personnelUtiliShared;

    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

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

        String accessCode;
        do {
            accessCode = "SON-" + String.format("%06d", RANDOM.nextInt(1_000_000));
        } while (repo.getPersonnelRepository().existsByAccessCode(accessCode));
        personnel.setAccessCode(accessCode);
        personnel.setPassword(passwordEncoder.encode(accessCode));
        personnel.setActive(true);
        Set<ConstraintViolation<Personnel>> violations = validator.validate(personnel);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        repo.getPersonnelRepository().save(personnel);
        return HttpCustumMessage.addMessage();
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

        return LoginResponseDto.fromPersonnel(personnel);
    }


}
