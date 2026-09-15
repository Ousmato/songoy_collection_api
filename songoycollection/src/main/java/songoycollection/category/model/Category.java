package songoycollection.category.model;

import jakarta.persistence.*;
import lombok.Data;
import songoycollection.shared.EnumShared;

@Data
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private EnumShared.UniteMesureCategory mesureCategory;

    @Enumerated(EnumType.STRING)
    private EnumShared.CategoryType categoryType;

    @Enumerated(EnumType.STRING)
    private EnumShared.Entite entite;
}
