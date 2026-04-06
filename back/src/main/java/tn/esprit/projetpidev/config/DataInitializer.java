package tn.esprit.projetpidev.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String password = passwordEncoder.encode(
                System.getenv().getOrDefault("SEED_PASSWORD", "Dev@12345!"));

        List<User> usersToSave = new ArrayList<>();

        // 10 Campers
        for (int i = 1; i <= 10; i++) {
            String email = "camper" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                usersToSave.add(User.builder()
                        .firstName("Camper").lastName("User" + i).email(email)
                        .password(password).role(Role.COMPERS).enabled(true)
                        .phoneNumber("5500000" + (i < 10 ? "0" + i : i))
                        .build());
            }
        }

        // 10 Providers
        for (int i = 1; i <= 10; i++) {
            String email = "provider" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                usersToSave.add(User.builder()
                        .firstName("Provider").lastName("User" + i).email(email)
                        .password(password).role(Role.EQUIPEMENTPROVIEDERS).enabled(true)
                        .phoneNumber("5600000" + (i < 10 ? "0" + i : i))
                        .build());
            }
        }

        // 10 Delivery Personnel
        for (int i = 1; i <= 10; i++) {
            String email = "delivery" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                usersToSave.add(User.builder()
                        .firstName("Delivery").lastName("Agent" + i).email(email)
                        .password(password).role(Role.DELIVERYAGENT).enabled(true)
                        .phoneNumber("5700000" + (i < 10 ? "0" + i : i))
                        .build());
            }
        }

        // 2 Admins
        for (int i = 1; i <= 2; i++) {
            String email = "admin" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                usersToSave.add(User.builder()
                        .firstName("Admin").lastName("User" + i).email(email)
                        .password(password).role(Role.ADMIN).enabled(true)
                        .phoneNumber("58000000" + i)
                        .build());
            }
        }

        if (!usersToSave.isEmpty()) {
            userRepo.saveAll(usersToSave);
        }
    }
}
