package songoycollection.atelier.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommandeDetailDto {
    private Long id;
    private String reference;
    private String client;
    private String creePar;
    private LocalDateTime dateCreation;
    private LocalDate dateLivraisonPrevue;
    private String observation;
    private int nombreOuvrages;
    private int ouvragesEnConfection;
    private int ouvragesPrets;
    private int ouvragesLivres;
    private LocalDateTime dateDerniereLivraison;
    private BigDecimal montantTotal;
    private BigDecimal montantVerse;
    private BigDecimal resteAPayer;
    private List<OuvrageDetailDto> ouvrages;
}
