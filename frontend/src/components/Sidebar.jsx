import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const Sidebar = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h2>WorkLog</h2>
      </div>
      <nav className="sidebar-nav">
        <Link to="/dashboard" className="sidebar-link">
          Dashboard
        </Link>
        <Link to="/dashboard/clients" className="sidebar-link">
          Clients
        </Link>
        <Link to="/dashboard/worklogs" className="sidebar-link">
          Work Logs
        </Link>
        <Link to="/dashboard/payments" className="sidebar-link">
          Payments
        </Link>
        <button onClick={handleLogout} className="sidebar-link sidebar-logout">
          Logout
        </button>
      </nav>
    </div>
  );
};

export default Sidebar;
