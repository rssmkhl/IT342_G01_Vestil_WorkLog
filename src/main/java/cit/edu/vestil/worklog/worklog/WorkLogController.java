package cit.edu.vestil.worklog.worklog;

import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.common.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/{id}")
    public ResponseEntity<WorkLog> getWorkLogById(@PathVariable Long id, Principal principal) {
        User user = loadUser(principal);
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work log not found"));
        
        if (!workLog.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this work log");
        }
        
        return ResponseEntity.ok(workLog);
    }

    @PostMapping
    public ResponseEntity<WorkLog> createWorkLog(@RequestBody WorkLog workLog, Principal principal) {
        User user = loadUser(principal);
        workLog.setUser(user);
        return ResponseEntity.ok(workLogRepository.save(workLog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkLog> updateWorkLog(@PathVariable Long id, @RequestBody WorkLog workLogDetails, Principal principal) {
        User user = loadUser(principal);
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work log not found"));
        
        if (!workLog.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this work log");
        }
        
        workLog.setTitle(workLogDetails.getTitle());
        workLog.setDescription(workLogDetails.getDescription());
        workLog.setDate(workLogDetails.getDate());
        workLog.setHours(workLogDetails.getHours());
        workLog.setStatus(workLogDetails.getStatus());
        workLog.setProject(workLogDetails.getProject());
        workLog.setClient(workLogDetails.getClient());
        
        return ResponseEntity.ok(workLogRepository.save(workLog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkLog(@PathVariable Long id, Principal principal) {
        User user = loadUser(principal);
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work log not found"));
        
        if (!workLog.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this work log");
        }
        
        workLogRepository.delete(workLog);
        return ResponseEntity.noContent().build();
    }

    private User loadUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
