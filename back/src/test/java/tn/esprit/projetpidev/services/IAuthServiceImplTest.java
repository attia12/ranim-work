package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.projetpidev.domain.enums.*;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.userprofile.CamperProfile;
import tn.esprit.projetpidev.dto.auth.AuthResponse;
import tn.esprit.projetpidev.dto.auth.LoginRequest;
import tn.esprit.projetpidev.dto.auth.RegisterRequest;
import tn.esprit.projetpidev.jwt.JwtService;
import tn.esprit.projetpidev.repositories.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IAuthServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtSevice;
    @Mock private AuthenticationManager authenticationManager;

    @Mock private CamperProfileRepository camperProfileRepo;
    @Mock private ProviderProfileRepository providerProfileRepo;

    @InjectMocks
    private IAuthServiceImpl authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@campway.com")
                .password("encoded_password")
                .role(Role.COMPERS)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@campway.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setRole(Role.COMPERS);
        registerRequest.setExperienceLevel("BEGINNER");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@campway.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_SuccessForCamper() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepo.save(any(User.class))).thenReturn(user);

        String result = authService.register(registerRequest);

        assertNotNull(result);
        assertTrue(result.contains("User created"));
        verify(userRepo, times(1)).save(any(User.class));
        verify(camperProfileRepo, times(1)).save(any(CamperProfile.class));
    }

    @Test
    void testRegister_ThrowsExceptionIfEmailExists() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("An account with this email already exists.", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtSevice.generateToken(user)).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}

