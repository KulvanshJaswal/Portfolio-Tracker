import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { api } from './api';
import Navbar from './components/Navbar';

export default function Settings() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const [newUsername, setNewUsername] = useState('');
    const [newEmail, setNewEmail] = useState('');
    const [usernameMsg, setUsernameMsg] = useState({ text: '', ok: false });
    const [emailMsg, setEmailMsg] = useState({ text: '', ok: false });
    const [saving, setSaving] = useState({ username: false, email: false });

    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [deleteLoading, setDeleteLoading] = useState(false);
    const [deleteError, setDeleteError] = useState('');

    const userId = user?.userId;

    async function handleUpdateUsername(e) {
        e.preventDefault();
        if (!newUsername.trim()) return;
        if (!userId) { setUsernameMsg({ text: 'User ID unknown — please re-login.', ok: false }); return; }
        try {
            setSaving(prev => ({ ...prev, username: true }));
            await api.put(`/users/${userId}/username`, { username: newUsername.trim() });
            localStorage.setItem('username', newUsername.trim());
            setUsernameMsg({ text: 'Username updated!', ok: true });
            setNewUsername('');
        } catch (e) {
            setUsernameMsg({ text: e.message, ok: false });
        } finally {
            setSaving(prev => ({ ...prev, username: false }));
        }
    }

    async function handleUpdateEmail(e) {
        e.preventDefault();
        if (!newEmail.trim()) return;
        if (!userId) { setEmailMsg({ text: 'User ID unknown — please re-login.', ok: false }); return; }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(newEmail)) { setEmailMsg({ text: 'Invalid email format', ok: false }); return; }
        try {
            setSaving(prev => ({ ...prev, email: true }));
            await api.put(`/users/${userId}/email`, { email: newEmail.trim() });
            setEmailMsg({ text: 'Email updated!', ok: true });
            setNewEmail('');
        } catch (e) {
            setEmailMsg({ text: e.message, ok: false });
        } finally {
            setSaving(prev => ({ ...prev, email: false }));
        }
    }

    async function handleDeleteAccount() {
        if (!userId) { setDeleteError('User ID unknown — please re-login.'); return; }
        try {
            setDeleteLoading(true);
            await api.del(`/users/${userId}`);
            logout();
            navigate('/login');
        } catch (e) {
            setDeleteError(e.message);
            setDeleteLoading(false);
            setShowDeleteConfirm(false);
        }
    }

    return (
        <div className="app-layout">
            <Navbar />
            <main className="page-container">
                <div className="page-header">
                    <h1 className="page-title">Settings</h1>
                </div>

                {!userId && (
                    <p className="error-banner">
                        Your user ID could not be determined. Some settings may not work until you log out and back in.
                    </p>
                )}

                <div className="settings-grid">
                    <div className="settings-card">
                        <h2>Account Info</h2>
                        {user?.username && (
                            <p className="settings-current">
                                Current username: <strong>{user.username}</strong>
                            </p>
                        )}

                        <form onSubmit={handleUpdateUsername} style={{ marginBottom: '28px' }}>
                            <h3>Change Username</h3>
                            {usernameMsg.text && (
                                <span className={usernameMsg.ok ? 'success' : 'error'}>{usernameMsg.text}</span>
                            )}
                            <div style={{ marginTop: '12px' }}>
                                <label>New Username</label>
                                <input
                                    type="text"
                                    placeholder="Enter new username"
                                    value={newUsername}
                                    onChange={e => { setNewUsername(e.target.value); setUsernameMsg({ text: '', ok: false }); }}
                                />
                            </div>
                            <button type="submit" disabled={saving.username} style={{ marginTop: '12px' }}>
                                {saving.username ? 'Saving...' : 'Update Username'}
                            </button>
                        </form>

                        <form onSubmit={handleUpdateEmail}>
                            <h3>Change Email</h3>
                            {emailMsg.text && (
                                <span className={emailMsg.ok ? 'success' : 'error'}>{emailMsg.text}</span>
                            )}
                            <div style={{ marginTop: '12px' }}>
                                <label>New Email</label>
                                <input
                                    type="email"
                                    placeholder="Enter new email"
                                    value={newEmail}
                                    onChange={e => { setNewEmail(e.target.value); setEmailMsg({ text: '', ok: false }); }}
                                />
                            </div>
                            <button type="submit" disabled={saving.email} style={{ marginTop: '12px' }}>
                                {saving.email ? 'Saving...' : 'Update Email'}
                            </button>
                        </form>
                    </div>

                    <div className="settings-card danger-zone">
                        <h2>Danger Zone</h2>
                        <p className="settings-current">
                            Permanently delete your account and all associated portfolios. This cannot be undone.
                        </p>
                        {deleteError && <span className="error">{deleteError}</span>}
                        <button className="btn-danger" onClick={() => setShowDeleteConfirm(true)} style={{ marginTop: '16px' }}>
                            Delete Account
                        </button>
                    </div>
                </div>
            </main>

            {showDeleteConfirm && (
                <div className="modal-overlay" onClick={() => setShowDeleteConfirm(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <h2>Delete Account</h2>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>
                            This will permanently delete your account and all your portfolios. Are you absolutely sure?
                        </p>
                        <div className="modal-actions">
                            <button className="btn-secondary" onClick={() => setShowDeleteConfirm(false)}>Cancel</button>
                            <button className="btn-danger" onClick={handleDeleteAccount} disabled={deleteLoading}>
                                {deleteLoading ? 'Deleting...' : 'Yes, Delete My Account'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
