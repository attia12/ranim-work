package tn.esprit.projetpidev.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class UserController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Bienvenue Admin !");
    }

    @GetMapping("/coach")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<String> coachEndpoint() {
        return ResponseEntity.ok("Bienvenue Coach !");
    }

    @GetMapping("/guide")
    @PreAuthorize("hasRole('GUIDE')")
    public ResponseEntity<String> guideEndpoint() {
        return ResponseEntity.ok("Bienvenue Guide !");
    }

    @GetMapping("/sponsors")
    @PreAuthorize("hasRole('SPONSORS')")
    public ResponseEntity<String> sponsorsEndpoint() {
        return ResponseEntity.ok("Bienvenue Sponsors !");
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Endpoint public !");
    }
}