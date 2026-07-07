import { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import authService from '../services/authService';
import api from '../services/api';
import Clients from './Clients';
import Worklogs from './Worklogs';
import Payments from './Payments';
import Admin from './Admin';

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [summary, setSummary] = useState({ totalClients: 0, totalWorkLogs: 0, totalPayments: 0 });

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    api.get('/dashboard/summary').then((response) => setSummary(response.data)).catch(() => {});
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
                <div className="dashboard-header">
                  <h1>Welcome, {user?.fullName || 'User'}!</h1>
                  <p>Keep clients, work logs, and payments organized in one place.</p>
                </div>
                <div className="dashboard-cards">
                  <div className="card">
                    <h3>Total Clients</h3>
                    <p className="card-value">{summary.totalClients}</p>
                  </div>
                  <div className="card">
                    <h3>Total Work Logs</h3>
                    <p className="card-value">{summary.totalWorkLogs}</p>
                  </div>
                  <div className="card">
                    <h3>Total Payments</h3>
                    <p className="card-value">${Number(summary.totalPayments || 0).toFixed(2)}</p>
                  </div>
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
