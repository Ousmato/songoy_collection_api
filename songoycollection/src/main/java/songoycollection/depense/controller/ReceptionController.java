package songoycollection.depense.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import songoycollection.depense.dtos.ReceptionRequest;
import songoycollection.depense.dtos.ReceptionResponse;
import songoycollection.depense.service.ReceptionService;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class ReceptionController {
    private final ReceptionService service;

    @PostMapping("receptions/{idAdmin}")
    public ReceptionResponse create(
            @PathVariable Long idAdmin,
            @RequestHeader("Idempotency-Key") UUID receptionKey,
            @Valid @RequestBody ReceptionRequest request
    ) {
        return service.create(idAdmin, receptionKey, request);
    }
}

