import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { api } from './api';
import Navbar from './components/Navbar';

function fmt(val) {
    if (val == null) return '—';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
}

function pnlCls(val) {
    if (val == null) return '';
    return parseFloat(val) >= 0 ? 'positive' : 'negative';
}

export default function Dashboard() {
    const { user } = useAuth();
    const [summaries, setSummaries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showCreate, setShowCreate] = useState(false);
    const [newName, setNewName] = useState('');
    const [creating, setCreating] = useState(false);
    const [createError, setCreateError] = useState('');
    const [redeemCode, setRedeemCode] = useState('');
    const [redeemLoading, setRedeemLoading] = useState(false);
    const [redeemMsg, setRedeemMsg] = useState({ text: '', ok: false });

    const loadPortfolios = useCallback(async () => {
        if (!user?.userId) {
            setLoading(false);
            setError('Could not determine your user ID. Please log out and back in.');
            return;
        }
        try {
            setLoading(true);
            const data = await api.get(`/users/${user.userId}/portfolios/summary`);
            setSummaries(data || []);
            setError('');
        } catch (e) {
            setError(e.message);
        } finally {
            setLoading(false);
        }
    }, [user?.userId]);

    useEffect(() => { loadPortfolios(); }, [loadPortfolios]);

    async function handleCreate(e) {
        e.preventDefault();
        if (!newName.trim()) { setCreateError('Name is required'); return; }
        try {
            setCreating(true);
            await api.post(`/users/${user.userId}/portfolios`, { name: newName.trim() });
            setNewName('');
            setShowCreate(false);
            setCreateError('');
            loadPortfolios();
        } catch (e) {
            setCreateError(e.message);
        } finally {
            setCreating(false);
        }
    }

    async function handleRedeem(e) {
        e.preventDefault();
        if (!redeemCode.trim()) return;
        if (!user?.userId) { setRedeemMsg({ text: 'Cannot redeem — please re-login.', ok: false }); return; }
        try {
            setRedeemLoading(true);
            await api.post(`/invites/${redeemCode.trim()}/redeem/${user.userId}`);
            setRedeemMsg({ text: 'Invite redeemed! Portfolio added to your dashboard.', ok: true });
            setRedeemCode('');
            loadPortfolios();
        } catch (e) {
            setRedeemMsg({ text: e.message, ok: false });
        } finally {
            setRedeemLoading(false);
        }
    }

    return (
        <div className="app-layout">
            <Navbar />
            <main className="page-container">
                <div className="page-header">
                    <h1 className="page-title">My Portfolios</h1>
                    <button className="btn-primary" onClick={() => setShowCreate(true)}>+ New Portfolio</button>
                </div>

                {error && <p className="error-banner">{error}</p>}

                {loading ? (
                    <div className="loading-state">Loading portfolios...</div>
                ) : summaries.length === 0 && !error ? (
                    <div className="empty-state">
                        <p>No portfolios yet.</p>
                        <button className="btn-primary" onClick={() => setShowCreate(true)}>Create your first portfolio</button>
                    </div>
                ) : (
                    <div className="portfolio-grid">
                        {summaries.map(({ portfolio, totalValue, pnl, totalCost }) => (
                            <div className="portfolio-card" key={portfolio.portfolioId}>
                                <div className="portfolio-card-header">
                                    <h3>{portfolio.name}</h3>
                                </div>
                                <div className="portfolio-card-metrics">
                                    <div className="metric">
                                        <span className="metric-label">Total Value</span>
                                        <span className="metric-value">{fmt(totalValue)}</span>
                                    </div>
                                    <div className="metric">
                                        <span className="metric-label">P&amp;L</span>
                                        <span className={`metric-value ${pnlCls(pnl)}`}>{fmt(pnl)}</span>
                                    </div>
                                    <div className="metric">
                                        <span className="metric-label">Cost Basis</span>
                                        <span className="metric-value">{fmt(totalCost)}</span>
                                    </div>
                                </div>
                                <Link to={`/portfolios/${portfolio.portfolioId}`} className="btn-card-link">
                                    View Portfolio →
                                </Link>
                            </div>
                        ))}
                    </div>
                )}
            </main>

            <div className="redeem-invite-bar">
                <form onSubmit={handleRedeem} className="redeem-invite-form">
                    <input
                        type="text"
                        placeholder="Have an invite code? Paste it here"
                        value={redeemCode}
                        onChange={e => { setRedeemCode(e.target.value); setRedeemMsg({ text: '', ok: false }); }}
                    />
                    <button type="submit" className="btn-secondary" disabled={redeemLoading}>
                        {redeemLoading ? 'Redeeming...' : 'Redeem Invite'}
                    </button>
                </form>
                {redeemMsg.text && (
                    <span className={redeemMsg.ok ? 'success' : 'error'}>{redeemMsg.text}</span>
                )}
            </div>

            {showCreate && (
                <div className="modal-overlay" onClick={() => setShowCreate(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <h2>New Portfolio</h2>
                        <form onSubmit={handleCreate}>
                            {createError && <span className="error">{createError}</span>}
                            <div>
                                <label>Portfolio Name</label>
                                <input
                                    type="text"
                                    placeholder="e.g. Growth Portfolio"
                                    value={newName}
                                    onChange={e => setNewName(e.target.value)}
                                    autoFocus
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn-secondary" onClick={() => { setShowCreate(false); setCreateError(''); }}>Cancel</button>
                                <button type="submit" disabled={creating}>{creating ? 'Creating...' : 'Create'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
