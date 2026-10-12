package songoycollection.atelier.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import songoycollection.atelier.dtos.CommandeConfectionRequestDto;
import songoycollection.atelier.dtos.CommandeDetailDto;
import songoycollection.atelier.dtos.CommandeEnCoursDto;
import songoycollection.atelier.dtos.DepotMatiereDetailDto;
import songoycollection.atelier.dtos.DepotMatiereClientRequestDto;
import songoycollection.atelier.dtos.MesureOuvrageDto;
import songoycollection.atelier.dtos.MesureOuvrageRequestDto;
import songoycollection.atelier.dtos.OuvrageDetailDto;
import songoycollection.atelier.dtos.OuvrageConfectionRequestDto;
import songoycollection.atelier.model.CommandeConfection;
import songoycollection.atelier.model.DepotMatiereClient;
import songoycollection.atelier.model.MesureOuvrage;
import songoycollection.atelier.model.OuvrageConfection;
import songoycollection.atelier.model.VersementCommande;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;
import songoycollection.atelier.model.enums.TypeVersementCommande;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HelperAtelierService {

    private final PersonnelUtiliShared personnelUtiliShared;

    public Personnel getCreator(Long idPersonnel) {
        Personnel creator = personnelUtiliShared.getPersonnelById(idPersonnel);
        boolean caissierAtelier = creator.getRole() == EnumShared.PersonnelRole.CAISSIER
                && creator.getEntite() == EnumShared.Entite.ATELIER;
        boolean couturierAtelier = creator.getRole() == EnumShared.PersonnelRole.COUTURIER
                && creator.getEntite() == EnumShared.Entite.ATELIER;

        if (!caissierAtelier && !couturierAtelier) {
            throw new IllegalArgumentException("Ce compte ne peut pas créer une commande de confection.");
        }
        return creator;
    }

    public Personnel getCommandeReader(Long idPersonnel) {
        Personnel personnel = personnelUtiliShared.getPersonnelById(idPersonnel);
        boolean admin = personnel.getRole() == EnumShared.PersonnelRole.ADMIN
                || personnel.getRole() == EnumShared.PersonnelRole.SUPER_ADMIN;
        boolean caissierAtelier = personnel.getRole() == EnumShared.PersonnelRole.CAISSIER
                && personnel.getEntite() == EnumShared.Entite.ATELIER;
        boolean couturierAtelier = personnel.getRole() == EnumShared.PersonnelRole.COUTURIER
                && personnel.getEntite() == EnumShared.Entite.ATELIER;

        if (!admin && !caissierAtelier && !couturierAtelier) {
            throw new IllegalArgumentException("Ce compte ne peut pas consulter les commandes de confection.");
        }
        return personnel;
    }

    public boolean canMarkOuvrageReady(Personnel personnel, OuvrageConfection ouvrage) {
        boolean admin = isAdmin(personnel);
        boolean caissierAtelier = isCaissierAtelier(personnel);
        boolean assignedCouturier = personnel.getRole() == EnumShared.PersonnelRole.COUTURIER
                && personnel.getEntite() == EnumShared.Entite.ATELIER
                && ouvrage.getCouturier() != null
                && personnel.getId().equals(ouvrage.getCouturier().getId());
        return admin || caissierAtelier || assignedCouturier;
    }

    public boolean canDeliverOuvrage(Personnel personnel, OuvrageConfection ouvrage) {
        boolean assignedCouturier = personnel.getRole() == EnumShared.PersonnelRole.COUTURIER
                && personnel.getEntite() == EnumShared.Entite.ATELIER
                && ouvrage.getCouturier() != null
                && personnel.getId().equals(ouvrage.getCouturier().getId());
        return isCaissierAtelier(personnel) || assignedCouturier;
    }

    private boolean isAdmin(Personnel personnel) {
        return personnel.getRole() == EnumShared.PersonnelRole.ADMIN
                || personnel.getRole() == EnumShared.PersonnelRole.SUPER_ADMIN;
    }

    private boolean isCaissierAtelier(Personnel personnel) {
        return personnel.getRole() == EnumShared.PersonnelRole.CAISSIER
                && personnel.getEntite() == EnumShared.Entite.ATELIER;
    }

    public CommandeConfection prepareCommande(
            CommandeConfectionRequestDto request,
            Personnel creator
    ) {
        CommandeConfection commande = new CommandeConfection();
        commande.setCreePar(creator);
        commande.setDateLivraisonPrevue(request.getDateLivraisonPrevue());
        commande.setObservation(trimToNull(request.getObservation()));

        if (request.getClientId() != null) {
            Personnel client = personnelUtiliShared.getPersonnelById(request.getClientId());
            if (client.getRole() != EnumShared.PersonnelRole.CLIENT) {
                throw new IllegalArgumentException("La personne sélectionnée n’est pas un client.");
            }
            commande.setClient(client);
        } else {
            commande.setClientNomPassage(request.getClientNomPassage().trim());
        }
        return commande;
    }

    public OuvrageConfection prepareOuvrage(
            CommandeConfection commande,
            OuvrageConfectionRequestDto request,
            String photoReference
    ) {
        Personnel couturier = personnelUtiliShared.getPersonnelById(request.getCouturierId());
        if (couturier.getEntite() != EnumShared.Entite.ATELIER) {
            throw new IllegalArgumentException("Le couturier sélectionné doit appartenir à l’atelier.");
        }

        OuvrageConfection ouvrage = new OuvrageConfection();
        ouvrage.setCommande(commande);
        ouvrage.setCouturier(couturier);
        ouvrage.setDesignation(request.getDesignation().trim());
        ouvrage.setTypeHabit(request.getTypeHabit().trim());
        ouvrage.setPrixConvenu(request.getPrixConvenu());
        ouvrage.setConsignes(trimToNull(request.getConsignes()));
        ouvrage.setEtat(EtatOuvrageConfection.EN_CONFECTION);
        ouvrage.setDateDebutConfection(LocalDateTime.now());
        ouvrage.setUrlPhotoReference(photoReference);
        return ouvrage;
    }

    public List<MesureOuvrage> prepareMesures(
            OuvrageConfection ouvrage,
            List<MesureOuvrageRequestDto> requests
    ) {
        return requests.stream().map(request -> {
            MesureOuvrage mesure = new MesureOuvrage();
            mesure.setOuvrage(ouvrage);
            mesure.setLibelle(request.getLibelle().trim());
            mesure.setValeur(request.getValeur());
            mesure.setUnite(request.getUnite().trim());
            return mesure;
        }).toList();
    }

    public List<DepotMatiereClient> prepareDepots(
            OuvrageConfection ouvrage,
            List<DepotMatiereClientRequestDto> requests,
            List<String> photosReference
    ) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<DepotMatiereClient> depots = new ArrayList<>();
        for (int index = 0; index < requests.size(); index++) {
            DepotMatiereClientRequestDto request = requests.get(index);
            DepotMatiereClient depot = new DepotMatiereClient();
            depot.setOuvrage(ouvrage);
            depot.setDescription(request.getDescription().trim());
            depot.setQuantiteRecue(request.getQuantite());
            depot.setUnite(request.getUnite().trim());
            depot.setUrlPhotoReference(photosReference.get(index));
            depots.add(depot);
        }
        return depots;
    }

    public VersementCommande prepareVersementInitial(
            CommandeConfectionRequestDto request,
            CommandeConfection commande,
            Personnel creator,
            BigDecimal totalCommande
    ) {
        if (request.getVersementInitial() == null) {
            return null;
        }
        if (request.getVersementInitial().getModePaiement() == EnumShared.ModePaiement.CREDIT) {
            throw new IllegalArgumentException("Un versement initial ne peut pas être enregistré au crédit.");
        }
        if (request.getVersementInitial().getMontant().compareTo(totalCommande) > 0) {
            throw new IllegalArgumentException("Le versement initial ne peut pas dépasser le total de la commande.");
        }

        VersementCommande versement = new VersementCommande();
        versement.setCommande(commande);
        versement.setType(TypeVersementCommande.VERSEMENT);
        versement.setMontant(request.getVersementInitial().getMontant());
        versement.setModePaiement(request.getVersementInitial().getModePaiement());
        versement.setDateVersement(LocalDateTime.now());
        versement.setEnregistrePar(creator);
        return versement;
    }

    public VersementCommande prepareVersementLivraison(
            CommandeConfection commande,
            BigDecimal montant,
            EnumShared.ModePaiement modePaiement,
            Personnel personnel
    ) {
        VersementCommande versement = new VersementCommande();
        versement.setCommande(commande);
        versement.setType(TypeVersementCommande.VERSEMENT);
        versement.setMontant(montant);
        versement.setModePaiement(modePaiement);
        versement.setDateVersement(LocalDateTime.now());
        versement.setEnregistrePar(personnel);
        return versement;
    }

    public CommandeEnCoursDto toCommandeEnCoursDto(
            CommandeConfection commande,
            List<OuvrageConfection> ouvrages,
            BigDecimal montantVerse
    ) {
        CommandeEnCoursDto dto = new CommandeEnCoursDto();
        populateCommandeSummary(dto, commande, ouvrages, montantVerse);
        return dto;
    }

    public CommandeDetailDto toCommandeDetailDto(
            CommandeConfection commande,
            List<OuvrageConfection> ouvrages,
            List<OuvrageConfection> ouvragesAffiches,
            Map<Long, List<MesureOuvrage>> mesuresParOuvrage,
            Map<Long, List<DepotMatiereClient>> depotsParOuvrage,
            BigDecimal montantVerse
    ) {
        CommandeDetailDto dto = new CommandeDetailDto();
        populateCommandeSummary(dto, commande, ouvrages, montantVerse);
        dto.setCreePar(getPersonnelLabel(commande.getCreePar()));
        dto.setObservation(commande.getObservation());
        dto.setOuvrages(ouvragesAffiches.stream()
                .map(ouvrage -> toOuvrageDetailDto(
                        ouvrage,
                        mesuresParOuvrage.getOrDefault(ouvrage.getId(), List.of()),
                        depotsParOuvrage.getOrDefault(ouvrage.getId(), List.of())
                ))
                .toList());
        return dto;
    }

    private void populateCommandeSummary(
            CommandeEnCoursDto dto,
            CommandeConfection commande,
            List<OuvrageConfection> ouvrages,
            BigDecimal montantVerse
    ) {
        BigDecimal montantTotal = ouvrages.stream()
                .map(OuvrageConfection::getPrixConvenu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal verse = montantVerse.max(BigDecimal.ZERO);

        dto.setId(commande.getId());
        dto.setReference("CMD-" + String.format("%05d", commande.getId()));
        dto.setClient(getClientLabel(commande));
        dto.setDateCreation(commande.getDateCreation());
        dto.setDateLivraisonPrevue(commande.getDateLivraisonPrevue());
        dto.setNombreOuvrages(ouvrages.size());
        dto.setOuvragesEnConfection(countOuvrages(ouvrages, EtatOuvrageConfection.EN_CONFECTION));
        dto.setOuvragesPrets(countOuvrages(ouvrages, EtatOuvrageConfection.PRET));
        dto.setOuvragesLivres(countOuvrages(ouvrages, EtatOuvrageConfection.LIVRE));
        dto.setDateDerniereLivraison(ouvrages.stream()
                .map(OuvrageConfection::getDateLivraison)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        dto.setMontantTotal(montantTotal);
        dto.setMontantVerse(verse);
        dto.setResteAPayer(montantTotal.subtract(verse).max(BigDecimal.ZERO));
    }

    private void populateCommandeSummary(
            CommandeDetailDto dto,
            CommandeConfection commande,
            List<OuvrageConfection> ouvrages,
            BigDecimal montantVerse
    ) {
        BigDecimal montantTotal = ouvrages.stream()
                .map(OuvrageConfection::getPrixConvenu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal verse = montantVerse.max(BigDecimal.ZERO);

        dto.setId(commande.getId());
        dto.setReference("CMD-" + String.format("%05d", commande.getId()));
        dto.setClient(getClientLabel(commande));
        dto.setDateCreation(commande.getDateCreation());
        dto.setDateLivraisonPrevue(commande.getDateLivraisonPrevue());
        dto.setNombreOuvrages(ouvrages.size());
        dto.setOuvragesEnConfection(countOuvrages(ouvrages, EtatOuvrageConfection.EN_CONFECTION));
        dto.setOuvragesPrets(countOuvrages(ouvrages, EtatOuvrageConfection.PRET));
        dto.setOuvragesLivres(countOuvrages(ouvrages, EtatOuvrageConfection.LIVRE));
        dto.setDateDerniereLivraison(ouvrages.stream()
                .map(OuvrageConfection::getDateLivraison)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        dto.setMontantTotal(montantTotal);
        dto.setMontantVerse(verse);
        dto.setResteAPayer(montantTotal.subtract(verse).max(BigDecimal.ZERO));
    }

    private OuvrageDetailDto toOuvrageDetailDto(
            OuvrageConfection ouvrage,
            List<MesureOuvrage> mesures,
            List<DepotMatiereClient> depotsMatiereClient
    ) {
        OuvrageDetailDto dto = new OuvrageDetailDto();
        dto.setId(ouvrage.getId());
        dto.setDesignation(ouvrage.getDesignation());
        dto.setTypeHabit(ouvrage.getTypeHabit());
        dto.setCouturierId(ouvrage.getCouturier() == null ? null : ouvrage.getCouturier().getId());
        dto.setCouturier(ouvrage.getCouturier() == null
                ? null
                : getPersonnelLabel(ouvrage.getCouturier()));
        dto.setUrlPhotoReference(ouvrage.getUrlPhotoReference());
        dto.setConsignes(ouvrage.getConsignes());
        dto.setPrixConvenu(ouvrage.getPrixConvenu());
        dto.setDepotsMatiereClient(depotsMatiereClient.stream()
                .map(this::toDepotMatiereDetailDto)
                .toList());
        dto.setMesures(mesures.stream().map(this::toMesureOuvrageDto).toList());
        dto.setEtat(ouvrage.getEtat());
        dto.setDateDebutConfection(ouvrage.getDateDebutConfection());
        dto.setDatePret(ouvrage.getDatePret());
        dto.setDateLivraison(ouvrage.getDateLivraison());
        dto.setMotifAnnulation(ouvrage.getMotifAnnulation());
        return dto;
    }

    private MesureOuvrageDto toMesureOuvrageDto(MesureOuvrage mesure) {
        MesureOuvrageDto dto = new MesureOuvrageDto();
        dto.setId(mesure.getId());
        dto.setLibelle(mesure.getLibelle());
        dto.setValeur(mesure.getValeur());
        dto.setUnite(mesure.getUnite());
        return dto;
    }

    private DepotMatiereDetailDto toDepotMatiereDetailDto(DepotMatiereClient depot) {
        DepotMatiereDetailDto dto = new DepotMatiereDetailDto();
        dto.setId(depot.getId());
        dto.setDescription(depot.getDescription());
        dto.setQuantiteRecue(depot.getQuantiteRecue());
        dto.setQuantiteConsommee(depot.getQuantiteConsommee());
        dto.setQuantiteRetournee(depot.getQuantiteRetournee());
        dto.setUnite(depot.getUnite());
        dto.setUrlPhotoReference(depot.getUrlPhotoReference());
        dto.setDateDepot(depot.getDateDepot());
        dto.setDateRetour(depot.getDateRetour());
        dto.setObservation(depot.getObservation());
        return dto;
    }

    private String getClientLabel(CommandeConfection commande) {
        if (commande.getClient() == null) {
            return commande.getClientNomPassage();
        }
        return getPersonnelLabel(commande.getClient());
    }

    private String getPersonnelLabel(Personnel personnel) {
        return ((personnel.getPrenom() == null ? "" : personnel.getPrenom()) + " "
                + (personnel.getNom() == null ? "" : personnel.getNom())).trim();
    }

    private int countOuvrages(List<OuvrageConfection> ouvrages, EtatOuvrageConfection etat) {
        return (int) ouvrages.stream().filter(ouvrage -> ouvrage.getEtat() == etat).count();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
