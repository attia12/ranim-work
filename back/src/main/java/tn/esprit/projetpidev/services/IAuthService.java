package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.auth.AuthResponse;
import tn.esprit.projetpidev.dto.auth.LoginRequest;
import tn.esprit.projetpidev.dto.auth.RegisterRequest;

import java.util.List;

public interface IAuthService {
    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse updateProfile(Long userId, RegisterRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    List<User> searchUsers(String query);
}