package songoycollection.personne.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;
import java.util.List;

import songoycollection.achat.dtos.FournisseurDto;
import songoycollection.personne.dtos.*;
import songoycollection.personne.service.AdminService;
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

    @PostMapping("add-personnel/{idAdmin}")
    @Operation(summary = "Ajouter un personnel")
    public Object addPersonnel(
            @PathVariable("idAdmin") Long idAdmin, @RequestBody PersonnelDto personnelDto) {
        return serviceShared.getAdminService().addPersonnel(personnelDto, idAdmin);
    }

    @GetMapping("load-personnel/{idAdmin}")
    @Operation(summary = "Lister le personnel selon le rôle et l'entité du compte connecté")
    public List<SimplePersonnelResponseDto> loadPersonnel(@PathVariable Long idAdmin)
            throws AccessDeniedException {
        return serviceShared.getAdminService().loadPersonnel(idAdmin);
    }

    @GetMapping("personnel-menu/statistiques/{idAdmin}")
    @Operation(summary = "Récupérer les compteurs du menu personnel")
    public PersonnelMenuStatsDto getPersonnelMenuStats(@PathVariable Long idAdmin)
            throws AccessDeniedException {
        return serviceShared.getAdminService().getPersonnelMenuStats(idAdmin);
    }

    @GetMapping("load-clients/{idAdmin}")
    @Operation(summary = "Lister les clients actifs, toutes entités confondues")
    public List<ClientResponseDto> loadClients(@PathVariable Long idAdmin)
            throws AccessDeniedException {
        return serviceShared.getAdminService().loadClients(idAdmin);
    }

    @PostMapping("add-fournisseur/{idAdmin}")
    @Operation(summary = "Ajouter un fournisseur")
    public Object addFournisseur(@PathVariable Long idAdmin, @RequestBody FournisseurRequestDto requestDto) {
        return serviceShared.getFournisseurService().addFournisseur(idAdmin, requestDto);

    }

    @GetMapping("fournisseurs/{idAdmin}")
    @Operation(summary = "Lister les fournisseurs")
    public List<FournisseurDto> getFournisseurs(@PathVariable Long idAdmin) {
        return serviceShared.getFournisseurService().loadFournisseurs(idAdmin);
    }
}
