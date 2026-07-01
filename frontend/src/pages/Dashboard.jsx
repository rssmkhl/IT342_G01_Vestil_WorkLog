import { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import authService from '../services/authService';

const Dashboard = () => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
  }, []);

  return (
    <div className="dashboard-container">
      <Sidebar />
      <div className="dashboard-content">
        <div className="dashboard-header">
          <h1>Welcome, {user?.fullName || 'User'}!</h1>
        </div>
        <div className="dashboard-cards">
          <div className="card">
            <h3>Total Clients</h3>
            <p className="card-value">0</p>
          </div>
          <div className="card">
            <h3>Total Work Logs</h3>
            <p className="card-value">0</p>
          </div>
          <div className="card">
            <h3>Total Payments</h3>
            <p className="card-value">$0.00</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
