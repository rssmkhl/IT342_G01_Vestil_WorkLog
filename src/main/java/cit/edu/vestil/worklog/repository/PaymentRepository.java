package cit.edu.vestil.worklog.repository;

import cit.edu.vestil.worklog.entity.Payment;
import cit.edu.vestil.worklog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserOrderByPaymentDateDesc(User user);

    long countByStatusIgnoreCase(String status);

    void deleteByUser(User user);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal sumAmount();
}
