package cit.edu.vestil.worklog.repository;

import cit.edu.vestil.worklog.entity.User;
import cit.edu.vestil.worklog.entity.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByUserOrderByDateDesc(User user);

    void deleteByUser(User user);
}
