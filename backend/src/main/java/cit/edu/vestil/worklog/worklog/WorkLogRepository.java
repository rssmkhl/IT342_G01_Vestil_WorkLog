package cit.edu.vestil.worklog.worklog;

import cit.edu.vestil.worklog.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByUserOrderByDateDesc(User user);

    Optional<WorkLog> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);

    long countByUser(User user);
}
