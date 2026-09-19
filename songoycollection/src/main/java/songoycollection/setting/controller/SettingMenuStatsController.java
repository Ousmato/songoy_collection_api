package songoycollection.setting.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import songoycollection.setting.dtos.SettingMenuStatsDto;
import songoycollection.shared.EnumShared;
import songoycollection.shared.ServiceShared;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("api/v1/setting-menu")
@RequiredArgsConstructor
public class SettingMenuStatsController {

    private final ServiceShared serviceShared;

    @GetMapping("/statistiques/{idAdmin}")
    @Operation(summary = "Récupérer les statistiques du menu de paramétrage")
    public SettingMenuStatsDto getStats(
            @PathVariable Long idAdmin,
            @RequestParam(required = false) EnumShared.Entite entite
    ) throws AccessDeniedException {
        return serviceShared.getSettingMenuStatsService().getStats(idAdmin, entite);
    }
}
