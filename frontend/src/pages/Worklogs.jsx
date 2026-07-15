import { useEffect, useState } from 'react';
import { Eye, FileText, Pencil, Trash2 } from 'lucide-react';
import api from '../services/api';

const emptyForm = { title: '', description: '', date: '', hours: '', status: 'In Progress', project: '' };

const Worklogs = () => {
  const [worklogs, setWorklogs] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [viewingWorklog, setViewingWorklog] = useState(null);

  const loadWorklogs = async () => {
    try {
      const response = await api.get('/worklogs');
      setWorklogs(response.data);
    } catch (error) {
      setMessage('Unable to load work logs right now.');
    }
  };

  useEffect(() => {
    loadWorklogs();
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      if (editingId) {
        await api.put(`/worklogs/${editingId}`, { ...form, hours: Number(form.hours) || 0 });
        setMessage('Work log updated successfully.');
      } else {
        await api.post('/worklogs', { ...form, hours: Number(form.hours) || 0 });
        setMessage('Work log added successfully.');
      }
      setForm(emptyForm);
      setEditingId(null);
      loadWorklogs();
    } catch (error) {
      setMessage('Please enter valid work log details.');
    }
  };

  const handleEdit = (worklog) => {
    setForm({
      title: worklog.title,
      description: worklog.description,
      date: worklog.date || '',
      hours: worklog.hours || '',
      status: worklog.status || 'In Progress',
      project: worklog.project || ''
    });
    setEditingId(worklog.id);
    setViewingWorklog(null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this work log?')) {
      return;
    }
    try {
      await api.delete(`/worklogs/${id}`);
      setMessage('Work log deleted successfully.');
      loadWorklogs();
    } catch (error) {
      setMessage('Unable to delete work log.');
    }
  };

  const handleView = (worklog) => {
    setViewingWorklog(worklog);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  return (
    <div className="page-shell">
      <div className="page-header">
        <div>
          <h2>Work Logs</h2>
          <p>Log time, track progress, and keep project entries organized.</p>
        </div>
        <div className="header-actions">
          {editingId && (
            <button type="button" className="auth-button auth-button--secondary" onClick={cancelEdit}>
              Cancel Edit
            </button>
          )}
        </div>
      </div>
      {message ? <div className="alert success">{message}</div> : null}

      {viewingWorklog && (
        <div className="card detail-view-card">
          <div className="card-header">
            <div>
              <h3>Work Log Details</h3>
              <p>Focused detail view with structured fields and action choices.</p>
            </div>
            <button onClick={() => setViewingWorklog(null)} className="auth-button auth-button--secondary">
              Close
            </button>
          </div>
          <div className="detail-grid">
            <div className="detail-field">
              <p className="detail-label">Title</p>
              <p className="detail-value">{viewingWorklog.title}</p>
            </div>
            <div className="detail-field">
              <p className="detail-label">Project</p>
              <p className="detail-value">{viewingWorklog.project || 'General'}</p>
            </div>
            <div className="detail-field">
              <p className="detail-label">Date</p>
              <p className="detail-value">{viewingWorklog.date || 'N/A'}</p>
            </div>
            <div className="detail-field">
              <p className="detail-label">Hours</p>
              <p className="detail-value">{viewingWorklog.hours || 0}h</p>
            </div>
          </div>
          <div className="detail-notes">
            <p className="detail-label">Description</p>
            <p className="detail-value">{viewingWorklog.description || 'N/A'}</p>
          </div>
          <div className="card-actions">
            <button onClick={() => handleEdit(viewingWorklog)} className="auth-button">
              <Pencil size={14} /> Edit
            </button>
            <button onClick={() => handleDelete(viewingWorklog.id)} className="danger-button">
              <Trash2 size={14} /> Delete
            </button>
          </div>
        </div>
      )}

      {!viewingWorklog && (
        <form className="card form-card" onSubmit={handleSubmit}>
          <div className="card-header">
            <div>
              <h3>{editingId ? 'Edit Work Log' : 'Add New Work Log'}</h3>
              <p>{editingId ? 'Update logged work details.' : 'Create a new work log entry.'}</p>
            </div>
            {editingId && (
              <button type="button" className="auth-button auth-button--secondary" onClick={cancelEdit}>
                Cancel
              </button>
            )}
          </div>
          <div className="form-grid">
            <div className="form-group">
              <label>Title</label>
              <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Project</label>
              <input value={form.project} onChange={(e) => setForm({ ...form, project: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Date</label>
              <input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Hours</label>
              <input type="number" value={form.hours} onChange={(e) => setForm({ ...form, hours: e.target.value })} />
            </div>
          </div>
          <div className="form-group">
            <label>Status</label>
            <input value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows="3" />
          </div>
          <button className="auth-button" type="submit">{editingId ? 'Update Work Log' : 'Save Work Log'}</button>
        </form>
      )}

      <div className="card list-card">
        <div className="card-header">
          <div>
            <h3>Recent Entries</h3>
            <p>View recent work log entries at a glance.</p>
          </div>
        </div>
        {worklogs.length === 0 ? (
          <div className="empty-state">
            <FileText size={24} />
            <p>No work logs recorded yet.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Title</th>
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
                    <td>{entry.title}</td>
                    <td>{entry.project || 'General'}</td>
                    <td>{entry.date || '—'}</td>
                    <td>{entry.hours || 0}</td>
                    <td>{entry.status || 'In Progress'}</td>
                    <td>
                      <div className="action-group">
                        <button type="button" className="table-action-btn" onClick={() => handleView(entry)}>
                          <Eye size={14} /> View
                        </button>
                        <button type="button" className="table-action-btn" onClick={() => handleEdit(entry)}>
                          <Pencil size={14} /> Edit
                        </button>
                        <button type="button" className="table-action-btn table-action-btn--danger" onClick={() => handleDelete(entry.id)}>
                          <Trash2 size={14} /> Del
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default Worklogs;
