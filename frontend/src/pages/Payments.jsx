import { useEffect, useState } from 'react';
import api from '../services/api';

const emptyForm = { id: null, clientId: '', amount: '', method: 'Cash', status: 'Pending', reference: '' };

const Payments = () => {
  const [payments, setPayments] = useState([]);
  const [clients, setClients] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const loadPayments = async () => {
    try {
      const response = await api.get('/payments');
      setPayments(response.data);
    } catch (error) {
      setMessage('Unable to load payments right now.');
    }
  };

  const loadClients = async () => {
    try {
      const response = await api.get('/clients');
      setClients(response.data);
    } catch (error) {
      setError('Unable to load clients right now.');
    }
  };

  useEffect(() => {
    loadPayments();
    loadClients();
  }, []);

  const handleEdit = (payment) => {
    setEditingId(payment.id);
    setForm({
      id: payment.id,
      clientId: payment.client?.id || '',
      amount: payment.amount,
      method: payment.method || 'Cash',
      status: payment.status || 'Pending',
      reference: payment.reference || ''
    });
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this payment?')) {
      try {
        await api.delete(`/payments/${id}`);
        setMessage('Payment deleted successfully!');
        loadPayments();
      } catch (error) {
        setError('Failed to delete payment.');
      }
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setForm(emptyForm);
    setError('');
    setMessage('');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');

    const amountValue = Number(form.amount);
    if (!form.amount || Number.isNaN(amountValue) || amountValue <= 0) {
      setError('Amount must be a positive number.');
      return;
    }

    if (!form.clientId) {
      setError('Please select a client.');
      return;
    }

    if (!form.method) {
      setError('Please select a payment method.');
      return;
    }

    if (!form.status) {
      setError('Please select a payment status.');
      return;
    }

    setIsSubmitting(true);
    try {
      if (editingId) {
        await api.put(`/payments/${editingId}`, {
          amount: amountValue,
          method: form.method,
          status: form.status,
          reference: form.reference,
          client: { id: Number(form.clientId) },
        });
        setMessage('Payment updated successfully!');
      } else {
        await api.post('/payments', {
          amount: amountValue,
          method: form.method,
          status: form.status,
          reference: form.reference,
          client: { id: Number(form.clientId) },
        });
        setMessage('Payment recorded successfully.');
      }
      setEditingId(null);
      setForm(emptyForm);
      loadPayments();
    } catch (submitError) {
      setError(editingId ? 'Failed to update payment. Please try again.' : 'Failed to record payment. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="page-shell">
      <div className="page-header">
        <div>
          <h2>Payments</h2>
          <p>Track received payments and outstanding balances.</p>
        </div>
      </div>
      {message ? <div className="alert success">{message}</div> : null}
      {error ? <div className="alert error">{error}</div> : null}
      <form className="card form-card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-group">
            <label>Client</label>
            <select value={form.clientId} onChange={(e) => setForm({ ...form, clientId: e.target.value })} required>
              <option value="">Select client</option>
              {clients.map((client) => (
                <option key={client.id} value={client.id}>
                  {client.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Amount</label>
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={form.amount}
              onChange={(e) => setForm({ ...form, amount: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Method</label>
            <select value={form.method} onChange={(e) => setForm({ ...form, method: e.target.value })} required>
              <option value="">Select method</option>
              <option value="Cash">Cash</option>
              <option value="GCash">GCash</option>
              <option value="Maya">Maya</option>
              <option value="Bank Transfer">Bank Transfer</option>
              <option value="PayPal">PayPal</option>
            </select>
          </div>
          <div className="form-group">
            <label>Status</label>
            <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} required>
              <option value="">Select status</option>
              <option value="Pending">Pending</option>
              <option value="Paid">Paid</option>
              <option value="Partially Paid">Partially Paid</option>
            </select>
          </div>
          <div className="form-group">
            <label>Reference No. (Optional)</label>
            <input
              value={form.reference}
              onChange={(e) => setForm({ ...form, reference: e.target.value })}
              placeholder="Transaction ID or Receipt No."
            />
          </div>
        </div>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <button className="auth-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Saving...' : editingId ? 'Update Payment' : 'Record Payment'}
          </button>
          {editingId && (
            <button type="button" onClick={handleCancel} style={{ padding: '10px 20px', borderRadius: '4px', border: '1px solid #ccc', cursor: 'pointer' }}>
              Cancel
            </button>
          )}
        </div>
      </form>
      <div className="card list-card">
        <h3>Payment History</h3>
        {payments.length === 0 ? <p>No payments recorded yet.</p> : (
          <ul className="resource-list">
            {payments.map((payment) => (
              <li key={payment.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <strong>${Number(payment.amount || 0).toFixed(2)}</strong>
                  <span>{payment.client?.name || 'No client'} - {payment.method || 'Cash'} - {payment.status || 'Pending'}</span>
                  <small>{payment.reference || 'No reference'}</small>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button onClick={() => handleEdit(payment)} style={{ padding: '5px 10px', fontSize: '12px' }}>
                    Edit
                  </button>
                  <button onClick={() => handleDelete(payment.id)} style={{ padding: '5px 10px', fontSize: '12px' }}>
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Payments;
