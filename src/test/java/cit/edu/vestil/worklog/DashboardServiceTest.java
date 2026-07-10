package cit.edu.vestil.worklog;

import cit.edu.vestil.worklog.client.ClientRepository;
import cit.edu.vestil.worklog.common.entity.User;
import cit.edu.vestil.worklog.dashboard.DashboardService;
import cit.edu.vestil.worklog.payment.PaymentRepository;
import cit.edu.vestil.worklog.worklog.WorkLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Test
    void shouldReturnZeroSummaryWhenNoEntitiesExist() {
        ClientRepository clientRepository = Mockito.mock(ClientRepository.class);
        WorkLogRepository workLogRepository = Mockito.mock(WorkLogRepository.class);
        PaymentRepository paymentRepository = Mockito.mock(PaymentRepository.class);
        User mockUser = Mockito.mock(User.class);

        when(clientRepository.countByUser(mockUser)).thenReturn(0L);
        when(workLogRepository.countByUser(mockUser)).thenReturn(0L);
        when(paymentRepository.sumAmountByUser(mockUser)).thenReturn(BigDecimal.ZERO);

        DashboardService dashboardService = new DashboardService(clientRepository, workLogRepository, paymentRepository);
        DashboardService.DashboardSummary summary = dashboardService.getSummary(mockUser);

        assertEquals(0L, summary.getTotalClients());
        assertEquals(0L, summary.getTotalWorkLogs());
        assertEquals(BigDecimal.ZERO.setScale(2), summary.getTotalPayments());
    }
}
