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

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id, Principal principal) {
        User user = loadUser(principal);
        Payment payment = paymentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return ResponseEntity.ok(payment);
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

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment paymentDetails, Principal principal) {
        User user = loadUser(principal);
        Payment payment = paymentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (paymentDetails.getClient() != null && paymentDetails.getClient().getId() != null) {
            Client client = clientRepository.findByIdAndUser(paymentDetails.getClient().getId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found"));
            payment.setClient(client);
        }

        if (paymentDetails.getAmount() != null) {
            payment.setAmount(paymentDetails.getAmount());
        }

        if (paymentDetails.getMethod() != null) {
            payment.setMethod(paymentDetails.getMethod());
        }

        if (paymentDetails.getStatus() != null) {
            payment.setStatus(paymentDetails.getStatus());
        }

        if (paymentDetails.getReference() != null) {
            payment.setReference(paymentDetails.getReference());
        }

        if (paymentDetails.getPaymentDate() != null) {
            payment.setPaymentDate(paymentDetails.getPaymentDate());
        }

        return ResponseEntity.ok(paymentRepository.save(payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id, Principal principal) {
        User user = loadUser(principal);
        Payment payment = paymentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        paymentRepository.delete(payment);
        return ResponseEntity.noContent().build();
    }

    private User loadUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
