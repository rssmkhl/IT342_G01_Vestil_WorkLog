package cit.edu.vestil.worklog.admin;

import cit.edu.vestil.worklog.client.Client;
import cit.edu.vestil.worklog.client.ClientRepository;
import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import cit.edu.vestil.worklog.payment.Payment;
import cit.edu.vestil.worklog.payment.PaymentRepository;
import cit.edu.vestil.worklog.worklog.WorkLog;
import cit.edu.vestil.worklog.worklog.WorkLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final WorkLogRepository workLogRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/summary")
    public AdminSummary getSummary() {
        return new AdminSummary(
                userRepository.count(),
                clientRepository.count(),
                workLogRepository.count(),
                paymentRepository.countByStatusIgnoreCase("Pending")
        );
    }

    @GetMapping("/users")
    public List<UserRow> getUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(user -> new UserRow(
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getEmail(),
                        normalizeRole(user.getRole(), user.getUsername())
                ))
                .toList();
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

        paymentRepository.deleteByUser(user);
        workLogRepository.deleteByUser(user);
        clientRepository.deleteByUser(user);
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/worklogs")
    @Transactional(readOnly = true)
    public List<WorkLogRow> getWorkLogs() {
        return workLogRepository.findAll(Sort.by(Sort.Direction.DESC, "date"))
                .stream()
                .map(this::toWorkLogRow)
                .toList();
    }

    @GetMapping("/clients")
    @Transactional(readOnly = true)
    public List<ClientRow> getClients() {
        return clientRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toClientRow)
                .toList();
    }

    @GetMapping("/payments")
    @Transactional(readOnly = true)
    public List<PaymentRow> getPayments() {
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate"))
                .stream()
                .map(this::toPaymentRow)
                .toList();
    }

    private ClientRow toClientRow(Client client) {
        return new ClientRow(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getCompany(),
                client.getNotes(),
                client.getUser() != null ? client.getUser().getFullName() : "Unknown user"
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

    private User loadUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String normalizeRole(String role, String username) {
        if (role != null && !role.isBlank()) {
            return role;
        }
        return "admin".equalsIgnoreCase(username) ? "ADMIN" : "USER";
    }

    public record AdminSummary(long totalUsers, long totalClients, long totalWorkLogs, long pendingPayments) {
    }

    public record UserRow(Long id, String fullName, String username, String email, String role) {
    }

    public record ClientRow(
            Long id,
            String name,
            String email,
            String phone,
            String company,
            String notes,
            String userName
    ) {
    }

    public record WorkLogRow(
            Long id,
            String title,
            String project,
            LocalDate date,
            Double hours,
            String status,
            String userName,
            String clientName
    ) {
    }

    public record PaymentRow(
            Long id,
            BigDecimal amount,
            LocalDate paymentDate,
            String method,
            String status,
            String reference,
            String userName,
            String clientName
    ) {
    }
}
