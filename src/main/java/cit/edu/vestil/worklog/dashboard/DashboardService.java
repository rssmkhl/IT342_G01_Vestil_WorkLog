package cit.edu.vestil.worklog.dashboard;

import cit.edu.vestil.worklog.client.ClientRepository;
import cit.edu.vestil.worklog.payment.PaymentRepository;
import cit.edu.vestil.worklog.worklog.WorkLogRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class DashboardService {
    private final ClientRepository clientRepository;
    private final WorkLogRepository workLogRepository;
    private final PaymentRepository paymentRepository;

    public DashboardSummary getSummary() {
        long totalClients = clientRepository.count();
        long totalWorkLogs = workLogRepository.count();
        BigDecimal totalPayments = paymentRepository.sumAmount();
        if (totalPayments == null) {
            totalPayments = BigDecimal.ZERO.setScale(2);
        } else {
            totalPayments = totalPayments.setScale(2);
        }

        return new DashboardSummary(totalClients, totalWorkLogs, totalPayments);
    }

    @Data
    @AllArgsConstructor
    public static class DashboardSummary {
        private long totalClients;
        private long totalWorkLogs;
        private BigDecimal totalPayments;
    }
}
