package cit.edu.vestil.worklog;

import cit.edu.vestil.worklog.repository.ClientRepository;
import cit.edu.vestil.worklog.repository.PaymentRepository;
import cit.edu.vestil.worklog.repository.WorkLogRepository;
import cit.edu.vestil.worklog.service.DashboardService;
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

        when(clientRepository.count()).thenReturn(0L);
        when(workLogRepository.count()).thenReturn(0L);
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.sumAmount()).thenReturn(BigDecimal.ZERO);

        DashboardService dashboardService = new DashboardService(clientRepository, workLogRepository, paymentRepository);
        DashboardService.DashboardSummary summary = dashboardService.getSummary();

        assertEquals(0L, summary.getTotalClients());
        assertEquals(0L, summary.getTotalWorkLogs());
        assertEquals(BigDecimal.ZERO.setScale(2), summary.getTotalPayments());
    }
}
