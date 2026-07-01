package cit.edu.vestil.worklog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String fullName;
    private String username;
    private String email;

    public AuthResponse(String token, Long id, String fullName, String username, String email) {
        this.token = token;
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
    }
}
