import { useEffect, useMemo, useState } from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import {
  Activity,
  AlertCircle,
  Archive,
  Briefcase,
  CalendarDays,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  CircleDollarSign,
  CircleOff,
  Clock3,
  Eye,
  FileText,
  Filter,
  KeyRound,
  LayoutDashboard,
  LogOut,
  Pencil,
  Search,
  ShieldCheck,
  ShieldOff,
  Sparkles,
  Trash2,
  User,
  UserPlus2,
  Users,
  Wallet,
} from 'lucide-react';
import api from '../services/api';
import authService from '../services/authService';
import MetricCard from '../components/MetricCard';
import SectionCard from '../components/SectionCard';

const chartColors = ['#4f46e5', '#0891b2', '#f59e0b', '#64748b'];

const formatCurrency = (value) =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(Number(value || 0));

const formatDate = (value) => {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const getInitials = (name) => {
  if (!name) return 'AD';
  return name
    .split(' ')
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
};

const getStatusClass = (status) => {
  const normalized = (status || '').toUpperCase();
  if (normalized === 'ACTIVE' || normalized === 'PAID' || normalized === 'APPROVED') return 'badge badge--success';
  if (normalized === 'INACTIVE' || normalized === 'PENDING') return 'badge badge--neutral';
  if (normalized === 'SUSPENDED' || normalized === 'REJECTED') return 'badge badge--warning';
  return 'badge badge--neutral';
};

const getRoleClass = (role) => {
  return role === 'ADMIN' ? 'badge badge--danger' : 'badge badge--info';
};

const Admin = () => {
  const [summary, setSummary] = useState({
    totalFreelancers: 0,
    activeClients: 0,
    workLogsThisWeek: 0,
    pendingPayments: 0,
    totalRevenue: 0,
    activeProjects: 0,
  });
  const [users, setUsers] = useState([]);
  const [clients, setClients] = useState([]);
  const [worklogs, setWorklogs] = useState([]);
  const [payments, setPayments] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [profileOpen, setProfileOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [sortBy, setSortBy] = useState('name');
  const [rowsPerPage, setRowsPerPage] = useState(8);
  const [page, setPage] = useState(1);
  const currentUser = authService.getCurrentUser();
  const isAdmin = currentUser?.role === 'ADMIN';

  const loadAdminData = async () => {
    setError('');
    setLoading(true);
    try {
      const [summaryResponse, usersResponse, clientsResponse, worklogsResponse, paymentsResponse, auditLogsResponse] = await Promise.all([
        api.get('/admin/summary'),
        api.get('/admin/users'),
        api.get('/admin/clients'),
        api.get('/admin/worklogs'),
        api.get('/admin/payments'),
        api.get('/admin/audit-logs'),
      ]);
      setSummary(summaryResponse.data);
      setUsers(usersResponse.data);
      setClients(clientsResponse.data);
      setWorklogs(worklogsResponse.data);
      setPayments(paymentsResponse.data);
      setAuditLogs(auditLogsResponse.data);
    } catch (error) {
      setError('Unable to load admin data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAdmin) {
      loadAdminData();
    }
  }, [isAdmin]);

  useEffect(() => {
    setPage(1);
  }, [searchTerm, roleFilter, statusFilter, rowsPerPage]);

  const adminCount = users.filter((user) => user.role === 'ADMIN').length;

  const filteredUsers = useMemo(() => {
    const term = searchTerm.toLowerCase();
    let data = [...users];

    if (term) {
      data = data.filter((user) => {
        const fullName = (user.fullName || '').toLowerCase();
        const username = (user.username || '').toLowerCase();
        const email = (user.email || '').toLowerCase();
        return fullName.includes(term) || username.includes(term) || email.includes(term);
      });
    }

    if (roleFilter !== 'all') {
      data = data.filter((user) => user.role === roleFilter.toUpperCase());
    }

    if (statusFilter !== 'all') {
      data = data.filter((user) => (user.status || '').toUpperCase() === statusFilter.toUpperCase());
    }

    data.sort((left, right) => {
      if (sortBy === 'date') {
        return new Date(right.createdAt || 0) - new Date(left.createdAt || 0);
      }
      if (sortBy === 'login') {
        return new Date(right.lastLogin || 0) - new Date(left.lastLogin || 0);
      }
      return (left.fullName || '').localeCompare(right.fullName || '');
    });

    return data;
  }, [users, searchTerm, roleFilter, statusFilter, sortBy]);

  const totalPages = Math.max(1, Math.ceil(filteredUsers.length / rowsPerPage));
  const pagedUsers = filteredUsers.slice((page - 1) * rowsPerPage, page * rowsPerPage);

  const weeklyHoursData = useMemo(() => {
    const last7Days = Array.from({ length: 7 }, (_, index) => {
      const date = new Date();
      date.setDate(date.getDate() - (6 - index));
      return date.toISOString().slice(0, 10);
    });

    return last7Days.map((day) => {
      const total = worklogs.reduce((sum, entry) => {
        const entryDate = String(entry.date || '').slice(0, 10);
        return entryDate === day ? sum + Number(entry.hours || 0) : sum;
      }, 0);

      return {
        name: new Date(day).toLocaleDateString('en-US', { weekday: 'short' }),
        hours: total,
      };
    });
  }, [worklogs]);

  const paymentStatusData = useMemo(() => {
    const counts = payments.reduce(
      (acc, payment) => {
        const status = (payment.status || 'PENDING').toUpperCase();
        acc[status] = (acc[status] || 0) + 1;
        return acc;
      },
      {}
    );

    return [
      { name: 'Pending', value: counts.PENDING || 0 },
      { name: 'Paid', value: counts.PAID || 0 },
      { name: 'Rejected', value: counts.REJECTED || 0 },
    ].filter((item) => item.value > 0);
  }, [payments]);

  const monthlyRevenueData = useMemo(() => {
    const buckets = {};
    payments.forEach((payment) => {
      if ((payment.status || '').toUpperCase() !== 'PAID') return;
      const month = new Date(payment.paymentDate || new Date()).toLocaleDateString('en-US', { month: 'short' });
      buckets[month] = (buckets[month] || 0) + Number(payment.amount || 0);
    });

    return Object.entries(buckets).map(([name, value]) => ({ name, revenue: value }));
  }, [payments]);

  const recentActivity = useMemo(() => {
    return [...auditLogs].slice(0, 6);
  }, [auditLogs]);

  const handleDeleteUser = async (user) => {
    if (!window.confirm(`Soft-delete ${user.fullName || user.username}? This will disable the account without removing the record.`)) {
      return;
    }

    setMessage('');
    setError('');
    try {
      await api.delete(`/admin/users/${user.id}`);
      setMessage('User account disabled successfully.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to disable this user.');
    }
  };

  const handleResetPassword = async (user) => {
    setMessage('');
    setError('');
    try {
      const response = await api.post(`/admin/users/${user.id}/reset-password`);
      setMessage(`Temporary password generated: ${response.data}`);
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to reset the password.');
    }
  };

  const handleToggleUserStatus = async (user) => {
    if (user.role === 'ADMIN' && adminCount <= 1) {
      setError('The last remaining admin cannot be changed.');
      return;
    }

    setMessage('');
    setError('');
    try {
      await api.post(`/admin/users/${user.id}/toggle-status`);
      setMessage('User status updated.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to change the user status.');
    }
  };

  const handleEditUser = async (user) => {
    const fullName = window.prompt('Enter the full name', user.fullName || '');
    if (fullName === null) return;

    const email = window.prompt('Enter the email address', user.email || '');
    if (email === null) return;

    setMessage('');
    setError('');
    try {
      await api.put(`/admin/users/${user.id}`, {
        fullName,
        email,
        username: user.username,
        role: user.role,
      });
      setMessage('User details updated.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to update this user.');
    }
  };

  const handleArchiveClient = async (client) => {
    setMessage('');
    setError('');
    try {
      await api.put(`/admin/clients/${client.id}/archive`);
      setMessage('Client archived successfully.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to archive this client.');
    }
  };

  const handleApproveWorkLog = async (entry) => {
    setMessage('');
    setError('');
    try {
      await api.put(`/admin/worklogs/${entry.id}/approve`);
      setMessage('Work log approved.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to approve this work log.');
    }
  };

  const handleRejectWorkLog = async (entry) => {
    setMessage('');
    setError('');
    try {
      await api.put(`/admin/worklogs/${entry.id}/reject`);
      setMessage('Work log rejected.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to reject this work log.');
    }
  };

  const handleApprovePayment = async (payment) => {
    setMessage('');
    setError('');
    try {
      await api.put(`/admin/payments/${payment.id}/approve`);
      setMessage('Payment approved.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to approve this payment.');
    }
  };

  const handleRejectPayment = async (payment) => {
    setMessage('');
    setError('');
    try {
      await api.put(`/admin/payments/${payment.id}/reject`);
      setMessage('Payment rejected.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to reject this payment.');
    }
  };

  const handleMarkAsPaid = async (payment) => {
    setMessage('');
    setError('');
    try {
      await api.put(`/admin/payments/${payment.id}/approve`);
      setMessage('Payment marked as paid.');
      loadAdminData();
    } catch (error) {
      setError(error.response?.data?.message || 'Unable to update this payment.');
    }
  };

  if (!isAdmin) {
    return (
      <div className="page-shell">
        <div className="page-header">
          <div>
            <h2>Admin Console</h2>
            <p>You need an administrator account to view this page.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-shell admin-shell">
      <div className="page-header admin-header">
        <div>
          <p className="eyebrow">Operations Center</p>
          <h2>Admin Dashboard</h2>
          <p>Monitor activity, manage users, and keep the freelance operation running smoothly.</p>
        </div>
        <div className="admin-profile">
          <button className="admin-profile__toggle" type="button" onClick={() => setProfileOpen((open) => !open)}>
            <div className="avatar avatar--sm">{getInitials(currentUser?.fullName || 'Admin')}</div>
            <div className="admin-profile__meta">
              <strong>{currentUser?.fullName || 'Administrator'}</strong>
              <span>Admin</span>
            </div>
          </button>
          {profileOpen ? (
            <div className="admin-profile__menu">
              <button type="button" className="admin-profile__item">
                <User size={16} /> Profile
              </button>
              <button type="button" className="admin-profile__item">
                <KeyRound size={16} /> Change Password
              </button>
              <button type="button" className="admin-profile__item admin-profile__item--danger">
                <LogOut size={16} /> Logout
              </button>
            </div>
          ) : null}
        </div>
      </div>

      {message ? <div className="alert success">{message}</div> : null}
      {error ? <div className="alert error">{error}</div> : null}

      <div className="metric-grid">
        <MetricCard
          title="Total Freelancers"
          value={loading ? '—' : summary.totalFreelancers}
          description="Registered professionals"
          icon={Users}
          accentClass="metric-card--blue"
          loading={loading}
        />
        <MetricCard
          title="Active Clients"
          value={loading ? '—' : summary.activeClients}
          description="Currently engaged"
          icon={Users}
          accentClass="metric-card--teal"
          loading={loading}
        />
        <MetricCard
          title="Work Logs This Week"
          value={loading ? '—' : summary.workLogsThisWeek}
          description="Entries submitted"
          icon={Clock3}
          accentClass="metric-card--amber"
          loading={loading}
        />
        <MetricCard
          title="Pending Payments"
          value={loading ? '—' : summary.pendingPayments}
          description="Awaiting approval"
          icon={Wallet}
          accentClass="metric-card--violet"
          loading={loading}
        />
        <MetricCard
          title="Total Revenue"
          value={loading ? '—' : formatCurrency(summary.totalRevenue)}
          description="Paid across projects"
          icon={CircleDollarSign}
          accentClass="metric-card--slate"
          loading={loading}
        />
      </div>

      <div className="analytics-grid">
        <SectionCard title="Weekly Work Hours" subtitle="Weekly effort overview">
          <div className="chart-card">
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={weeklyHoursData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="name" tickLine={false} axisLine={false} />
                <YAxis tickLine={false} axisLine={false} />
                <Tooltip />
                <Bar dataKey="hours" radius={[8, 8, 0, 0]} fill="#4f46e5" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>

        <SectionCard title="Payment Status" subtitle="Current distribution">
          <div className="chart-card">
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={paymentStatusData} dataKey="value" nameKey="name" innerRadius={60} outerRadius={90} paddingAngle={4}>
                  {paymentStatusData.map((entry, index) => (
                    <Cell key={`${entry.name}-${index}`} fill={chartColors[index % chartColors.length]} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>

        <SectionCard title="Monthly Revenue" subtitle="Paid invoices generated">
          <div className="chart-card">
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={monthlyRevenueData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="name" tickLine={false} axisLine={false} />
                <YAxis tickLine={false} axisLine={false} />
                <Tooltip formatter={(value) => formatCurrency(value)} />
                <Bar dataKey="revenue" radius={[8, 8, 0, 0]} fill="#0f766e" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>
      </div>

      <div className="content-grid">
        <SectionCard title="Recent Activity" subtitle="Latest actions in the system">
          <div className="activity-list">
            {recentActivity.length ? (
              recentActivity.map((entry, index) => (
                <div className="activity-item" key={`${entry.action}-${index}`}>
                  <div className="activity-icon">
                    <Activity size={18} />
                  </div>
                  <div className="activity-copy">
                    <strong>{entry.action || 'Activity recorded'}</strong>
                    <p>{entry.targetRecord || 'System activity'}</p>
                  </div>
                  <span className="activity-time">{formatDate(entry.timestamp)}</span>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <AlertCircle size={24} />
                <p>No recent activity tracked yet.</p>
              </div>
            )}
          </div>
        </SectionCard>

        <SectionCard title="Registered Users" subtitle="Manage freelancer and admin accounts">
          <div className="toolbar">
            <label className="toolbar__search">
              <Search size={16} />
              <input type="text" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Search users" />
            </label>
            <label className="toolbar__select">
              <Filter size={16} />
              <select value={roleFilter} onChange={(event) => setRoleFilter(event.target.value)}>
                <option value="all">All roles</option>
                <option value="admin">Admin</option>
                <option value="freelancer">Freelancer</option>
              </select>
            </label>
            <label className="toolbar__select">
              <ShieldCheck size={16} />
              <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                <option value="all">All statuses</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
                <option value="suspended">Suspended</option>
              </select>
            </label>
            <label className="toolbar__select">
              <CalendarDays size={16} />
              <select value={sortBy} onChange={(event) => setSortBy(event.target.value)}>
                <option value="name">Sort by name</option>
                <option value="date">Sort by date</option>
                <option value="login">Sort by login</option>
              </select>
            </label>
          </div>

          {loading ? (
            <div className="table-skeleton">Loading user records...</div>
          ) : filteredUsers.length ? (
            <>
              <div className="table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Profile</th>
                      <th>Full Name</th>
                      <th>Username</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th>Date</th>
                      <th>Last Login</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pagedUsers.map((user) => (
                      <tr key={user.id}>
                        <td>
                          <div className="avatar">{getInitials(user.fullName || user.username)}</div>
                        </td>
                        <td>{user.fullName || '—'}</td>
                        <td>{user.username || '—'}</td>
                        <td>{user.email || '—'}</td>
                        <td><span className={getRoleClass(user.role)}>{(user.role || 'FREELANCER').toLowerCase()}</span></td>
                        <td><span className={getStatusClass(user.status)}>{(user.status || 'ACTIVE').toLowerCase()}</span></td>
                        <td>{formatDate(user.createdAt)}</td>
                        <td>{formatDate(user.lastLogin)}</td>
                        <td>
                          <div className="action-group">
                            <button type="button" className="icon-btn" onClick={() => handleEditUser(user)}><Pencil size={14} /></button>
                            <button type="button" className="icon-btn" onClick={() => handleResetPassword(user)}><KeyRound size={14} /></button>
                            <button type="button" className="icon-btn" onClick={() => handleToggleUserStatus(user)}><ShieldOff size={14} /></button>
                            <button type="button" className="icon-btn danger" onClick={() => handleDeleteUser(user)}><Trash2 size={14} /></button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="pagination">
                <button type="button" className="pagination__btn" onClick={() => setPage((current) => Math.max(1, current - 1))} disabled={page === 1}>
                  <ChevronLeft size={16} /> Previous
                </button>
                <span>
                  Page {page} of {totalPages}
                </span>
                <label className="toolbar__select toolbar__select--compact">
                  <select value={rowsPerPage} onChange={(event) => setRowsPerPage(Number(event.target.value))}>
                    <option value={8}>8 rows</option>
                    <option value={12}>12 rows</option>
                    <option value={16}>16 rows</option>
                  </select>
                </label>
                <button type="button" className="pagination__btn" onClick={() => setPage((current) => Math.min(totalPages, current + 1))} disabled={page === totalPages}>
                  Next <ChevronRight size={16} />
                </button>
              </div>
            </>
          ) : (
            <div className="empty-state">
              <Users size={24} />
              <p>No matching users were found.</p>
            </div>
          )}
        </SectionCard>
      </div>

      <div className="content-grid content-grid--stacked">
        <SectionCard title="Clients" subtitle="Manage business relationships">
          {clients.length ? (
            <div className="table-wrap">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Company</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Status</th>
                    <th>Projects</th>
                    <th>Added By</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {clients.map((client) => (
                    <tr key={client.id}>
                      <td>{client.name}</td>
                      <td>{client.company || '—'}</td>
                      <td>{client.email || '—'}</td>
                      <td>{client.phone || '—'}</td>
                      <td><span className={getStatusClass(client.status)}>{(client.status || 'ACTIVE').toLowerCase()}</span></td>
                      <td>{client.projectCount || 0}</td>
                      <td>{client.userName || '—'}</td>
                      <td>
                        <div className="action-group">
                          <button type="button" className="icon-btn"><Eye size={14} /></button>
                          <button type="button" className="icon-btn"><Pencil size={14} /></button>
                          <button type="button" className="icon-btn" onClick={() => handleArchiveClient(client)}><Archive size={14} /></button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">
              <Briefcase size={24} />
              <p>No clients available. Add one to get started.</p>
            </div>
          )}
        </SectionCard>

        <SectionCard title="Work Logs" subtitle="Review submitted hours and approvals">
          <div className="toolbar toolbar--compact">
            <label className="toolbar__select">
              <select defaultValue="all">
                <option value="all">All statuses</option>
                <option value="pending">Pending</option>
                <option value="approved">Approved</option>
                <option value="rejected">Rejected</option>
              </select>
            </label>
          </div>
          {worklogs.length ? (
            <div className="table-wrap">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Freelancer</th>
                    <th>Client</th>
                    <th>Project</th>
                    <th>Date</th>
                    <th>Hours</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {worklogs.map((entry) => (
                    <tr key={entry.id}>
                      <td>{entry.userName || '—'}</td>
                      <td>{entry.clientName || '—'}</td>
                      <td>{entry.project || 'General'}</td>
                      <td>{formatDate(entry.date)}</td>
                      <td>{entry.hours || 0}</td>
                      <td><span className={getStatusClass(entry.status)}>{(entry.status || 'PENDING').toLowerCase()}</span></td>
                      <td>
                        <div className="action-group">
                          <button type="button" className="icon-btn"><Eye size={14} /></button>
                          <button type="button" className="icon-btn" onClick={() => handleApproveWorkLog(entry)}><CheckCircle2 size={14} /></button>
                          <button type="button" className="icon-btn" onClick={() => handleRejectWorkLog(entry)}><CircleOff size={14} /></button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">
              <FileText size={24} />
              <p>No work logs found.</p>
            </div>
          )}
        </SectionCard>

        <SectionCard title="Payments" subtitle="Approve and track settlement status">
          {payments.length ? (
            <div className="table-wrap">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Amount</th>
                    <th>Freelancer</th>
                    <th>Client</th>
                    <th>Method</th>
                    <th>Reference</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {payments.map((payment) => (
                    <tr key={payment.id}>
                      <td>{formatCurrency(payment.amount)}</td>
                      <td>{payment.userName || '—'}</td>
                      <td>{payment.clientName || '—'}</td>
                      <td>{payment.method || 'Cash'}</td>
                      <td>{payment.reference || '—'}</td>
                      <td>{formatDate(payment.paymentDate)}</td>
                      <td><span className={getStatusClass(payment.status)}>{(payment.status || 'PENDING').toLowerCase()}</span></td>
                      <td>
                        <div className="action-group">
                          <button type="button" className="icon-btn"><Eye size={14} /></button>
                          <button type="button" className="icon-btn" onClick={() => handleApprovePayment(payment)}><CheckCircle2 size={14} /></button>
                          <button type="button" className="icon-btn" onClick={() => handleRejectPayment(payment)}><CircleOff size={14} /></button>
                          <button type="button" className="icon-btn" onClick={() => handleMarkAsPaid(payment)}><Wallet size={14} /></button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">
              <Wallet size={24} />
              <p>No pending payments.</p>
            </div>
          )}
        </SectionCard>

        <SectionCard title="Audit Logs" subtitle="Read-only history of administrative actions">
          {auditLogs.length ? (
            <div className="table-wrap">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>Admin</th>
                    <th>Action</th>
                    <th>Target Record</th>
                  </tr>
                </thead>
                <tbody>
                  {auditLogs.map((entry, index) => (
                    <tr key={`${entry.timestamp}-${index}`}>
                      <td>{formatDate(entry.timestamp)}</td>
                      <td>{entry.adminName || 'System'}</td>
                      <td>{entry.action || 'Activity'}</td>
                      <td>{entry.targetRecord || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">
              <ShieldCheck size={24} />
              <p>No audit logs recorded yet.</p>
            </div>
          )}
        </SectionCard>
      </div>
    </div>
  );
};

export default Admin;
