package songoycollection.vente.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;
import songoycollection.vente.model.Vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class VenteHistoriqueDto {

    private Long id;
    private String numeroFacture;
    private LocalDateTime date;
    private String client;

    private BigDecimal totalVente;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;

    private EnumShared.StatutPaiement statutPaiement;
    private EnumShared.ModePaiement modePaiement;

    public static VenteHistoriqueDto fromVente(Vente vente) {
        return fromVente(vente, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static VenteHistoriqueDto fromVente(
            Vente vente,
            BigDecimal totalVente,
            BigDecimal montantPaye
    ) {
        VenteHistoriqueDto dto = new VenteHistoriqueDto();
        dto.setId(vente.getId());
        dto.setNumeroFacture(vente.getNumeroFacture());
        dto.setDate(vente.getDate());
        dto.setClient(clientName(vente));
        dto.setModePaiement(vente.getModePaiement());

        BigDecimal total = amountOrZero(totalVente);
        BigDecimal paid = amountOrZero(montantPaye).max(BigDecimal.ZERO).min(total);
        dto.setTotalVente(total);
        dto.setMontantPaye(paid);
        dto.setResteAPayer(total.subtract(paid).max(BigDecimal.ZERO));
        dto.setStatutPaiement(paymentStatus(total, paid));
        return dto;
    }

    public static List<VenteHistoriqueDto> fromVentes(List<Vente> ventes) {
        if (ventes == null || ventes.isEmpty()) {
            return Collections.emptyList();
        }
        return ventes.stream().map(VenteHistoriqueDto::fromVente).toList();
    }

    private static String clientName(Vente vente) {
        if (vente.getClient() != null) {
            String firstName = vente.getClient().getPrenom();
            String lastName = vente.getClient().getNom();
            return ((firstName == null ? "" : firstName) + " "
                    + (lastName == null ? "" : lastName)).trim();
        }
        return vente.getNomClient();
    }

    private static BigDecimal amountOrZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private static EnumShared.StatutPaiement paymentStatus(
            BigDecimal total,
            BigDecimal paid
    ) {
        if (paid.compareTo(BigDecimal.ZERO) <= 0) {
            return EnumShared.StatutPaiement.CREDIT;
        }
        return paid.compareTo(total) >= 0
                ? EnumShared.StatutPaiement.PAYE
                : EnumShared.StatutPaiement.PARTIEL;
    }
}
