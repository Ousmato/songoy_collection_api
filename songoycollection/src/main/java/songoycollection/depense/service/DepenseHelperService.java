package songoycollection.depense.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.depense.dtos.DepenseRequest;
import songoycollection.depense.dtos.DepenseHistoriqueDto;
import songoycollection.depense.dtos.SalaireHistoriqueDto;
import songoycollection.depense.model.Depense;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.model.Fournisseur;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** Validations et construction d'une dépense dans la transaction appelante. */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class DepenseHelperService {

    private final EntityManager entityManager;
    private final PersonnelUtiliShared personnelUtil;

    public void validateRequest(DepenseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Les données de la dépense sont obligatoires.");
        }
        if (request.date() == null || request.date().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de la dépense est invalide.");
        }
        if (request.motif() == null) {
            throw new IllegalArgumentException("Le motif de la dépense est obligatoire.");
        }
        if (request.entite() == null) {
            throw new IllegalArgumentException("L'entité de la dépense est obligatoire.");
        }
        if (request.montant() == null || request.montant().signum() <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro.");
        }
        if (request.modePaiement() == null) {
            throw new IllegalArgumentException("Le moyen de paiement est obligatoire.");
        }
        if (request.modePaiement() == EnumShared.ModePaiement.CREDIT) {
            throw new IllegalArgumentException(
                    "Une dépense générale doit être enregistrée avec un paiement effectué."
            );
        }
        boolean hasSupplierId = request.fournisseurId() != null;
        boolean hasSupplierName = request.nomFournisseur() != null
                && !request.nomFournisseur().isBlank();
        if (hasSupplierId && hasSupplierName) {
            throw new IllegalArgumentException(
                    "Choisissez un fournisseur enregistré ou saisissez un nom libre, pas les deux."
            );
        }
        if (hasSupplierId && request.fournisseurId() <= 0) {
            throw new IllegalArgumentException("Identifiant du fournisseur invalide.");
        }
        if (request.motif() == EnumShared.MotifDepense.SALAIRE
                && (request.personnelBeneficiaireId() == null
                || request.personnelBeneficiaireId() <= 0)) {
            throw new IllegalArgumentException(
                    "Le bénéficiaire est obligatoire pour une dépense de salaire."
            );
        }
        if (request.motif() == EnumShared.MotifDepense.SALAIRE) {
            validateSalaryPeriod(request.periodeSalaire());
        }
    }

    public Personnel loadResponsible(Long idAdmin) {
        personnelUtil.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        return personnelUtil.getPersonnelById(idAdmin);
    }

    public void validateAdmin(Long idAdmin) {
        personnelUtil.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
    }

    public Personnel resolveBeneficiary(DepenseRequest request) {
        Long beneficiaryId = request.personnelBeneficiaireId();
        if (beneficiaryId == null) {
            return null;
        }
        if (beneficiaryId <= 0) {
            throw new IllegalArgumentException("Identifiant du bénéficiaire invalide.");
        }
        return personnelUtil.getPersonnelById(beneficiaryId);
    }

    public Fournisseur resolveFournisseur(DepenseRequest request) {
        if (request.fournisseurId() == null) {
            return null;
        }
        Fournisseur fournisseur = entityManager.find(Fournisseur.class, request.fournisseurId());
        if (fournisseur == null) {
            throw new IllegalArgumentException("Fournisseur introuvable.");
        }
        return fournisseur;
    }

    public Depense createEntity(
            DepenseRequest request,
            Personnel responsable,
            Personnel beneficiaire,
            Fournisseur fournisseur
    ) {
        Depense depense = new Depense();
        depense.setDate(toRecordedDateTime(request.date()));
        depense.setMotifDepense(request.motif());
        depense.setEntite(request.entite());
        depense.setLibelle(request.libelle());
        depense.setMontant(request.montant().setScale(2, RoundingMode.HALF_UP));
        depense.setModePaiement(request.modePaiement());
        depense.setPeriodeSalaire(request.motif() == EnumShared.MotifDepense.SALAIRE
                ? request.periodeSalaire().trim()
                : null);
        depense.setPersonnelResponsable(responsable);
        depense.setPersonnelBeneficiaire(beneficiaire);
        depense.setFournisseur(fournisseur);
        depense.setNomFournisseur(fournisseur == null ? trimToNull(request.nomFournisseur()) : null);
        return depense;
    }

    public DepenseHistoriqueDto toHistoryDto(Depense depense) {
        DepenseHistoriqueDto dto = new DepenseHistoriqueDto();
        dto.setId(depense.getId());
        dto.setDate(depense.getDate());
        dto.setMotif(depense.getMotifDepense());
        dto.setEntite(depense.getEntite());
        dto.setLibelle(depense.getLibelle());
        dto.setMontant(depense.getMontant());
        dto.setModePaiement(depense.getModePaiement());

        Personnel responsable = depense.getPersonnelResponsable();
        if (responsable != null) {
            dto.setPersonnelResponsableId(responsable.getId());
            dto.setPersonnelResponsable(personName(responsable));
        }

        Personnel beneficiaire = depense.getPersonnelBeneficiaire();
        if (beneficiaire != null) {
            dto.setPersonnelBeneficiaireId(beneficiaire.getId());
            dto.setPersonnelBeneficiaire(personName(beneficiaire));
        }
        Fournisseur fournisseur = depense.getFournisseur();
        if (fournisseur != null) {
            dto.setFournisseurId(fournisseur.getId());
            dto.setFournisseur(supplierName(fournisseur));
        } else {
            dto.setFournisseur(depense.getNomFournisseur());
        }
        return dto;
    }

    public SalaireHistoriqueDto toSalaryHistoryDto(Depense depense) {
        SalaireHistoriqueDto dto = new SalaireHistoriqueDto();
        dto.setId(depense.getId());
        dto.setDatePaiement(depense.getDate());
        dto.setPeriodeSalaire(depense.getPeriodeSalaire());
        dto.setEntite(depense.getEntite());
        dto.setMontant(depense.getMontant());
        dto.setModePaiement(depense.getModePaiement());
        dto.setLibelle(depense.getLibelle());

        Personnel beneficiaire = depense.getPersonnelBeneficiaire();
        if (beneficiaire != null) {
            dto.setPersonnelBeneficiaireId(beneficiaire.getId());
            dto.setPersonnelBeneficiaire(personName(beneficiaire));
            dto.setRoleBeneficiaire(beneficiaire.getRole());
        }

        Personnel responsable = depense.getPersonnelResponsable();
        if (responsable != null) {
            dto.setPersonnelResponsableId(responsable.getId());
            dto.setPersonnelResponsable(personName(responsable));
        }
        return dto;
    }

    public void validateSalaryPeriod(String periodeSalaire) {
        if (periodeSalaire == null || periodeSalaire.isBlank()) {
            throw new IllegalArgumentException(
                    "La periode de salaire est obligatoire au format AAAA-MM."
            );
        }
        try {
            YearMonth.parse(periodeSalaire.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "La periode de salaire doit respecter le format AAAA-MM."
            );
        }
    }

    private String personName(Personnel person) {
        return ((person.getPrenom() == null ? "" : person.getPrenom()) + " "
                + (person.getNom() == null ? "" : person.getNom())).trim();
    }

    private String supplierName(Fournisseur fournisseur) {
        return ((fournisseur.getPrenom() == null ? "" : fournisseur.getPrenom()) + " "
                + (fournisseur.getNom() == null ? "" : fournisseur.getNom())).trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDateTime toRecordedDateTime(LocalDate date) {
        return date.equals(LocalDate.now())
                ? date.atTime(LocalTime.now())
                : date.atStartOfDay();
    }
}
