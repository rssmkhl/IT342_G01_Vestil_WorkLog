package cit.edu.vestil.worklog.payment;

import cit.edu.vestil.worklog.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserOrderByPaymentDateDesc(User user);

    long countByStatusIgnoreCase(String status);

    void deleteByUser(User user);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal sumAmount();
}
