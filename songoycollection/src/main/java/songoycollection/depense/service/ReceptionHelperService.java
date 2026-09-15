package songoycollection.depense.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.achat.model.Achat;
import songoycollection.achat.model.LigneAchat;
import songoycollection.article.model.ArticleVariant;
import songoycollection.article.repository.ArticleVariantRepository;
import songoycollection.depense.dtos.ReceptionRequest;
import songoycollection.personne.model.Fournisseur;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;
import songoycollection.stock.model.StockLot;
import songoycollection.stock.model.StockMouvement;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ReceptionHelperService {
    private final EntityManager entityManager;
    private final PersonnelUtiliShared personnelUtil;
    private final ArticleVariantRepository variantsRepository;

    public Personnel validatePersonnel(Long id) {
        personnelUtil.getPersonnelRoleIsActiveById(id, EnumShared.PersonnelRole.ADMIN);
        Personnel personnel = personnelUtil.getPersonnelById(id);
        // Sérialise les réceptions d'un même administrateur, y compris les doubles clics.
        entityManager.lock(personnel, LockModeType.PESSIMISTIC_WRITE);
        return personnel;
    }

    public void validateRequest(ReceptionRequest request) {
        if (request == null || request.date() == null
                || request.lignes() == null || request.lignes().isEmpty()) {
            throw new IllegalArgumentException("La date et les lignes de réception sont obligatoires.");
        }
        if (request.entite() != null && request.entite() != EnumShared.Entite.BOUTIQUE) {
            throw new IllegalArgumentException("Cette réception concerne uniquement la boutique.");
        }
        Set<Long> ids = new HashSet<>();
        for (var line : request.lignes()) {
            if (line == null || line.variantId() == null || line.variantId() <= 0
                    || line.quantite() == null || !Double.isFinite(line.quantite())
                    || line.quantite() <= 0 || line.prixAchat() == null
                    || !Double.isFinite(line.prixAchat()) || line.prixAchat() <= 0) {
                throw new IllegalArgumentException("Chaque ligne doit contenir une variante, une quantité et un prix positifs.");
            }
            if (!ids.add(line.variantId())) {
                throw new IllegalArgumentException("Une variante est répétée dans la réception.");
            }
        }
    }

    public String reserveReference(UUID key) {
        if (key == null) {
            throw new IllegalArgumentException("La clé de réception est obligatoire.");
        }
        String reference = "REC-" + key;
        Long count = entityManager.createQuery(
                "select count(a) from Achat a where a.reference = :reference", Long.class
        ).setParameter("reference", reference).getSingleResult();
        if (count > 0) {
            throw new IllegalArgumentException("Cette réception a déjà été enregistrée.");
        }
        return reference;
    }

    public Fournisseur resolveFournisseur(ReceptionRequest request) {
        boolean hasId = request.fournisseurId() != null;
        boolean hasName = request.nomFournisseur() != null && !request.nomFournisseur().isBlank();
        if (hasId == hasName) {
            throw new IllegalArgumentException("Choisissez un fournisseur ou saisissez son nom, pas les deux.");
        }
        if (!hasId) {
            return null;
        }
        Fournisseur fournisseur = entityManager.find(Fournisseur.class, request.fournisseurId());
        if (fournisseur == null) {
            throw new IllegalArgumentException("Fournisseur introuvable.");
        }
        return fournisseur;
    }

    public Map<Long, ArticleVariant> loadVariants(ReceptionRequest request) {
        Map<Long, ArticleVariant> variants = new HashMap<>();
        // Ordre fixe des verrous pour limiter les interblocages entre réceptions.
        for (Long id : request.lignes().stream().map(ReceptionRequest.Ligne::variantId).sorted().toList()) {
            ArticleVariant variant = variantsRepository.findForStockUpdate(id)
                    .orElseThrow(() -> new IllegalArgumentException("Variante introuvable : " + id));
            var category = variant.getArticle().getCategory();
            if (!variant.getArticle().isActive() || !category.isActive()) {
                throw new IllegalArgumentException("L'article ou sa catégorie est inactif.");
            }
            if (category.getEntite() == EnumShared.Entite.ATELIER) {
                throw new IllegalArgumentException("Cette variante appartient à une catégorie atelier.");
            }
            variants.put(id, variant);
        }
        return variants;
    }

    public Double calculateTotal(ReceptionRequest request) {
        double total = 0.0;
        for (var line : request.lignes()) {
            double amount = line.quantite() * line.prixAchat();
            if (!Double.isFinite(amount) || amount > 9_007_199_254_740_991d) {
                throw new IllegalArgumentException("Le montant de la ligne est trop élevé.");
            }
            // Même arrondi au FCFA par ligne que dans le formulaire.
            total += Math.floor(amount + 0.5d);
        }
        if (!Double.isFinite(total) || total <= 0 || total > 9_007_199_254_740_991d) {
            throw new IllegalArgumentException("Le total de réception est invalide.");
        }
        return total;
    }

    public Double validatePayment(ReceptionRequest.Paiement payment, Double total) {
        if (payment == null) {
            return 0.0;
        }
        Double amount = payment.montant();
        if (amount == null || !Double.isFinite(amount) || amount < 0 || amount > total) {
            throw new IllegalArgumentException("Le paiement doit être compris entre zéro et le total.");
        }
        if (amount > 0 && (payment.modePaiement() == null
                || payment.modePaiement() == EnumShared.ModePaiement.CREDIT)) {
            throw new IllegalArgumentException("Précisez le moyen utilisé pour la somme versée.");
        }
        return amount;
    }

    public Achat createAchat(ReceptionRequest request, String reference,
                             Personnel personnel, Fournisseur fournisseur) {
        Achat achat = new Achat();
        achat.setDate(request.date().atStartOfDay());
        achat.setReference(reference);
        achat.setNumeroFacture(trimToNull(request.numeroFacture()));
        achat.setEntite(EnumShared.Entite.BOUTIQUE);
        achat.setAdminResponsable(personnel);
        achat.setFournisseur(fournisseur);
        achat.setNomFournisseur(fournisseur == null ? request.nomFournisseur().trim() : null);
        achat.setModePaiement(request.paiement() == null || request.paiement().montant() == 0
                ? EnumShared.ModePaiement.CREDIT : request.paiement().modePaiement());
        entityManager.persist(achat);
        return achat;
    }

    public List<LigneAchat> createLines(Achat achat, ReceptionRequest request,
                                        Map<Long, ArticleVariant> variants) {
        List<LigneAchat> lines = new ArrayList<>();
        for (var input : request.lignes()) {
            LigneAchat line = new LigneAchat();
            line.setAchat(achat);
            line.setVariant(variants.get(input.variantId()));
            line.setQuantite(input.quantite());
            line.setPrixAchat(input.prixAchat());
            line.setDate(achat.getDate());
            entityManager.persist(line);
            lines.add(line);
        }
        return lines;
    }

    public void createStock(Achat achat, List<LigneAchat> lines, Personnel personnel) {
        for (LigneAchat line : lines) {
            StockLot lot = createLot(line, personnel);
            createMovement(achat, line, lot);
        }
    }

    private StockLot createLot(LigneAchat line, Personnel personnel) {
        StockLot lot = new StockLot();
        lot.setVariant(line.getVariant());
        lot.setLigneAchat(line);
        lot.setInitialQuantity(line.getQuantite());
        lot.setQuantityRestante(line.getQuantite());
        lot.setPersonnel(personnel);
        lot.setEntryDate(line.getDate().toLocalDate());
        lot.setCreatedAt(LocalDateTime.now());
        entityManager.persist(lot);
        return lot;
    }

    private void createMovement(Achat achat, LigneAchat line, StockLot lot) {
        StockMouvement mouvement = new StockMouvement();
        mouvement.setStockLot(lot);
        mouvement.setVariant(line.getVariant());
        mouvement.setQuantite(line.getQuantite());
        mouvement.setType(EnumShared.TypeMouvement.ACHAT);
        mouvement.setReference(achat.getReference());
        mouvement.setDateEntree(achat.getDate());
        entityManager.persist(mouvement);
    }

    public void validateAchat(Achat achat) {
        achat.setStatut(EnumShared.StatutAchat.VALIDE);
        achat.setDateValidation(LocalDateTime.now());
        entityManager.flush();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
