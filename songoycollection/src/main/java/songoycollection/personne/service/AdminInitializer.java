package songoycollection.personne.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.repository.PersonnelRepository;
import songoycollection.shared.EnumShared;

@Configuration
public class AdminInitializer {

    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    @Value("${app.default-super-admin.nom:SUPER}")
    private String defaultAdminName;

    @Value("${app.default-super-admin.prenom:Admin}")
    private String defaultAdminPrenom;

    @Value("${app.default-super-admin.telephone:76000000}")
    private String defaultAdminTelephone;

    @Bean
    CommandLineRunner initData(PersonnelRepository repositorie,
                               PasswordEncoder passwordEncoder) {

        return args -> {

            // Créer le SuperAdmin s'il n'existe pas
            if (!repositorie.existsByRole(EnumShared.PersonnelRole.SUPER_ADMIN)) {
                String accessCode;
                do {
                    accessCode = "SUP-" + String.format("%06d", RANDOM.nextInt(1_000_000));
                } while (repositorie.existsByAccessCode(accessCode));
                Personnel user = new Personnel();
                user.setNom(defaultAdminName);
                user.setRole(EnumShared.PersonnelRole.SUPER_ADMIN);
                user.setEntite(EnumShared.Entite.GLOBAL);
                user.setAdresse("Bamako");
                user.setPrenom(defaultAdminPrenom);
                user.setAccessCode(accessCode);
                user.setPassword(passwordEncoder.encode(accessCode));
                user.setActive(true);
                user.setTelephone(defaultAdminTelephone);

                repositorie.save(user);
                //System.out.println("Super Admin initialisé avec le code d'accès : " + accessCode);
            }
        };
    }
}
