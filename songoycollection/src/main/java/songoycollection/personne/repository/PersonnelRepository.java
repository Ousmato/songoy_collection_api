package songoycollection.personne.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.util.Optional;
import java.util.List;

@Repository
public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    Optional<Personnel> findByIdAndActive(Long id, Boolean active);

    boolean existsByIdAndActive(Long id, Boolean active);



    boolean existsByIdAndActiveAndRole(Long id, Boolean active, EnumShared.PersonnelRole role);



    boolean existsByRole(EnumShared.PersonnelRole role);

    boolean existsByAccessCode(String accessCode);

    Optional<Personnel> findByAccessCodeAndActiveTrue(String accessCode);

    List<Personnel> findAllByActiveTrueAndRoleAndEntiteOrderByNomAscPrenomAsc(
            EnumShared.PersonnelRole role,
            EnumShared.Entite entite
    );

    List<Personnel> findAllByActiveTrueAndRoleOrderByNomAscPrenomAsc(
            EnumShared.PersonnelRole role
    );

    List<Personnel> findAllByEntiteInAndActiveTrueAndRoleNotInOrderByNomAscPrenomAsc(
            List<EnumShared.Entite> entites,
            List<EnumShared.PersonnelRole> rolesExclus
    );

    long countByActiveTrueAndRole(EnumShared.PersonnelRole role);

    long countByActiveTrueAndRoleAndEntite(
            EnumShared.PersonnelRole role,
            EnumShared.Entite entite
    );

    long countByEntiteInAndActiveTrueAndRoleNotIn(
            List<EnumShared.Entite> entites,
            List<EnumShared.PersonnelRole> rolesExclus
    );
}
