package cit.edu.vestil.worklog.payment;

import cit.edu.vestil.worklog.client.Client;
import cit.edu.vestil.worklog.client.ClientRepository;
import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @GetMapping
    public List<Payment> getPayments(Principal principal) {
        User user = loadUser(principal);
        return paymentRepository.findByUserOrderByPaymentDateDesc(user);
    }

    @PostMapping
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
