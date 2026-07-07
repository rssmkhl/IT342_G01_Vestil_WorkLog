import { useEffect, useState } from 'react';
import api from '../services/api';

const emptyForm = { title: '', description: '', date: '', hours: '', status: 'In Progress', project: '' };

const Worklogs = () => {
  const [worklogs, setWorklogs] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');

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
      await api.post('/worklogs', { ...form, hours: Number(form.hours) || 0 });
      setForm(emptyForm);
      setMessage('Work log added successfully.');
      loadWorklogs();
    } catch (error) {
      setMessage('Please enter valid work log details.');
    }
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
      <form className="card form-card" onSubmit={handleSubmit}>
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
        <button className="auth-button" type="submit">Save Work Log</button>
      </form>
      <div className="card list-card">
        <h3>Recent Entries</h3>
        {worklogs.length === 0 ? <p>No work logs recorded yet.</p> : (
          <ul className="resource-list">
            {worklogs.map((entry) => (
              <li key={entry.id}>
                <strong>{entry.title}</strong>
                <span>{entry.project || 'General'} • {entry.hours || 0}h</span>
                <small>{entry.status || 'In Progress'}</small>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Worklogs;
