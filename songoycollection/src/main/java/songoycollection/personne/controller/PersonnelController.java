package songoycollection.personne.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import songoycollection.personne.dtos.LoginRequestDto;
import songoycollection.personne.dtos.LoginResponseDto;
import songoycollection.personne.dtos.PersonnelDto;
import songoycollection.personne.service.AdminService;
import songoycollection.shared.ServiceShared;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class PersonnelController {

    private final ServiceShared serviceShared;

    @PostMapping("login")
    @Operation(summary = "Se connecter à la base de données")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto) {
        return serviceShared.getAdminService().login(loginRequestDto);
    }

    @PostMapping("add-personnel/{idAdmin}")
    @Operation(summary = "Ajouter un personnel")
    public Object addPersonnel(
            @PathVariable("idAdmin") Long idAdmin, @RequestBody PersonnelDto personnelDto) {
        return serviceShared.getAdminService().addPersonnel(personnelDto, idAdmin);
    }
}
