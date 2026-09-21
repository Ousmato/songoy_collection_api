package songoycollection.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.shared.EnumShared;
import songoycollection.stock.model.StockMouvement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMouvementRepository extends JpaRepository<StockMouvement, Long> {

    boolean existsByVariantId(Long variantId);

    @Query("""
            select distinct m
            from StockMouvement m
            join fetch m.stockLot lot
            join fetch m.variant variant
            join fetch variant.declinaison declinaison
            join fetch declinaison.modeleArticle modele
            join fetch modele.article article
            join fetch article.category category
            left join fetch article.articleType articleType
            left join fetch variant.attributs variantAttribute
            left join fetch variantAttribute.attribute
            left join fetch lot.ligneAchat ligneAchat
            left join fetch ligneAchat.achat achat
            left join fetch achat.fournisseur fournisseur
            left join fetch achat.adminResponsable responsableAchat
            left join fetch m.ligneVente ligneVente
            left join fetch ligneVente.vente vente
            left join fetch vente.client client
            left join fetch vente.caissier caissier
            where category.entite = :entite
              and m.dateEntree >= :debut
              and m.dateEntree < :finExclusive
              and (:type is null or m.type = :type)
              and (
                    :recherche is null
                    or lower(coalesce(category.nom, '')) like :recherche
                    or lower(coalesce(articleType.nom, '')) like :recherche
                    or lower(coalesce(variant.reference, '')) like :recherche
                    or lower(coalesce(m.reference, '')) like :recherche
                    or lower(coalesce(achat.numeroFacture, '')) like :recherche
                    or lower(coalesce(vente.numeroFacture, '')) like :recherche
                    or lower(coalesce(fournisseur.nom, '')) like :recherche
                    or lower(coalesce(fournisseur.prenom, '')) like :recherche
                    or lower(coalesce(achat.nomFournisseur, '')) like :recherche
                    or lower(coalesce(client.nom, '')) like :recherche
                    or lower(coalesce(client.prenom, '')) like :recherche
                    or lower(coalesce(vente.nomClient, '')) like :recherche
              )
            order by m.dateEntree desc, m.id desc
            """)
    List<StockMouvement> findHistoriqueByFilters(
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") LocalDateTime debut,
            @Param("finExclusive") LocalDateTime finExclusive,
            @Param("type") EnumShared.TypeMouvement type,
            @Param("recherche") String recherche
    );

    /** Coût d'achat des lots consommés par les ventes sur une période. */
    @Query("""
            select coalesce(sum(m.quantite * ligne.prixAchat), 0)
            from StockMouvement m
            join m.stockLot lot
            join lot.ligneAchat ligne
            where m.type = :type
              and lot.variant.declinaison.modeleArticle.article.category.entite = :entite
              and m.dateEntree >= :debut
              and m.dateEntree < :finExclusive
              and ligne.annulee = false
            """)
    Double sumCostByTypeAndDateBetween(
            @Param("type") EnumShared.TypeMouvement type,
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") LocalDateTime debut,
            @Param("finExclusive") LocalDateTime finExclusive
    );
}
