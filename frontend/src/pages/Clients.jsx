import { useEffect, useState } from 'react';
import api from '../services/api';

const emptyForm = { name: '', email: '', phone: '', company: '', notes: '' };

const Clients = () => {
  const [clients, setClients] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [viewingClient, setViewingClient] = useState(null);

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
      if (editingId) {
        await api.put(`/clients/${editingId}`, form);
        setMessage('Client updated successfully.');
      } else {
        await api.post('/clients', form);
        setMessage('Client added successfully.');
      }
      setForm(emptyForm);
      setEditingId(null);
      loadClients();
    } catch (error) {
      setMessage('Please enter valid client details.');
    }
  };

  const handleEdit = (client) => {
    setForm({
      name: client.name,
      email: client.email,
      phone: client.phone,
      company: client.company,
      notes: client.notes
    });
    setEditingId(client.id);
    setViewingClient(null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this client?')) {
      return;
    }
    try {
      await api.delete(`/clients/${id}`);
      setMessage('Client deleted successfully.');
      loadClients();
    } catch (error) {
      setMessage('Unable to delete client.');
    }
  };

  const handleView = (client) => {
    setViewingClient(client);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
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
      
      {/* Client Details View */}
      {viewingClient && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3>Client Details</h3>
            <button onClick={() => setViewingClient(null)} className="auth-button" style={{ padding: '0.5rem 1rem' }}>Close</button>
          </div>
          <div>
            <p><strong>Name:</strong> {viewingClient.name}</p>
            <p><strong>Email:</strong> {viewingClient.email}</p>
            <p><strong>Phone:</strong> {viewingClient.phone || 'N/A'}</p>
            <p><strong>Company:</strong> {viewingClient.company || 'N/A'}</p>
            <p><strong>Notes:</strong> {viewingClient.notes || 'N/A'}</p>
          </div>
          <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
            <button onClick={() => handleEdit(viewingClient)} className="auth-button">Edit</button>
            <button onClick={() => handleDelete(viewingClient.id)} className="danger-button">Delete</button>
          </div>
        </div>
      )}

      {!viewingClient && (
        <form className="card form-card" onSubmit={handleSubmit}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3>{editingId ? 'Edit Client' : 'Add New Client'}</h3>
            {editingId && (
              <button type="button" onClick={cancelEdit} style={{ padding: '0.5rem 1rem' }}>Cancel</button>
            )}
          </div>
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
          <button className="auth-button" type="submit">{editingId ? 'Update Client' : 'Save Client'}</button>
        </form>
      )}

      <div className="card list-card">
        <h3>Recent Clients</h3>
        {clients.length === 0 ? <p>No clients added yet.</p> : (
          <ul className="resource-list">
            {clients.map((client) => (
              <li key={client.id} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong>{client.name}</strong>
                    <span>{client.email}</span>
                    <small>{client.company || 'Independent'}</small>
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button onClick={() => handleView(client)} className="auth-button" style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem' }}>View</button>
                    <button onClick={() => handleEdit(client)} className="auth-button" style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem' }}>Edit</button>
                    <button onClick={() => handleDelete(client.id)} className="danger-button" style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem' }}>Delete</button>
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

export default Clients;
