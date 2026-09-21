package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.DeclinaisonAttribute;

import java.util.Collection;
import java.util.List;

@Repository
public interface DeclinaisonAttributeRepository extends JpaRepository<DeclinaisonAttribute, Long> {

    @Query("""
            select attributeValue
            from DeclinaisonAttribute attributeValue
            join fetch attributeValue.attribute attribute
            where attributeValue.declinaison.id in :declinaisonIds
            order by attributeValue.declinaison.id, attributeValue.id
            """)
    List<DeclinaisonAttribute> findAllByDeclinaisonIdsWithAttribute(
            @Param("declinaisonIds") Collection<Long> declinaisonIds
    );
}
