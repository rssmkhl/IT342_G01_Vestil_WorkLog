package cit.edu.vestil.worklog.auditlog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String adminName;

    @Column(nullable = false)
    private String action; // e.g., USER_CREATED, USER_UPDATED, CLIENT_ADDED, etc.

    @Column
    private String targetRecord; // e.g., "User: john.doe", "Client: Acme Inc"

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
