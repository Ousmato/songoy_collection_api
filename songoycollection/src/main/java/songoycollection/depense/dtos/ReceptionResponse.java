package songoycollection.depense.dtos;

public record ReceptionResponse(
        Long id,
        String reference,
        Double total,
        Double montantPaye,
        Double resteAPayer,
        String message
) {}

