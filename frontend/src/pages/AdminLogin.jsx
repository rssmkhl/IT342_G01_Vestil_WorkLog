import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import logo from '../assets/logo.svg';

const AdminLogin = () => {
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const navigate = useNavigate();

  const validate = () => {
    const newErrors = {};
    if (!formData.usernameOrEmail) newErrors.usernameOrEmail = 'Username or Email is required';
    if (!formData.password) newErrors.password = 'Password is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    setMessage('');
    try {
      const response = await authService.login(formData);
      if (response.role === 'ADMIN') {
        navigate('/dashboard/admin');
      } else {
        // clear any stored credentials and show access denied
        authService.logout();
        setMessage('Access denied: Admin privileges required. Use the user login if you are not an admin.');
        setMessageType('error');
      }
    } catch (error) {
      setMessage(error.response?.data?.message || 'Invalid username/email or password');
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-brand">
          <div className="auth-logo">
            <img src={logo} alt="WorkLog" />
          </div>
          <div>
            <h2>Admin Login</h2>
            <p className="auth-subtitle">Sign in to your admin account</p>
          </div>
        </div>
        {message && <div className={`alert ${messageType}`}>{message}</div>}
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Username or Email</label>
            <input
              type="text"
              name="usernameOrEmail"
              value={formData.usernameOrEmail}
              onChange={handleChange}
              className={errors.usernameOrEmail ? 'input-error' : ''}
            />
            {errors.usernameOrEmail && <span className="error-text">{errors.usernameOrEmail}</span>}
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className={errors.password ? 'input-error' : ''}
            />
            {errors.password && <span className="error-text">{errors.password}</span>}
          </div>
          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? 'Loading...' : 'Login as Admin'}
          </button>
        </form>
        <div className="auth-footer-links">
          <p className="auth-link">Not an admin? <Link to="/login">Sign in as user</Link></p>
        </div>
      </div>
    </div>
  );
};

export default AdminLogin;
