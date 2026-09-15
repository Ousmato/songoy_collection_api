package songoycollection.shared;

public final class EnumShared {
    private EnumShared() {
        // Empêche l'instanciation
    }

    public  enum UniteMesureCategory{
        UNITE, METRE, PAGNE,CARTON
    }

    public enum PersonnelRole {
    ADMIN, CAISSIER, APPRENANT, JUNIOR, MENTOR, CLIENT,RESPONSABLE,SUPER_ADMIN
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
        SALAIRE, TRANSPORT, ACCESSOIRE, REPARATION, AUTRE, ACHAT
    }

    public enum ModePaiement {
        CASH,CHECKING,MOBILE, CREDIT
    }

    public enum StatutAchat {
        BROUILLON, VALIDE
    }

    public enum TypeMouvement {
        VENTE, ACHAT, AJUSTEMENT, INVENTAIRE, ANNULATION_VENTE,
    }

    public enum CategoryType {
        SIMPLE, TRANSFERT
    }
}
