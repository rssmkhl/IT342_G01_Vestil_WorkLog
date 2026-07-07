package cit.edu.vestil.worklog.repository;

import cit.edu.vestil.worklog.entity.Client;
import cit.edu.vestil.worklog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByUserOrderByCreatedAtDesc(User user);

    Optional<Client> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
