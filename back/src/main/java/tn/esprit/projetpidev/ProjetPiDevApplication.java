package tn.esprit.projetpidev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjetPiDevApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetPiDevApplication.class, args);
    }

}
