import { useEffect, useState } from 'react';
import api from '../services/api';
import authService from '../services/authService';

const Admin = () => {
  const [summary, setSummary] = useState({ totalUsers: 0, totalClients: 0, totalWorkLogs: 0, pendingPayments: 0 });
  const [users, setUsers] = useState([]);
  const [clients, setClients] = useState([]);
  const [worklogs, setWorklogs] = useState([]);
  const [payments, setPayments] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const currentUser = authService.getCurrentUser();
  const isAdmin = currentUser?.role === 'ADMIN';

  const loadAdminData = async () => {
    setError('');
    try {
      const [summaryResponse, usersResponse, clientsResponse, worklogsResponse, paymentsResponse] = await Promise.all([
        api.get('/admin/summary'),
        api.get('/admin/users'),
        api.get('/admin/clients'),
        api.get('/admin/worklogs'),
        api.get('/admin/payments'),
      ]);
      setSummary(summaryResponse.data);
      setUsers(usersResponse.data);
      setClients(clientsResponse.data);
      setWorklogs(worklogsResponse.data);
      setPayments(paymentsResponse.data);
    } catch (error) {
      setError('Unable to load admin data.');
    }
  };

  useEffect(() => {
    if (isAdmin) {
      loadAdminData();
    }
  }, [isAdmin]);

  const handleDeleteUser = async (user) => {
    if (!window.confirm(`Delete ${user.fullName || user.username}? This also removes their clients, work logs, and payments.`)) {
      return;
    }

    setMessage('');
    setError('');
    try {
      await api.delete(`/admin/users/${user.id}`);
      setMessage('User account deleted.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to delete this user.');
    }
  };

  if (!isAdmin) {
    return (
      <div className="page-shell">
        <div className="page-header">
          <div>
            <h2>Admin</h2>
            <p>You need an administrator account to view this page.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <div className="page-header">
        <div>
          <h2>Admin</h2>
          <p>Manage users and monitor system activity.</p>
        </div>
      </div>

      {message ? <div className="alert success">{message}</div> : null}
      {error ? <div className="alert error">{error}</div> : null}

      <div className="dashboard-cards">
        <div className="card">
          <h3>Total Users</h3>
          <p className="card-value">{summary.totalUsers}</p>
        </div>
        <div className="card">
          <h3>Total Clients</h3>
          <p className="card-value">{summary.totalClients}</p>
        </div>
        <div className="card">
          <h3>Total Work Logs</h3>
          <p className="card-value">{summary.totalWorkLogs}</p>
        </div>
        <div className="card">
          <h3>Pending Payments</h3>
          <p className="card-value">{summary.pendingPayments}</p>
        </div>
      </div>

      <div className="card list-card">
        <h3>Registered Users</h3>
        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.fullName}</td>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.role}</td>
                  <td>
                    <button
                      className="danger-button"
                      type="button"
                      onClick={() => handleDeleteUser(user)}
                      disabled={user.id === currentUser?.id}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card list-card">
        <h3>All Clients</h3>
        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Company</th>
                <th>Added By</th>
              </tr>
            </thead>
            <tbody>
              {clients.map((client) => (
                <tr key={client.id}>
                  <td>{client.name}</td>
                  <td>{client.email}</td>
                  <td>{client.phone || 'N/A'}</td>
                  <td>{client.company || 'N/A'}</td>
                  <td>{client.userName}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card list-card">
        <h3>All Work Logs</h3>
        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>User</th>
                <th>Client</th>
                <th>Project</th>
                <th>Date</th>
                <th>Hours</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {worklogs.map((entry) => (
                <tr key={entry.id}>
                  <td>{entry.title}</td>
                  <td>{entry.userName}</td>
                  <td>{entry.clientName}</td>
                  <td>{entry.project || 'General'}</td>
                  <td>{entry.date || 'No date'}</td>
                  <td>{entry.hours || 0}</td>
                  <td>{entry.status || 'In Progress'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card list-card">
        <h3>Payment Status</h3>
        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Amount</th>
                <th>Status</th>
                <th>User</th>
                <th>Client</th>
                <th>Method</th>
                <th>Date</th>
                <th>Reference</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((payment) => (
                <tr key={payment.id}>
                  <td>${Number(payment.amount || 0).toFixed(2)}</td>
                  <td>{payment.status || 'Pending'}</td>
                  <td>{payment.userName}</td>
                  <td>{payment.clientName}</td>
                  <td>{payment.method || 'Cash'}</td>
                  <td>{payment.paymentDate || 'No date'}</td>
                  <td>{payment.reference || 'No reference'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Admin;
