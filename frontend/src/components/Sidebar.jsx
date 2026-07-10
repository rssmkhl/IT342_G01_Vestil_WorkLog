import { Link, useNavigate, useLocation } from 'react-router-dom';
import authService from '../services/authService';

const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const user = authService.getCurrentUser();
  const isAdmin = user?.role === 'ADMIN';
  const isOnAdminPage = location.pathname === '/dashboard/admin';

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
        {/* Show all links if not on admin page */}
        {!isOnAdminPage ? (
          <>
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
          </>
        ) : null}
        
        {/* Always show Admin link if user is admin */}
        {isAdmin ? (
          <Link to="/dashboard/admin" className="sidebar-link">
            Admin
          </Link>
        ) : null}
        
        <button onClick={handleLogout} className="sidebar-link sidebar-logout">
          Logout
        </button>
      </nav>
    </div>
  );
};

export default Sidebar;
