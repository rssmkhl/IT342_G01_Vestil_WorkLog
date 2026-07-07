import { useEffect, useState } from 'react';
import api from '../services/api';

const emptyForm = { name: '', email: '', phone: '', company: '', notes: '' };

const Clients = () => {
  const [clients, setClients] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');

  const loadClients = async () => {
    try {
      const response = await api.get('/clients');
      setClients(response.data);
    } catch (error) {
      setMessage('Unable to load clients right now.');
    }
  };

  useEffect(() => {
    loadClients();
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      await api.post('/clients', form);
      setForm(emptyForm);
      setMessage('Client added successfully.');
      loadClients();
    } catch (error) {
      setMessage('Please enter valid client details.');
    }
  };

  return (
    <div className="page-shell">
      <div className="page-header">
        <div>
          <h2>Clients</h2>
          <p>Manage client relationships and project contacts.</p>
        </div>
      </div>
      {message ? <div className="alert success">{message}</div> : null}
      <form className="card form-card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-group">
            <label>Name</label>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>Phone</label>
            <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Company</label>
            <input value={form.company} onChange={(e) => setForm({ ...form, company: e.target.value })} />
          </div>
        </div>
        <div className="form-group">
          <label>Notes</label>
          <textarea value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} rows="3" />
        </div>
        <button className="auth-button" type="submit">Save Client</button>
      </form>
      <div className="card list-card">
        <h3>Recent Clients</h3>
        {clients.length === 0 ? <p>No clients added yet.</p> : (
          <ul className="resource-list">
            {clients.map((client) => (
              <li key={client.id}>
                <strong>{client.name}</strong>
                <span>{client.email}</span>
                <small>{client.company || 'Independent'}</small>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Clients;
