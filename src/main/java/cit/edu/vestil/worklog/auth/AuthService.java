package cit.edu.vestil.worklog.auth;

import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import cit.edu.vestil.worklog.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match!");
        }

        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        // Determine role: if provided in request, use it; else use default logic
        String role = registerRequest.getRole();
        if (role == null || role.isBlank()) {
            role = resolveRole(registerRequest.getUsername());
        } else {
            // Validate role is either ADMIN or USER
            if (!"ADMIN".equalsIgnoreCase(role) && !"USER".equalsIgnoreCase(role)) {
                throw new RuntimeException("Invalid role! Must be either ADMIN or USER.");
            }
            role = role.toUpperCase();
        }
        
        user.setRole(role);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        String role = normalizeRole(user.getRole(), user.getUsername());
        if (!role.equals(user.getRole())) {
            user.setRole(role);
            userRepository.save(user);
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getFullName(), role);
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getUsername(), user.getEmail(), role);
    }

    private String resolveRole(String username) {
        if (userRepository.count() == 0 || "admin".equalsIgnoreCase(username)) {
            return "ADMIN";
        }
        return "USER";
    }

    private String normalizeRole(String role, String username) {
        if (role != null && !role.isBlank()) {
            return role;
        }
        return "admin".equalsIgnoreCase(username) ? "ADMIN" : "USER";
    }
}
