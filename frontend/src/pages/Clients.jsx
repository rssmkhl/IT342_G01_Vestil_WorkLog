import { useEffect, useState } from 'react';
import { Eye, Pencil, Trash2, Users } from 'lucide-react';
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
          <p>Manage client relationships and business contacts.</p>
        </div>
        <div className="header-actions">
          {editingId ? (
            <button type="button" className="auth-button auth-button--secondary" onClick={cancelEdit}>
              Cancel Edit
            </button>
          ) : null}
        </div>
      </div>
      {message ? <div className="alert success">{message}</div> : null}

      {viewingClient && (
        <div className="card detail-view-card">
          <div className="card-header">
            <div>
              <h3>Client Details</h3>
              <p>Review full client information with a clean, focused view.</p>
            </div>
            <button onClick={() => setViewingClient(null)} className="auth-button auth-button--secondary">
              Close
            </button>
          </div>
          <div className="detail-grid">
            <div className="detail-field">
              <p className="detail-label">Name</p>
              <p className="detail-value">{viewingClient.name}</p>
            </div>
            <div className="detail-field">
              <p className="detail-label">Email</p>
              <p className="detail-value">{viewingClient.email}</p>
            </div>
            <div className="detail-field">
              <p className="detail-label">Phone</p>
              <p className="detail-value">{viewingClient.phone || 'N/A'}</p>
            </div>
            <div className="detail-field">
              <p className="detail-label">Company</p>
              <p className="detail-value">{viewingClient.company || 'N/A'}</p>
            </div>
          </div>
          <div className="detail-notes">
            <p className="detail-label">Notes</p>
            <p className="detail-value">{viewingClient.notes || 'N/A'}</p>
          </div>
          <div className="card-actions">
            <button onClick={() => handleEdit(viewingClient)} className="auth-button">Edit</button>
            <button onClick={() => handleDelete(viewingClient.id)} className="danger-button">Delete</button>
          </div>
        </div>
      )}

      {!viewingClient && (
        <form className="card form-card" onSubmit={handleSubmit}>
          <div className="card-header">
            <div>
              <h3>{editingId ? 'Edit Client' : 'Add New Client'}</h3>
              <p>{editingId ? 'Update client contact details.' : 'Create a new client record.'}</p>
            </div>
            {editingId ? (
              <button type="button" className="auth-button auth-button--secondary" onClick={cancelEdit}>
                Cancel
              </button>
            ) : null}
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
        <div className="card-header">
          <div>
            <h3>Recent Clients</h3>
            <p>Latest client contacts added to your workspace.</p>
          </div>
        </div>
        {clients.length === 0 ? (
          <div className="empty-state">
            <Users size={24} />
            <p>No clients added yet.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Company</th>
                  <th style={{ width: '180px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {clients.map((client) => (
                  <tr key={client.id}>
                    <td>{client.name}</td>
                    <td>{client.email}</td>
                    <td>{client.company || 'Independent'}</td>
                    <td>
                      <div className="action-group">
                        <button type="button" className="table-action-btn" onClick={() => handleView(client)}>
                          <Eye size={14} /> View
                        </button>
                        <button type="button" className="table-action-btn" onClick={() => handleEdit(client)}>
                          <Pencil size={14} /> Edit
                        </button>
                        <button type="button" className="table-action-btn table-action-btn--danger" onClick={() => handleDelete(client.id)}>
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

export default Clients;
