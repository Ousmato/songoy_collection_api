package songoycollection.atelier.contoller;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import songoycollection.atelier.dtos.CommandeConfectionRequestDto;
import songoycollection.atelier.dtos.CommandeDetailDto;
import songoycollection.atelier.dtos.AtelierMenuStatsDto;
import songoycollection.atelier.dtos.CommandeEnCoursDto;
import songoycollection.atelier.dtos.OuvrageEtatUpdateRequestDto;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;
import songoycollection.config.HttpCustumMessage;
import songoycollection.personne.dtos.ClientResponseDto;
import songoycollection.personne.dtos.SimplePersonnelResponseDto;
import songoycollection.shared.ServiceShared;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/")
public class AtelierController {

    private final ServiceShared serviceShared;

    @GetMapping("atelier/commandes/{idPersonnel}")
    @Operation(summary = "Lister les commandes selon les états des ouvrages")
    public List<CommandeEnCoursDto> getCommandesByEtats(
            @PathVariable Long idPersonnel,
            @RequestParam List<EtatOuvrageConfection> etats
    ) {
        return serviceShared.getCommandeConfectionService()
                .getCommandesByEtats(idPersonnel, etats);
    }

    @GetMapping("atelier/commandes/detail/{idCommande}/{idPersonnel}")
    @Operation(summary = "Obtenir le détail complet d'une commande de confection")
    public CommandeDetailDto getCommandeDetail(
            @PathVariable Long idCommande,
            @PathVariable Long idPersonnel,
            @RequestParam(required = false) List<EtatOuvrageConfection> etats
    ) {
        return serviceShared.getCommandeConfectionService()
                .getCommandeDetail(idCommande, idPersonnel, etats);
    }

    @GetMapping("atelier/commandes/statistiques-menu/{idPersonnel}")
    @Operation(summary = "Obtenir les indicateurs affichés dans le menu des commandes atelier")
    public AtelierMenuStatsDto getMenuStats(@PathVariable Long idPersonnel) {
        return serviceShared.getCommandeConfectionService().getMenuStats(idPersonnel);
    }

    @GetMapping("atelier/commandes/clients/{idPersonnel}")
    @Operation(summary = "Lister les clients utilisables pour une commande de confection")
    public List<ClientResponseDto> getClientsForCommande(@PathVariable Long idPersonnel) {
        return serviceShared.getCommandeConfectionService().getClientsForCommande(idPersonnel);
    }

    @GetMapping("atelier/commandes/couturiers/{idPersonnel}")
    @Operation(summary = "Lister les couturiers de l'atelier pour une commande de confection")
    public List<SimplePersonnelResponseDto> getCouturiersForCommande(@PathVariable Long idPersonnel) {
        return serviceShared.getCommandeConfectionService().getCouturiersForCommande(idPersonnel);
    }

    @PostMapping(
            value = "atelier/commandes/{idPersonnel}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Ajouter une commande de confection")
    public HttpCustumMessage addCommandeConfection(
            @PathVariable Long idPersonnel,
            @Valid @RequestPart("request") CommandeConfectionRequestDto request,
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> images
    ) {
        return serviceShared.getCommandeConfectionService()
                .addCommande(request, images, idPersonnel);
    }

    @PutMapping("atelier/ouvrages/{idOuvrage}/etat/{idPersonnel}")
    @Operation(summary = "Mettre un ouvrage à l'état prêt ou livré, avec règlement facultatif à la livraison")
    public HttpCustumMessage updateOuvrageEtat(
            @PathVariable Long idOuvrage,
            @PathVariable Long idPersonnel,
            @Valid @RequestBody OuvrageEtatUpdateRequestDto request
    ) {
        return serviceShared.getCommandeConfectionService()
                .updateOuvrageEtat(idOuvrage, request, idPersonnel);
    }

}
