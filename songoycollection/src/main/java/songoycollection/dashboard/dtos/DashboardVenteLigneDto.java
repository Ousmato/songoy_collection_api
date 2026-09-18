package songoycollection.dashboard.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

@Data
public class DashboardVenteLigneDto {
    private String reference;
    private Double quantite;
    private EnumShared.UniteMesureCategory unite;
}
