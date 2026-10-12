package songoycollection.atelier.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import songoycollection.atelier.dtos.CommandeConfectionRequestDto;
import songoycollection.atelier.dtos.CommandeDetailDto;
import songoycollection.atelier.dtos.AtelierMenuStatsDto;
import songoycollection.atelier.dtos.CommandeEnCoursDto;
import songoycollection.atelier.dtos.OuvrageConfectionRequestDto;
import songoycollection.atelier.dtos.OuvrageEtatUpdateRequestDto;
import songoycollection.atelier.model.CommandeConfection;
import songoycollection.atelier.model.DepotMatiereClient;
import songoycollection.atelier.model.MesureOuvrage;
import songoycollection.atelier.model.OuvrageConfection;
import songoycollection.atelier.model.VersementCommande;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;
import songoycollection.atelier.model.enums.TypeVersementCommande;
import songoycollection.config.HttpCustumMessage;
import songoycollection.fileManagement.FileManagerService;
import songoycollection.personne.dtos.ClientResponseDto;
import songoycollection.personne.dtos.SimplePersonnelResponseDto;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.RepositoryService;
import songoycollection.shared.EnumShared;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandeConfectionService {

    private final RepositoryService repositoryService;
    private final HelperAtelierService helperAtelierService;
    private final FileManagerService fileManager;

    @Transactional(readOnly = true)
    public List<CommandeEnCoursDto> getCommandesByEtats(
            Long idPersonnel,
            List<EtatOuvrageConfection> etats
    ) {
        helperAtelierService.getCommandeReader(idPersonnel);
        List<EtatOuvrageConfection> requestedStates = normalizeEtats(etats);

        List<CommandeConfection> commandes = repositoryService.getCommandeConfectionRepository()
                .findAllWithOuvrageInStates(requestedStates);
        if (commandes.isEmpty()) {
            return List.of();
        }

        List<Long> commandeIds = commandes.stream().map(CommandeConfection::getId).toList();
        Map<Long, List<OuvrageConfection>> ouvragesParCommande = repositoryService
                .getOuvrageConfectionRepository()
                .findAllByCommandeIdsWithCouturier(commandeIds)
                .stream()
                .collect(Collectors.groupingBy(ouvrage -> ouvrage.getCommande().getId()));
        Map<Long, BigDecimal> montantsVerses = repositoryService.getVersementCommandeRepository()
                .findAllByCommandeIds(commandeIds)
                .stream()
                .collect(Collectors.groupingBy(
                        versement -> versement.getCommande().getId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                versement -> versement.getType() == TypeVersementCommande.REMBOURSEMENT
                                        ? versement.getMontant().negate()
                                        : versement.getMontant(),
                                BigDecimal::add
                        )
                ));

        return commandes.stream()
                .map(commande -> helperAtelierService.toCommandeEnCoursDto(
                        commande,
                        ouvragesParCommande.getOrDefault(commande.getId(), List.of()),
                        montantsVerses.getOrDefault(commande.getId(), BigDecimal.ZERO)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CommandeDetailDto getCommandeDetail(
            Long idCommande,
            Long idPersonnel,
            List<EtatOuvrageConfection> etats
    ) {
        helperAtelierService.getCommandeReader(idPersonnel);
        CommandeConfection commande = repositoryService.getCommandeConfectionRepository()
                .findByIdWithClientAndCreator(idCommande)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable."));
        List<OuvrageConfection> ouvrages = repositoryService.getOuvrageConfectionRepository()
                .findAllByCommandeIdsWithCouturier(List.of(idCommande));
        List<EtatOuvrageConfection> requestedStates = etats == null
                ? List.of()
                : etats.stream().distinct().toList();
        List<OuvrageConfection> ouvragesAffiches = requestedStates.isEmpty()
                ? ouvrages
                : ouvrages.stream()
                        .filter(ouvrage -> requestedStates.contains(ouvrage.getEtat()))
                        .toList();
        List<Long> ouvrageIds = ouvragesAffiches.stream().map(OuvrageConfection::getId).toList();
        Map<Long, List<MesureOuvrage>> mesuresParOuvrage = ouvrageIds.isEmpty()
                ? Map.of()
                : repositoryService.getMesureOuvrageRepository()
                        .findAllByOuvrageIds(ouvrageIds)
                        .stream()
                        .collect(Collectors.groupingBy(mesure -> mesure.getOuvrage().getId()));
        Map<Long, List<DepotMatiereClient>> depotsParOuvrage = ouvrageIds.isEmpty()
                ? Map.of()
                : repositoryService.getDepotMatiereClientRepository()
                        .findAllByOuvrageIds(ouvrageIds)
                        .stream()
                        .collect(Collectors.groupingBy(depot -> depot.getOuvrage().getId()));
        BigDecimal montantVerse = repositoryService.getVersementCommandeRepository()
                .findAllByCommandeIds(List.of(idCommande))
                .stream()
                .map(versement -> versement.getType() == TypeVersementCommande.REMBOURSEMENT
                        ? versement.getMontant().negate()
                        : versement.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return helperAtelierService.toCommandeDetailDto(
                commande,
                ouvrages,
                ouvragesAffiches,
                mesuresParOuvrage,
                depotsParOuvrage,
                montantVerse
        );
    }

    private List<EtatOuvrageConfection> normalizeEtats(List<EtatOuvrageConfection> etats) {
        if (etats == null || etats.isEmpty()) {
            throw new IllegalArgumentException("Indiquez au moins un état d'ouvrage.");
        }
        return etats.stream().distinct().toList();
    }

    @Transactional(readOnly = true)
    public AtelierMenuStatsDto getMenuStats(Long idPersonnel) {
        helperAtelierService.getCommandeReader(idPersonnel);

        AtelierMenuStatsDto stats = new AtelierMenuStatsDto();
        stats.setOuvragesEnConfection(repositoryService.getOuvrageConfectionRepository()
                .countByEtat(EtatOuvrageConfection.EN_CONFECTION));
        stats.setOuvragesPretsALivrer(repositoryService.getOuvrageConfectionRepository()
                .countByEtat(EtatOuvrageConfection.PRET));

        LocalDate today = LocalDate.now();
        stats.setOuvragesLivresCeMois(repositoryService.getOuvrageConfectionRepository()
                .countByEtatAndDateLivraisonGreaterThanEqualAndDateLivraisonLessThan(
                        EtatOuvrageConfection.LIVRE,
                        today.withDayOfMonth(1).atStartOfDay(),
                        today.plusMonths(1).withDayOfMonth(1).atStartOfDay()
                ));

        List<Long> commandeIds = repositoryService.getCommandeConfectionRepository()
                .findIdsWithOuvrageInStates(List.of(
                        EtatOuvrageConfection.EN_CONFECTION,
                        EtatOuvrageConfection.PRET,
                        EtatOuvrageConfection.LIVRE
                ));
        if (commandeIds.isEmpty()) {
            return stats;
        }

        Map<Long, BigDecimal> totauxParCommande = repositoryService.getOuvrageConfectionRepository()
                .sumPrixConvenuByCommandeIds(commandeIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));
        Map<Long, BigDecimal> montantsVerses = repositoryService.getVersementCommandeRepository()
                .findAllByCommandeIds(commandeIds)
                .stream()
                .collect(Collectors.groupingBy(
                        versement -> versement.getCommande().getId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                versement -> versement.getType() == TypeVersementCommande.REMBOURSEMENT
                                        ? versement.getMontant().negate()
                                        : versement.getMontant(),
                                BigDecimal::add
                        )
                ));
        BigDecimal restesAEncaisser = commandeIds.stream()
                .map(id -> totauxParCommande.getOrDefault(id, BigDecimal.ZERO)
                        .subtract(montantsVerses.getOrDefault(id, BigDecimal.ZERO)))
                .filter(reste -> reste.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setRestesAEncaisser(restesAEncaisser);
        return stats;
    }

    @Transactional
    public HttpCustumMessage updateOuvrageEtat(
            Long idOuvrage,
            OuvrageEtatUpdateRequestDto request,
            Long idPersonnel
    ) {
        Personnel actor = helperAtelierService.getCommandeReader(idPersonnel);
        OuvrageConfection ouvrage = repositoryService.getOuvrageConfectionRepository()
                .findById(idOuvrage)
                .orElseThrow(() -> new IllegalArgumentException("Ouvrage introuvable."));

        if (request.getEtat() == EtatOuvrageConfection.PRET) {
            if (getMontantReglement(request).signum() > 0) {
                throw new IllegalArgumentException("Un règlement ne peut être enregistré qu'au moment de la livraison.");
            }
            markOuvrageReady(ouvrage, actor);
        } else if (request.getEtat() == EtatOuvrageConfection.LIVRE) {
            deliverOuvrage(ouvrage, request, actor);
        } else {
            throw new IllegalArgumentException("Cette transition d'état n'est pas autorisée.");
        }

        repositoryService.getOuvrageConfectionRepository().save(ouvrage);
        return HttpCustumMessage.updateMessage();
    }

    private void markOuvrageReady(OuvrageConfection ouvrage, Personnel actor) {
        if (ouvrage.getEtat() != EtatOuvrageConfection.EN_CONFECTION) {
            throw new IllegalArgumentException("Seul un ouvrage en confection peut être marqué prêt.");
        }
        if (!helperAtelierService.canMarkOuvrageReady(actor, ouvrage)) {
            throw new IllegalArgumentException("Seul le couturier assigné, un caissier atelier ou un administrateur peut marquer cet ouvrage prêt.");
        }

        ouvrage.setEtat(EtatOuvrageConfection.PRET);
        ouvrage.setDatePret(java.time.LocalDateTime.now());
    }

    private void deliverOuvrage(
            OuvrageConfection ouvrage,
            OuvrageEtatUpdateRequestDto request,
            Personnel actor
    ) {
        if (ouvrage.getEtat() != EtatOuvrageConfection.PRET) {
            throw new IllegalArgumentException("Seul un ouvrage prêt peut être livré.");
        }
        if (!helperAtelierService.canDeliverOuvrage(actor, ouvrage)) {
            throw new IllegalArgumentException("Seul le caissier atelier ou le couturier assigné peut livrer cet ouvrage.");
        }

        registerDeliveryPayment(ouvrage.getCommande(), request, actor);
        ouvrage.setEtat(EtatOuvrageConfection.LIVRE);
        ouvrage.setDateLivraison(java.time.LocalDateTime.now());
    }

    private void registerDeliveryPayment(
            CommandeConfection commande,
            OuvrageEtatUpdateRequestDto request,
            Personnel actor
    ) {
        BigDecimal montantReglement = getMontantReglement(request);
        if (montantReglement.signum() == 0) {
            return;
        }
        if (request.getModePaiement() == null || request.getModePaiement() == EnumShared.ModePaiement.CREDIT) {
            throw new IllegalArgumentException("Choisissez un mode de paiement comptant pour enregistrer le règlement.");
        }

        BigDecimal resteAPayer = getResteAPayer(commande);
        if (resteAPayer.signum() == 0) {
            throw new IllegalArgumentException("Cette commande est déjà totalement réglée.");
        }
        if (montantReglement.compareTo(resteAPayer) > 0) {
            throw new IllegalArgumentException("Le règlement ne peut pas dépasser le reste à payer de la commande.");
        }

        repositoryService.getVersementCommandeRepository().save(
                helperAtelierService.prepareVersementLivraison(
                        commande,
                        montantReglement,
                        request.getModePaiement(),
                        actor
                )
        );
    }

    private BigDecimal getMontantReglement(OuvrageEtatUpdateRequestDto request) {
        return request.getMontantReglement() == null ? BigDecimal.ZERO : request.getMontantReglement();
    }

    private BigDecimal getResteAPayer(CommandeConfection commande) {
        List<Long> commandeIds = List.of(commande.getId());
        BigDecimal totalCommande = repositoryService.getOuvrageConfectionRepository()
                .sumPrixConvenuByCommandeIds(commandeIds)
                .stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal montantVerse = repositoryService.getVersementCommandeRepository()
                .findAllByCommandeIds(commandeIds)
                .stream()
                .map(versement -> versement.getType() == TypeVersementCommande.REMBOURSEMENT
                        ? versement.getMontant().negate()
                        : versement.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalCommande.subtract(montantVerse).max(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getClientsForCommande(Long idPersonnel) {
        helperAtelierService.getCreator(idPersonnel);

        return repositoryService.getPersonnelRepository()
                .findAllByActiveTrueAndRoleOrderByNomAscPrenomAsc(EnumShared.PersonnelRole.CLIENT)
                .stream()
                .map(ClientResponseDto::fromPersonnel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SimplePersonnelResponseDto> getCouturiersForCommande(Long idPersonnel) {
        helperAtelierService.getCreator(idPersonnel);

        return repositoryService.getPersonnelRepository()
                .findAllByActiveTrueAndRoleAndEntiteOrderByNomAscPrenomAsc(
                        EnumShared.PersonnelRole.COUTURIER,
                        EnumShared.Entite.ATELIER
                )
                .stream()
                .map(SimplePersonnelResponseDto::fromPersonnel)
                .toList();
    }

    @Transactional
    public HttpCustumMessage addCommande(
            CommandeConfectionRequestDto request,
            MultiValueMap<String, MultipartFile> images,
            Long idPersonnel
    ) {
        if (request == null || !request.isClientCoherent()) {
            throw new IllegalArgumentException(
                    "Indiquez soit un client enregistré, soit le nom d’un client de passage."
            );
        }

        Personnel creator = helperAtelierService.getCreator(idPersonnel);
        List<Path> createdFiles = new ArrayList<>();
        try {
            CommandeConfection commande = repositoryService.getCommandeConfectionRepository()
                    .save(helperAtelierService.prepareCommande(request, creator));
            Set<String> usedImageKeys = new HashSet<>();
            BigDecimal totalCommande = BigDecimal.ZERO;

            for (OuvrageConfectionRequestDto ouvrageRequest : request.getOuvrages()) {
                OuvrageConfection ouvrage = createOuvrage(
                        commande,
                        ouvrageRequest,
                        images,
                        usedImageKeys,
                        createdFiles
                );
                totalCommande = totalCommande.add(ouvrage.getPrixConvenu());
            }

            VersementCommande versement = helperAtelierService.prepareVersementInitial(
                    request,
                    commande,
                    creator,
                    totalCommande
            );
            if (versement != null) {
                repositoryService.getVersementCommandeRepository().save(versement);
            }
            registerRollbackFileCleanup(createdFiles);
            return HttpCustumMessage.addMessage();
        } catch (IOException exception) {
            deleteFiles(createdFiles);
            throw new IllegalStateException("Impossible d’enregistrer les photos de la commande.", exception);
        } catch (RuntimeException exception) {
            deleteFiles(createdFiles);
            throw exception;
        }
    }

    private OuvrageConfection createOuvrage(
            CommandeConfection commande,
            OuvrageConfectionRequestDto request,
            MultiValueMap<String, MultipartFile> images,
            Set<String> usedImageKeys,
            List<Path> createdFiles
    ) throws IOException {
        String photoReference = saveImage(
                images,
                request.getPhotoReferenceKey(),
                usedImageKeys,
                createdFiles
        );

        OuvrageConfection savedOuvrage = repositoryService.getOuvrageConfectionRepository()
                .save(helperAtelierService.prepareOuvrage(commande, request, photoReference));
        repositoryService.getMesureOuvrageRepository().saveAll(
                helperAtelierService.prepareMesures(savedOuvrage, request.getMesures())
        );
        createDepots(savedOuvrage, request, images, usedImageKeys, createdFiles);
        return savedOuvrage;
    }

    private void createDepots(
            OuvrageConfection ouvrage,
            OuvrageConfectionRequestDto request,
            MultiValueMap<String, MultipartFile> images,
            Set<String> usedImageKeys,
            List<Path> createdFiles
    ) throws IOException {
        if (request.getDepotsMatiereClient() == null || request.getDepotsMatiereClient().isEmpty()) {
            return;
        }

        List<String> photosReference = new ArrayList<>();
        for (var depotRequest : request.getDepotsMatiereClient()) {
            photosReference.add(saveImage(
                    images,
                    depotRequest.getPhotoReferenceKey(),
                    usedImageKeys,
                    createdFiles
            ));
        }
        repositoryService.getDepotMatiereClientRepository().saveAll(
                helperAtelierService.prepareDepots(
                        ouvrage,
                        request.getDepotsMatiereClient(),
                        photosReference
                )
        );
    }

    private String saveImage(
            MultiValueMap<String, MultipartFile> images,
            String imageKey,
            Set<String> usedImageKeys,
            List<Path> createdFiles
    ) throws IOException {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        if (!usedImageKeys.add(imageKey)) {
            throw new IllegalArgumentException("Une photo ne peut être associée qu’à un seul élément.");
        }
        MultipartFile image = images == null ? null : images.getFirst(imageKey);
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("La photo associée à « " + imageKey + " » est introuvable.");
        }
        if (image.getContentType() == null || !image.getContentType().toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Chaque fichier associé à une photo doit être une image.");
        }
        if (image.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Une photo de commande ne doit pas dépasser 5 Mo.");
        }

        Path imagePath = fileManager.save(image, "atelier-commandes");
        createdFiles.add(imagePath);
        return imagePath.getFileName().toString();
    }

    private void registerRollbackFileCleanup(List<Path> createdFiles) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteFiles(createdFiles);
                }
            }
        });
    }

    private void deleteFiles(List<Path> files) {
        for (Path file : files) {
            try {
                fileManager.delete(file);
            } catch (IOException ignored) {
                // Le nettoyage d’un fichier ne doit pas masquer l’erreur métier initiale.
            }
        }
    }

}
