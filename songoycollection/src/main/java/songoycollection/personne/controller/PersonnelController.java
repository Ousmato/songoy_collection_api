package songoycollection.personne.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;
import java.util.List;

import songoycollection.achat.dtos.FournisseurDto;
import songoycollection.config.security.PersonnelPrincipal;
import songoycollection.config.security.TokenRevocationService;
import songoycollection.personne.dtos.*;
import songoycollection.shared.ServiceShared;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class PersonnelController {

    private final ServiceShared serviceShared;
    private final TokenRevocationService tokenRevocationService;

    @PostMapping("login")
    @Operation(summary = "Se connecter à la base de données")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto) {
        return serviceShared.getAdminService().login(loginRequestDto);
    }

    @PostMapping("logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "DÃ©connecter le compte et rÃ©voquer son jeton d'accÃ¨s")
    public void logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel) {
        tokenRevocationService.revoke(extractBearerToken(authorization), connectedPersonnel);
    }

    @GetMapping("profile")
    @Operation(summary = "Récupérer le profil complet du compte connecté")
    public PersonnelProfileResponseDto getConnectedProfile(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel) {
        return serviceShared.getAdminService().getConnectedProfile(connectedPersonnel.getId());
    }

    @PostMapping("add-personnel")
    @Operation(summary = "Ajouter un personnel")
    public Object addPersonnel(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel,
            @RequestBody PersonnelDto personnelDto) {
        return serviceShared.getAdminService().addPersonnel(personnelDto, connectedPersonnel.getId());
    }

    @GetMapping("load-personnel")
    @Operation(summary = "Lister le personnel selon le rôle et l'entité du compte connecté")
    public List<SimplePersonnelResponseDto> loadPersonnel(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel)
            throws AccessDeniedException {
        return serviceShared.getAdminService().loadPersonnel(connectedPersonnel.getId());
    }

    @GetMapping("personnel-menu/statistiques")
    @Operation(summary = "Récupérer les compteurs du menu personnel")
    public PersonnelMenuStatsDto getPersonnelMenuStats(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel)
            throws AccessDeniedException {
        return serviceShared.getAdminService().getPersonnelMenuStats(connectedPersonnel.getId());
    }

    @GetMapping("load-clients")
    @Operation(summary = "Lister les clients actifs, toutes entités confondues")
    public List<ClientResponseDto> loadClients(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel)
            throws AccessDeniedException {
        return serviceShared.getAdminService().loadClients(connectedPersonnel.getId());
    }

    @PostMapping("add-fournisseur")
    @Operation(summary = "Ajouter un fournisseur")
    public Object addFournisseur(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel,
            @RequestBody FournisseurRequestDto requestDto) {
        return serviceShared.getFournisseurService().addFournisseur(connectedPersonnel.getId(), requestDto);

    }

    @GetMapping("fournisseurs")
    @Operation(summary = "Lister les fournisseurs")
    public List<FournisseurDto> getFournisseurs(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel) {
        return serviceShared.getFournisseurService().loadFournisseurs(connectedPersonnel.getId());
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Jeton d'accÃ¨s obligatoire.");
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Jeton d'accÃ¨s obligatoire.");
        }

        return token;
    }
}
