package songoycollection.personne.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;

@Service
@RequiredArgsConstructor
public class PersonnelUtiliShared {

    private final RepositoryService repo;

    public Personnel getPersonnelById(Long id) {
        return repo.getPersonnelRepository().findByIdAndActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Personne non trouvé"));

    }

    public void getPersonnelIsActiveById(Long id) {
        boolean active =  repo.getPersonnelRepository().existsByIdAndActive(id, true);
        if (!active) {
            throw new IllegalArgumentException("Personne introuvable ou n'existe pas");

        }
    }



    public void getPersonnelRoleIsActiveById(Long id, EnumShared.PersonnelRole role) {
        boolean active =  repo.getPersonnelRepository().existsByIdAndActiveAndRole(id, true, role);
        if (!active) {
            throw new IllegalArgumentException("Autorisation invalide");

        }
    }

}
