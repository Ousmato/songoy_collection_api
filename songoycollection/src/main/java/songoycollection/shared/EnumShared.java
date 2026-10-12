package songoycollection.shared;

public final class EnumShared {
    private EnumShared() {
        // Empêche l'instanciation
    }

    public  enum UniteMesureCategory{
        UNITE, METRE, PAGNE,CARTON, PAIRE
    }

    public enum PersonnelRole {
    ADMIN, CAISSIER, APPRENANT, JUNIOR, MENTOR, CLIENT,LIVREUR, RESPONSABLE,SUPER_ADMIN, COUTURIER,
    }

    public enum Entite {
        GLOBAL, BOUTIQUE, ATELIER
    }

    public enum StatutPaiement {
        CREDIT,
        PARTIEL,
        PAYE
    }

    public enum MotifDepense {
        SALAIRE, TRANSPORT, ACCESSOIRE, REPARATION, AUTRE, EAUX, ELECTRICITER
    }

    public enum ModePaiement {
        CASH,CHECKING,MOBILE, CREDIT
    }

    public enum StatutAchat {
        BROUILLON, VALIDE
    }

    public enum TypeMouvement {
        VENTE, ACHAT, AJUSTEMENT,
        /** Valeur historique conservée pour lire d'anciennes données ; aucun nouveau mouvement ne l'utilise. */
        @Deprecated INVENTAIRE,
        ANNULATION_VENTE,
    }

    public enum CategoryType {
        SIMPLE, TRANSFERT
    }
}
