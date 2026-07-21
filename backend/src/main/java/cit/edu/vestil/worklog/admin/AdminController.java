package cit.edu.vestil.worklog.admin;

import cit.edu.vestil.worklog.auditlog.AuditLog;
import cit.edu.vestil.worklog.auditlog.AuditLogRepository;
import cit.edu.vestil.worklog.client.Client;
import cit.edu.vestil.worklog.client.ClientRepository;
import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import cit.edu.vestil.worklog.payment.Payment;
import cit.edu.vestil.worklog.payment.PaymentRepository;
import cit.edu.vestil.worklog.worklog.WorkLog;
import cit.edu.vestil.worklog.worklog.WorkLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final WorkLogRepository workLogRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/summary")
    public AdminSummary getSummary() {
        BigDecimal totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        long workLogsThisWeek = workLogRepository.findAll().stream()
                .filter(wl -> wl.getDate() != null && wl.getDate().isAfter(oneWeekAgo))
                .count();

        return new AdminSummary(
                userRepository.count(),
                clientRepository.count(),
                workLogsThisWeek,
                paymentRepository.countByStatusIgnoreCase("Pending"),
                totalRevenue,
                0L
        );
    }

    @GetMapping("/users")
    public List<UserRow> getUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(user -> new UserRow(
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus(),
                        user.getCreatedAt(),
                        user.getLastLogin()
                ))
                .toList();
    }

    @PostMapping("/users")
    @Transactional
    public ResponseEntity<UserRow> createUser(@RequestBody CreateUserRequest request, Principal principal) {
        User currentUser = loadUser(principal);

        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User newUser = new User();
        newUser.setFullName(request.fullName());
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setRole(request.role() != null ? request.role() : "FREELANCER");
        newUser.setStatus("ACTIVE");
        newUser.setPassword(passwordEncoder.encode(request.password() != null ? request.password() : generateTempPassword()));
        User savedUser = userRepository.save(newUser);

        logAudit(currentUser.getFullName(), "USER_CREATED", "User: " + savedUser.getUsername());

        return ResponseEntity.ok(toUserRow(savedUser));
    }

    @PutMapping("/users/{id}")
    @Transactional
    public ResponseEntity<UserRow> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request, Principal principal) {
        User currentUser = loadUser(principal);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.email() != null) user.setEmail(request.email());
        if (request.username() != null) user.setUsername(request.username());
        if (request.role() != null) user.setRole(request.role());

        User savedUser = userRepository.save(user);
        logAudit(currentUser.getFullName(), "USER_UPDATED", "User: " + savedUser.getUsername());

        return ResponseEntity.ok(toUserRow(savedUser));
    }

    @PostMapping("/users/{id}/reset-password")
    @Transactional
    public ResponseEntity<String> resetPassword(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String tempPassword = generateTempPassword();
        user.setTemporaryPassword(tempPassword);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        logAudit(currentUser.getFullName(), "PASSWORD_RESET", "User: " + user.getUsername());

        return ResponseEntity.ok(tempPassword);
    }

    @PostMapping("/users/{id}/toggle-status")
    @Transactional
    public ResponseEntity<UserRow> toggleUserStatus(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot toggle your own status");
        }

        if ("ADMIN".equals(user.getRole()) && userRepository.countByRole("ADMIN") <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot disable the last admin");
        }

        String newStatus = "ACTIVE".equals(user.getStatus()) ? "INACTIVE" : "ACTIVE";
        user.setStatus(newStatus);
        User savedUser = userRepository.save(user);

        logAudit(currentUser.getFullName(), "USER_STATUS_CHANGED", "User: " + savedUser.getUsername() + " → " + newStatus);

        return ResponseEntity.ok(toUserRow(savedUser));
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }

        if ("ADMIN".equals(user.getRole()) && userRepository.countByRole("ADMIN") <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete the last admin");
        }

        user.setStatus("INACTIVE");
        userRepository.save(user);

        logAudit(currentUser.getFullName(), "USER_DELETED", "User: " + user.getUsername());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/worklogs")
    @Transactional(readOnly = true)
    public List<WorkLogRow> getWorkLogs() {
        return workLogRepository.findAll().stream()
                .sorted(Comparator.comparing(WorkLog::getDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toWorkLogRow)
                .toList();
    }

    @PutMapping("/worklogs/{id}/approve")
    @Transactional
    public ResponseEntity<WorkLogRow> approveWorkLog(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work log not found"));
        workLog.setStatus("APPROVED");
        WorkLog saved = workLogRepository.save(workLog);
        logAudit(currentUser.getFullName(), "WORK_LOG_APPROVED", "Work log ID: " + saved.getId());
        return ResponseEntity.ok(toWorkLogRow(saved));
    }

    @PutMapping("/worklogs/{id}/reject")
    @Transactional
    public ResponseEntity<WorkLogRow> rejectWorkLog(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work log not found"));
        workLog.setStatus("REJECTED");
        WorkLog saved = workLogRepository.save(workLog);
        logAudit(currentUser.getFullName(), "WORK_LOG_REJECTED", "Work log ID: " + saved.getId());
        return ResponseEntity.ok(toWorkLogRow(saved));
    }

    @GetMapping("/clients")
    @Transactional(readOnly = true)
    public List<ClientRow> getClients() {
        return clientRepository.findAll().stream()
                .sorted(Comparator.comparing(Client::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toClientRow)
                .toList();
    }

    @PutMapping("/clients/{id}/archive")
    @Transactional
    public ResponseEntity<ClientRow> archiveClient(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
        logAudit(currentUser.getFullName(), "CLIENT_ARCHIVED", "Client: " + client.getName());
        return ResponseEntity.ok(toClientRow(client));
    }

    @GetMapping("/payments")
    @Transactional(readOnly = true)
    public List<PaymentRow> getPayments() {
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toPaymentRow)
                .toList();
    }

    @PutMapping("/payments/{id}/approve")
    @Transactional
    public ResponseEntity<PaymentRow> approvePayment(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        payment.setStatus("PAID");
        Payment saved = paymentRepository.save(payment);
        logAudit(currentUser.getFullName(), "PAYMENT_APPROVED", "Payment ID: " + saved.getId());
        return ResponseEntity.ok(toPaymentRow(saved));
    }

    @PutMapping("/payments/{id}/reject")
    @Transactional
    public ResponseEntity<PaymentRow> rejectPayment(@PathVariable Long id, Principal principal) {
        User currentUser = loadUser(principal);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        payment.setStatus("REJECTED");
        Payment saved = paymentRepository.save(payment);
        logAudit(currentUser.getFullName(), "PAYMENT_REJECTED", "Payment ID: " + saved.getId());
        return ResponseEntity.ok(toPaymentRow(saved));
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    private ClientRow toClientRow(Client client) {
        return new ClientRow(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getCompany(),
                client.getNotes(),
                client.getUser() != null ? client.getUser().getFullName() : "Unknown user",
                "ACTIVE"
        );
    }

    private WorkLogRow toWorkLogRow(WorkLog workLog) {
        return new WorkLogRow(
                workLog.getId(),
                workLog.getTitle(),
                workLog.getProject(),
                workLog.getDate(),
                workLog.getHours(),
                workLog.getStatus(),
                workLog.getUser() != null ? workLog.getUser().getFullName() : "Unknown user",
                workLog.getClient() != null ? workLog.getClient().getName() : "No client"
        );
    }

    private PaymentRow toPaymentRow(Payment payment) {
        return new PaymentRow(
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getReference(),
                payment.getUser() != null ? payment.getUser().getFullName() : "Unknown user",
                payment.getClient() != null ? payment.getClient().getName() : "No client"
        );
    }

    private UserRow toUserRow(User user) {
        return new UserRow(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }

    private User loadUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void logAudit(String adminName, String action, String targetRecord) {
        AuditLog log = new AuditLog();
        log.setAdminName(adminName);
        log.setAction(action);
        log.setTargetRecord(targetRecord);
        auditLogRepository.save(log);
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public record AdminSummary(
            long totalFreelancers,
            long activeClients,
            long workLogsThisWeek,
            long pendingPayments,
            BigDecimal totalRevenue,
            long activeProjects
    ) {}

    public record UserRow(
            Long id,
            String fullName,
            String username,
            String email,
            String role,
            String status,
            LocalDateTime createdAt,
            LocalDateTime lastLogin
    ) {}

    public record ClientRow(
            Long id,
            String name,
            String email,
            String phone,
            String company,
            String notes,
            String userName,
            String status
    ) {}

    public record WorkLogRow(
            Long id,
            String title,
            String project,
            LocalDate date,
            Double hours,
            String status,
            String userName,
            String clientName
    ) {}

    public record PaymentRow(
            Long id,
            BigDecimal amount,
            LocalDate paymentDate,
            String method,
            String status,
            String reference,
            String userName,
            String clientName
    ) {}

    public record CreateUserRequest(
            String fullName,
            String username,
            String email,
            String role,
            String password
    ) {}

    public record UpdateUserRequest(
            String fullName,
            String username,
            String email,
            String role
    ) {}
}
