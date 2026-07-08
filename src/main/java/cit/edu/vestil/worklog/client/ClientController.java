package cit.edu.vestil.worklog.client;

import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ClientController {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Client> getClients(Principal principal) {
        User user = loadUser(principal);
        return clientRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client, Principal principal) {
        User user = loadUser(principal);
        client.setUser(user);
        return ResponseEntity.ok(clientRepository.save(client));
    }

    private User loadUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
