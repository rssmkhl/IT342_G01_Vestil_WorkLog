package cit.edu.vestil.worklog.controller;

import cit.edu.vestil.worklog.entity.Client;
import cit.edu.vestil.worklog.entity.Payment;
import cit.edu.vestil.worklog.entity.User;
import cit.edu.vestil.worklog.entity.WorkLog;
import cit.edu.vestil.worklog.repository.ClientRepository;
import cit.edu.vestil.worklog.repository.PaymentRepository;
import cit.edu.vestil.worklog.repository.UserRepository;
import cit.edu.vestil.worklog.repository.WorkLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class WorklogController {
    private final ClientRepository clientRepository;
    private final WorkLogRepository workLogRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @GetMapping("/clients")
    public List<Client> getClients(Principal principal) {
        User user = loadUser(principal);
        return clientRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @PostMapping("/clients")
    public ResponseEntity<Client> createClient(@RequestBody Client client, Principal principal) {
        User user = loadUser(principal);
        client.setUser(user);
        return ResponseEntity.ok(clientRepository.save(client));
    }

    @GetMapping("/worklogs")
    public List<WorkLog> getWorkLogs(Principal principal) {
        User user = loadUser(principal);
        return workLogRepository.findByUserOrderByDateDesc(user);
    }

    @PostMapping("/worklogs")
    public ResponseEntity<WorkLog> createWorkLog(@RequestBody WorkLog workLog, Principal principal) {
        User user = loadUser(principal);
        workLog.setUser(user);
        return ResponseEntity.ok(workLogRepository.save(workLog));
    }

    @GetMapping("/payments")
    public List<Payment> getPayments(Principal principal) {
        User user = loadUser(principal);
        return paymentRepository.findByUserOrderByPaymentDateDesc(user);
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment, Principal principal) {
        User user = loadUser(principal);
        payment.setUser(user);
        if (payment.getClient() == null || payment.getClient().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client is required");
        }
        Client client = clientRepository.findByIdAndUser(payment.getClient().getId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found"));
        payment.setClient(client);
        if (payment.getAmount() == null) {
            payment.setAmount(BigDecimal.ZERO);
        }
        return ResponseEntity.ok(paymentRepository.save(payment));
    }

    private User loadUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
