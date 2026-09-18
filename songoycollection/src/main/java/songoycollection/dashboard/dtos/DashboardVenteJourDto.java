package songoycollection.dashboard.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Une vente affichée dans le tableau des ventes de la journée. */
@Data
public class DashboardVenteJourDto {
    private Long id;
    private List<DashboardVenteLigneDto> lignes;
    private String client;
    private Double quantiteArticles;
    private BigDecimal total;
    private LocalDateTime date;
    private EnumShared.ModePaiement modePaiement;
}
