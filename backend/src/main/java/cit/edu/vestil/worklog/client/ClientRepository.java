package cit.edu.vestil.worklog.client;

import cit.edu.vestil.worklog.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByUserOrderByCreatedAtDesc(User user);

    Optional<Client> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);

    long countByUser(User user);
}
