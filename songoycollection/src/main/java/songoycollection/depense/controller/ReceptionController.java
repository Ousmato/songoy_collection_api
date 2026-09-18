package songoycollection.depense.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import songoycollection.achat.service.ReceptionService;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class ReceptionController {
    private final ReceptionService service;




}

