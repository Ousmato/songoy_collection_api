package songoycollection.personne.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.personne.model.Fournisseur;

import java.util.List;

@Repository
public interface FourniseurRepository extends JpaRepository<Fournisseur, Long> {

    boolean existsByTelephone(String telephone);

    List<Fournisseur> findAllByOrderByNomAscPrenomAsc();
}
