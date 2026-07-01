package cit.edu.vestil.worklog.controller;

import cit.edu.vestil.worklog.dto.AuthResponse;
import cit.edu.vestil.worklog.dto.LoginRequest;
import cit.edu.vestil.worklog.dto.RegisterRequest;
import cit.edu.vestil.worklog.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid username/email or password");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
