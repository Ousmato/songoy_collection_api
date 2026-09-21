package songoycollection.stock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.achat.model.Achat;
import songoycollection.achat.model.LigneAchat;
import songoycollection.article.model.Article;
import songoycollection.article.model.ArticleVariant;
import songoycollection.article.model.DeclinaisonAttribute;
import songoycollection.personne.model.Fournisseur;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;
import songoycollection.stock.dtos.StockMouvementHistoriqueDto;
import songoycollection.stock.dtos.StockMouvementAttributDto;
import songoycollection.stock.model.StockLot;
import songoycollection.stock.model.StockMouvement;
import songoycollection.vente.model.Vente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMouvementService {

    private final RepositoryService repositoryService;
    private final PersonnelUtiliShared personnelUtil;

    @Transactional(readOnly = true)
    public List<StockMouvementHistoriqueDto> getHistorique(
            Long idAdmin,
            EnumShared.Entite entiteDemandee,
            LocalDate debut,
            LocalDate fin,
            EnumShared.TypeMouvement type,
            String recherche
    ) {
        Personnel requester = personnelUtil.getPersonnelById(idAdmin);
        EnumShared.Entite entite = resolveEntite(requester, entiteDemandee);
        Period period = resolvePeriod(debut, fin);
        String terme = normalizeSearch(recherche);

        List<StockMouvement> mouvements = repositoryService.getStockMouvementRepository().findHistoriqueByFilters(
                        entite,
                        period.debut().atStartOfDay(),
                        period.finExclusive().atStartOfDay(),
                        type,
                        terme
                );
        if (mouvements.isEmpty()) {
            return List.of();
        }

        var declinaisonIds = mouvements.stream()
                .map(mouvement -> declinaisonId(mouvement.getVariant()))
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, List<DeclinaisonAttribute>> attributsDeclinaisonParId = declinaisonIds.isEmpty()
                ? Map.of()
                : repositoryService.getDeclinaisonAttributeRepository()
                        .findAllByDeclinaisonIdsWithAttribute(declinaisonIds)
                        .stream()
                        .collect(Collectors.groupingBy(attribut -> attribut.getDeclinaison().getId()));

        return mouvements.stream()
                .map(mouvement -> {
                    Long declinaisonId = declinaisonId(mouvement.getVariant());
                    List<DeclinaisonAttribute> attributs = declinaisonId == null
                            ? List.of()
                            : attributsDeclinaisonParId.getOrDefault(declinaisonId, List.of());
                    return toDto(mouvement, attributs);
                })
                .toList();
    }

    private StockMouvementHistoriqueDto toDto(
            StockMouvement mouvement,
            List<DeclinaisonAttribute> attributsDeclinaison
    ) {
        StockLot lot = mouvement.getStockLot();
        ArticleVariant variante = mouvement.getVariant();
        var declinaison = variante == null ? null : variante.getDeclinaison();
        var modele = declinaison == null ? null : declinaison.getModeleArticle();
        LigneAchat ligneAchat = lot == null ? null : lot.getLigneAchat();
        Achat achat = ligneAchat == null ? null : ligneAchat.getAchat();
        Vente vente = mouvement.getLigneVente() == null
                ? null
                : mouvement.getLigneVente().getVente();

        BigDecimal coutUnitaire = money(ligneAchat == null ? null : ligneAchat.getPrixAchat());
        BigDecimal quantite = BigDecimal.valueOf(mouvement.getQuantite() == null
                ? 0.0
                : mouvement.getQuantite());
        Article article = articleOf(variante);

        StockMouvementHistoriqueDto dto = new StockMouvementHistoriqueDto();
        dto.setIdMouvement(mouvement.getId());
        dto.setDate(mouvement.getDateEntree());
        dto.setType(mouvement.getType());
        dto.setLotId(lot == null ? null : lot.getId());
        dto.setArticleId(article == null ? null : article.getId());
        dto.setArticle(articleName(article));
        dto.setModele(modele == null ? "" : emptyIfNull(modele.getNom()));
        dto.setMarque(modele == null ? "" : emptyIfNull(modele.getMarque()));
        dto.setVariantId(variante == null ? null : variante.getId());
        dto.setReferenceVariante(variante == null ? "" : emptyIfNull(variante.getReference()));
        dto.setAttributsDeclinaison(attributsDeclinaison.stream()
                .filter(attribut -> attribut.getAttribute() != null
                        && attribut.getAttribute().getId() != null)
                .map(attribut -> toAttributeDto(
                        attribut.getAttribute().getId(),
                        attribut.getAttribute().getLabel(),
                        attribut.getValue()
                ))
                .toList());
        dto.setAttributsVariante(variante == null || variante.getAttributs() == null
                ? List.of()
                : variante.getAttributs().stream()
                        .filter(attribut -> attribut.getAttribute() != null
                                && attribut.getAttribute().getId() != null)
                        .map(attribut -> toAttributeDto(
                                attribut.getAttribute().getId(),
                                attribut.getAttribute().getLabel(),
                                attribut.getValue()
                        ))
                        .toList());
        dto.setUnite(article == null || article.getCategory() == null
                ? null
                : article.getCategory().getMesureCategory());
        dto.setQuantite(mouvement.getQuantite() == null ? 0.0 : mouvement.getQuantite());
        dto.setCoutAchatUnitaire(coutUnitaire);
        dto.setValeurMouvement(coutUnitaire.multiply(quantite).setScale(2, RoundingMode.HALF_UP));
        dto.setReferenceMouvement(emptyIfNull(mouvement.getReference()));
        dto.setNumeroFacture(numeroFacture(achat, vente));
        dto.setTiers(tiers(achat, vente));
        dto.setResponsable(responsable(lot, achat, vente));
        return dto;
    }

    private Long declinaisonId(ArticleVariant variante) {
        return variante == null || variante.getDeclinaison() == null
                ? null
                : variante.getDeclinaison().getId();
    }

    private StockMouvementAttributDto toAttributeDto(Long id, String label, String value) {
        StockMouvementAttributDto dto = new StockMouvementAttributDto();
        dto.setId(id);
        dto.setLabel(label);
        dto.setValue(value);
        return dto;
    }

    private Article articleOf(ArticleVariant variante) {
        if (variante == null || variante.getDeclinaison() == null
                || variante.getDeclinaison().getModeleArticle() == null) {
            return null;
        }
        return variante.getDeclinaison().getModeleArticle().getArticle();
    }

    private String articleName(Article article) {
        if (article == null) {
            return "Article";
        }
        String categorie = article.getCategory() == null ? null : article.getCategory().getNom();
        String type = article.getArticleType() == null ? null : article.getArticleType().getNom();
        if (categorie == null || categorie.isBlank()) {
            return type == null || type.isBlank() ? "Article" : type;
        }
        if (type == null || type.isBlank()) {
            return categorie;
        }
        return categorie + " - " + type;
    }

    private String numeroFacture(Achat achat, Vente vente) {
        if (vente != null && vente.getNumeroFacture() != null) {
            return vente.getNumeroFacture();
        }
        if (achat != null && achat.getNumeroFacture() != null) {
            return achat.getNumeroFacture();
        }
        return "";
    }

    private String tiers(Achat achat, Vente vente) {
        if (vente != null) {
            Personnel client = vente.getClient();
            if (client != null) {
                return fullName(client.getPrenom(), client.getNom());
            }
            return vente.getNomClient() == null || vente.getNomClient().isBlank()
                    ? "Client passager"
                    : vente.getNomClient();
        }
        if (achat != null) {
            Fournisseur fournisseur = achat.getFournisseur();
            if (fournisseur != null) {
                return fullName(fournisseur.getPrenom(), fournisseur.getNom());
            }
            return emptyIfNull(achat.getNomFournisseur());
        }
        return "";
    }

    private String responsable(StockLot lot, Achat achat, Vente vente) {
        if (vente != null) {
            Personnel caissier = vente.getCaissier();
            return caissier == null ? "" : fullName(caissier.getPrenom(), caissier.getNom());
        }
        Personnel responsableAchat = achat == null ? null : achat.getAdminResponsable();
        if (responsableAchat == null && lot != null) {
            responsableAchat = lot.getPersonnel();
        }
        return responsableAchat == null
                ? ""
                : fullName(responsableAchat.getPrenom(), responsableAchat.getNom());
    }

    private String fullName(String prenom, String nom) {
        return ((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim();
    }

    private String normalizeSearch(String recherche) {
        if (recherche == null || recherche.isBlank()) {
            return null;
        }
        return "%" + recherche.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private BigDecimal money(Double value) {
        return BigDecimal.valueOf(value == null ? 0.0 : value)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Period resolvePeriod(LocalDate debut, LocalDate fin) {
        if ((debut == null) != (fin == null)) {
            throw new IllegalArgumentException("Les deux dates de la période sont obligatoires.");
        }
        if (debut == null) {
            LocalDate aujourdHui = LocalDate.now();
            return new Period(aujourdHui.withDayOfMonth(1), aujourdHui.plusDays(1));
        }
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("La date de début doit être antérieure ou égale à la date de fin.");
        }
        return new Period(debut, fin.plusDays(1));
    }

    private EnumShared.Entite resolveEntite(Personnel requester, EnumShared.Entite entiteDemandee) {
        if (requester.getEntite() == EnumShared.Entite.GLOBAL) {
            if (entiteDemandee == null || entiteDemandee == EnumShared.Entite.GLOBAL) {
                throw new IllegalArgumentException("L'entité du stock est obligatoire.");
            }
            return entiteDemandee;
        }
        if (requester.getEntite() == null) {
            throw new IllegalArgumentException("L'utilisateur n'a pas d'entité définie.");
        }
        if (entiteDemandee != null && entiteDemandee != requester.getEntite()) {
            throw new IllegalArgumentException("L'utilisateur n'est pas autorisé pour cette entité.");
        }
        return requester.getEntite();
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private record Period(LocalDate debut, LocalDate finExclusive) {}
}
