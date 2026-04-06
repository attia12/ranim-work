package tn.esprit.projetpidev.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@lombok.NoArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatar;
    private String address;
    private String city;
}