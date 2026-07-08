package cit.edu.vestil.worklog.worklog;

import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/worklogs")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class WorkLogController {
    private final WorkLogRepository workLogRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<WorkLog> getWorkLogs(Principal principal) {
        User user = loadUser(principal);
        return workLogRepository.findByUserOrderByDateDesc(user);
    }

    @PostMapping
    public ResponseEntity<WorkLog> createWorkLog(@RequestBody WorkLog workLog, Principal principal) {
        User user = loadUser(principal);
        workLog.setUser(user);
        return ResponseEntity.ok(workLogRepository.save(workLog));
    }

    private User loadUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
