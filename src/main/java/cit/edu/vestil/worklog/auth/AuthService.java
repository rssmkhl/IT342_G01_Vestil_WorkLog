package cit.edu.vestil.worklog.auth;

import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import cit.edu.vestil.worklog.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private JavaMailSender mailSender;

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
        
        // Determine role: use resolveRole to decide
        String role = resolveRole(registerRequest.getUsername());
        user.setRole(role);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        System.out.println("Login attempt for: " + loginRequest.getUsernameOrEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }

        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Found user: " + user.getUsername() + ", role: " + user.getRole());

        String role = normalizeRole(user.getRole(), user.getUsername());
        if (!role.equals(user.getRole())) {
            user.setRole(role);
            userRepository.save(user);
            System.out.println("Updated user role to: " + role);
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getFullName(), role);
        System.out.println("Generated token for user: " + user.getUsername());
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

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));
        
        // Generate reset token
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
        userRepository.save(user);
        
        // Send email with reset link
        sendResetEmail(user.getEmail(), token);
    }

    private void sendResetEmail(String email, String token) {
        if (mailSender == null) {
            System.out.println("Mail sender not configured. Reset token (for testing): " + token);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" +
                "http://localhost:3000/reset-password?token=" + token + "\n\n" +
                "This link expires in 1 hour.");
        message.setFrom("your-email@gmail.com");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send reset email: " + e.getMessage());
            // Don't throw an exception here, so the user still gets a success message
        }
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match!");
        }
        
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
        
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired!");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
