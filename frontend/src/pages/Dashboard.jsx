import { useEffect, useRef, useState } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import { CircleDollarSign, Clock3, FileText, PlusCircle, Sparkles, Users, Wallet } from 'lucide-react';
import Sidebar from '../components/Sidebar';
import authService from '../services/authService';
import api from '../services/api';
import Clients from './Clients';
import Worklogs from './Worklogs';
import Payments from './Payments';
import Admin from './Admin';
import MetricCard from '../components/MetricCard';
import SectionCard from '../components/SectionCard';

const formatCurrency = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(Number(value || 0));

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [summary, setSummary] = useState({ totalClients: 0, totalWorkLogs: 0, totalPayments: 0 });
  const [recentClients, setRecentClients] = useState([]);
  const [recentWorklogs, setRecentWorklogs] = useState([]);
  const [recentPayments, setRecentPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const hasLoadedDashboard = useRef(false);

  useEffect(() => {
    if (hasLoadedDashboard.current) {
      return;
    }

    hasLoadedDashboard.current = true;
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);

    const loadDashboardData = async () => {
      setLoading(true);
      try {
        const [summaryResponse, clientsResponse, worklogsResponse, paymentsResponse] = await Promise.all([
          api.get('/dashboard/summary'),
          api.get('/clients'),
          api.get('/worklogs'),
          api.get('/payments'),
        ]);

        setSummary(summaryResponse.data);
        setRecentClients(clientsResponse.data.slice(0, 4));
        setRecentWorklogs(worklogsResponse.data.slice(0, 4));
        setRecentPayments(paymentsResponse.data.slice(0, 4));
      } catch (error) {
        // ignore UI load failures for now
      } finally {
        setLoading(false);
      }
    };

    loadDashboardData();
  }, []);

  return (
    <div className="dashboard-container">
      <Sidebar />
      <div className="dashboard-content">
        <Routes>
          <Route
            path=""
            element={
              <>
                <div className="dashboard-header user-header">
                  <div>
                    <p className="eyebrow">Freelancer Dashboard</p>
                    <h1>Welcome back, {user?.fullName || 'User'}.</h1>
                    <p className="page-description">Your client, work log, and payment summary in one place.</p>
                  </div>
                  <div className="header-actions">
                    <button type="button" className="quick-action-btn" onClick={() => navigate('/dashboard/worklogs')}>
                      <PlusCircle size={18} /> Log Work
                    </button>
                    <button type="button" className="quick-action-btn" onClick={() => navigate('/dashboard/payments')}>
                      <Wallet size={18} /> Record Payment
                    </button>
                  </div>
                </div>

                <div className="metric-grid">
                  <MetricCard
                    title="Total Clients"
                    value={summary.totalClients}
                    description="Active clients"
                    icon={Users}
                    accentClass="metric-card--teal"
                    loading={loading}
                  />
                  <MetricCard
                    title="Work Logs"
                    value={summary.totalWorkLogs}
                    description="All submitted entries"
                    icon={Clock3}
                    accentClass="metric-card--amber"
                    loading={loading}
                  />
                  <MetricCard
                    title="Payments"
                    value={formatCurrency(summary.totalPayments)}
                    description="Total paid"
                    icon={CircleDollarSign}
                    accentClass="metric-card--slate"
                    loading={loading}
                  />
                </div>

                <div className="content-grid">
                  <SectionCard title="Latest Clients" subtitle="Recently added client contacts">
                    {recentClients.length ? (
                      <div className="table-wrap">
                        <table className="admin-table">
                          <thead>
                            <tr>
                              <th>Name</th>
                              <th>Company</th>
                              <th>Email</th>
                            </tr>
                          </thead>
                          <tbody>
                            {recentClients.map((client) => (
                              <tr key={client.id}>
                                <td>{client.name || '—'}</td>
                                <td>{client.company || '—'}</td>
                                <td>{client.email || '—'}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <div className="empty-state">
                        <Users size={24} />
                        <p>No client activity yet.</p>
                      </div>
                    )}
                  </SectionCard>

                  <SectionCard title="Recent Work Logs" subtitle="Most recent task entries">
                    {recentWorklogs.length ? (
                      <div className="table-wrap">
                        <table className="admin-table">
                          <thead>
                            <tr>
                              <th>Project</th>
                              <th>Date</th>
                              <th>Hours</th>
                            </tr>
                          </thead>
                          <tbody>
                            {recentWorklogs.map((entry) => (
                              <tr key={entry.id}>
                                <td>{entry.project || 'General'}</td>
                                <td>{entry.date || '—'}</td>
                                <td>{entry.hours || 0}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <div className="empty-state">
                        <FileText size={24} />
                        <p>No work logs to show.</p>
                      </div>
                    )}
                  </SectionCard>
                </div>

                <div className="content-grid content-grid--stacked">
                  <SectionCard title="Recent Payments" subtitle="Latest payment activity">
                    {recentPayments.length ? (
                      <div className="table-wrap">
                        <table className="admin-table">
                          <thead>
                            <tr>
                              <th>Amount</th>
                              <th>Client</th>
                              <th>Status</th>
                            </tr>
                          </thead>
                          <tbody>
                            {recentPayments.map((payment) => (
                              <tr key={payment.id}>
                                <td>{formatCurrency(payment.amount)}</td>
                                <td>{payment.client?.name || '—'}</td>
                                <td>{payment.status || 'Pending'}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <div className="empty-state">
                        <Sparkles size={24} />
                        <p>No payments recorded yet.</p>
                        <button type="button" className="auth-button" onClick={() => navigate('/dashboard/payments')}>
                          Add a payment
                        </button>
                      </div>
                    )}
                  </SectionCard>
                </div>
              </>
            }
          />
          <Route path="clients" element={<Clients />} />
          <Route path="worklogs" element={<Worklogs />} />
          <Route path="payments" element={<Payments />} />
          <Route path="admin" element={<Admin />} />
        </Routes>
      </div>
    </div>
  );
};

export default Dashboard;
