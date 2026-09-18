package songoycollection.depense.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import songoycollection.config.HttpCustumMessage;
import songoycollection.depense.dtos.DepenseRequest;
import songoycollection.depense.dtos.DepenseHistoriqueDto;
import songoycollection.shared.ServiceShared;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class DepenseController {

    private final ServiceShared serviceShared;

    @PostMapping("add-depense/{idAdmin}")
    @Operation(summary = "Enregistrer une dépense générale")
    public HttpCustumMessage addDepense(
            @PathVariable Long idAdmin,
            @Valid @RequestBody DepenseRequest request
    ) {
        return serviceShared.getDepenseService().addDepense(request, idAdmin);
    }

    @GetMapping("get-depenses/{idAdmin}")
    @Operation(summary = "Récupérer les dépenses sur une période")
    public List<DepenseHistoriqueDto> getDepenses(
            @PathVariable Long idAdmin,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return serviceShared.getDepenseService().getDepenses(idAdmin, debut, fin);
    }
}
