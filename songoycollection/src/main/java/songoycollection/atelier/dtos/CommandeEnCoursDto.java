package songoycollection.atelier.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CommandeEnCoursDto {
    private Long id;
    private String reference;
    private String client;
    private LocalDateTime dateCreation;
    private LocalDate dateLivraisonPrevue;
    private int nombreOuvrages;
    private int ouvragesEnConfection;
    private int ouvragesPrets;
    private int ouvragesLivres;
    private LocalDateTime dateDerniereLivraison;
    private BigDecimal montantTotal;
    private BigDecimal montantVerse;
    private BigDecimal resteAPayer;
}
