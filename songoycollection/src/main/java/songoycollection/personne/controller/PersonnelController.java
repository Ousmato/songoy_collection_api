package songoycollection.personne.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;
import java.util.List;

import songoycollection.achat.dtos.FournisseurDto;
import songoycollection.config.security.PersonnelPrincipal;
import songoycollection.personne.dtos.*;
import songoycollection.shared.ServiceShared;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class PersonnelController {

    private final ServiceShared serviceShared;

    @PostMapping("login")
    @Operation(summary = "Se connecter à la base de données")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto) {
        return serviceShared.getAdminService().login(loginRequestDto);
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
}
