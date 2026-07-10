import { useEffect, useState } from 'react';
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
          <p>Record tasks, durations, and project progress.</p>
        </div>
      </div>
      {message ? <div className="alert success">{message}</div> : null}

      {/* Work Log Details View */}
      {viewingWorklog && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3>Work Log Details</h3>
            <button onClick={() => setViewingWorklog(null)} className="auth-button" style={{ padding: '0.5rem 1rem' }}>Close</button>
          </div>
          <div>
            <p><strong>Title:</strong> {viewingWorklog.title}</p>
            <p><strong>Project:</strong> {viewingWorklog.project || 'General'}</p>
            <p><strong>Date:</strong> {viewingWorklog.date || 'N/A'}</p>
            <p><strong>Hours:</strong> {viewingWorklog.hours || 0}h</p>
            <p><strong>Status:</strong> {viewingWorklog.status || 'In Progress'}</p>
            <p><strong>Description:</strong> {viewingWorklog.description || 'N/A'}</p>
          </div>
          <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
            <button onClick={() => handleEdit(viewingWorklog)} className="auth-button">Edit</button>
            <button onClick={() => handleDelete(viewingWorklog.id)} className="danger-button">Delete</button>
          </div>
        </div>
      )}

      {!viewingWorklog && (
        <form className="card form-card" onSubmit={handleSubmit}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3>{editingId ? 'Edit Work Log' : 'Add New Work Log'}</h3>
            {editingId && (
              <button type="button" onClick={cancelEdit} style={{ padding: '0.5rem 1rem' }}>Cancel</button>
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
        <h3>Recent Entries</h3>
        {worklogs.length === 0 ? <p>No work logs recorded yet.</p> : (
          <ul className="resource-list">
            {worklogs.map((entry) => (
              <li key={entry.id} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong>{entry.title}</strong>
                    <span>{entry.project || 'General'} • {entry.hours || 0}h</span>
                    <small>{entry.status || 'In Progress'}</small>
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button onClick={() => handleView(entry)} className="auth-button" style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem' }}>View</button>
                    <button onClick={() => handleEdit(entry)} className="auth-button" style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem' }}>Edit</button>
                    <button onClick={() => handleDelete(entry.id)} className="danger-button" style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem' }}>Delete</button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Worklogs;
