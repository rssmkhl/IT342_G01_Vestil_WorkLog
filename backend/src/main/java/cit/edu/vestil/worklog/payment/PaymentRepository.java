package cit.edu.vestil.worklog.payment;

import cit.edu.vestil.worklog.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserOrderByPaymentDateDesc(User user);

    Optional<Payment> findByIdAndUser(Long id, User user);

    long countByStatusIgnoreCase(String status);

    void deleteByUser(User user);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal sumAmount();

    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user = :user")
    BigDecimal sumAmountByUser(User user);
}
