package songoycollection.personne.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.achat.dtos.FournisseurDto;
import songoycollection.achat.dtos.PersonnelAchatDto;
import songoycollection.config.HttpCustumMessage;
import songoycollection.personne.dtos.FournisseurRequestDto;
import songoycollection.personne.model.Fournisseur;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.RepositoryService;

import java.util.Set;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final RepositoryService repositoryService;
    private final PersonnelUtiliShared personnelUtiliShared;
    private final Validator validator;

    @Transactional(readOnly = true)
    public List<FournisseurDto> loadFournisseurs(Long idAdmin) {
        personnelUtiliShared.getPersonnelById(idAdmin);
        return repositoryService.getFourniseurRepository()
                .findAllByOrderByNomAscPrenomAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public HttpCustumMessage addFournisseur(Long idAdmin, FournisseurRequestDto request) {
        Personnel p = personnelUtiliShared.getPersonnelById(idAdmin);
        Fournisseur f = FournisseurRequestDto.toFournisseur(request);
        f.setAdminResponsable(p);
        Set<ConstraintViolation<Fournisseur>> violations = validator.validate(f);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (repositoryService.getFourniseurRepository().existsByTelephone(f.getTelephone())){
            throw new  IllegalArgumentException("Le numero de telephone existe deja.");
        }
        repositoryService.getFourniseurRepository().save(f);

        return HttpCustumMessage.addMessage();
    }

    private FournisseurDto toDto(Fournisseur fournisseur) {
        FournisseurDto dto = new FournisseurDto();
        dto.setId(fournisseur.getId());
        dto.setNom(fournisseur.getNom());
        dto.setPrenom(fournisseur.getPrenom());
        dto.setTelephone(fournisseur.getTelephone());
        dto.setAdresse(fournisseur.getAdresse());
        dto.setAdminResponsable(toPersonnelDto(fournisseur.getAdminResponsable()));
        return dto;
    }

    private PersonnelAchatDto toPersonnelDto(Personnel personnel) {
        return getPersonnelAchatDto(personnel);
    }

    public static PersonnelAchatDto getPersonnelAchatDto(Personnel personnel) {
        if (personnel == null) {
            return null;
        }
        PersonnelAchatDto dto = new PersonnelAchatDto();
        dto.setId(personnel.getId());
        dto.setNom(personnel.getNom());
        dto.setPrenom(personnel.getPrenom());
        return dto;
    }
}
