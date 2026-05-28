import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { api } from './api';
import Navbar from './components/Navbar';

const TX_ENDPOINT = { deposit: 'deposit', withdraw: 'withdrawal', buy: 'buy', sell: 'sell' };
const ROLES = ['ADMIN', 'MEMBER', 'VISITOR'];

function fmt(val) {
    if (val == null) return '—';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
}

function pnlCls(val) {
    if (val == null) return '';
    return parseFloat(val) >= 0 ? 'positive' : 'negative';
}

function fmtQty(amount, assetType) {
    if (amount == null) return '—';
    const n = parseFloat(amount);
    if (!isFinite(n)) return '—';

    // Cash is a dollar amount — always 2 decimal places
    if (assetType === 'CASH') {
        return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    if (n === 0) return '0';

    // Whole number — no decimals
    if (n === Math.round(n)) return n.toLocaleString('en-US');

    const abs = Math.abs(n);

    // >= 1 with a fractional part — up to 4 decimal places, trailing zeros stripped
    if (abs >= 1) {
        return n.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 4 });
    }

    // Small number (< 1) — show enough decimal places to expose the significant digits
    // e.g. 0.00001 → magnitude -5 → 8 decimal places → "0.00001"
    const magnitude = Math.floor(Math.log10(abs));
    const decimals = Math.min(Math.abs(magnitude) + 3, 8);
    return n.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: decimals });
}

function fmtDate(dt) {
    if (!dt) return '—';
    return new Date(dt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

export default function PortfolioDetail() {
    const { portfolioId } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [summary, setSummary] = useState(null);
    const [positions, setPositions] = useState([]);
    const [members, setMembers] = useState([]);
    const [invites, setInvites] = useState([]);
    const [activeTab, setActiveTab] = useState('positions');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [pnlMap, setPnlMap] = useState({});

    const [editingName, setEditingName] = useState(false);
    const [nameInput, setNameInput] = useState('');
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const [txMode, setTxMode] = useState(null);
    const [txForm, setTxForm] = useState({});
    const [txLoading, setTxLoading] = useState(false);
    const [txError, setTxError] = useState('');
    const [transactions, setTransactions] = useState([]);

    const [memberForm, setMemberForm] = useState({ userId: '', role: 'MEMBER' });
    const [memberError, setMemberError] = useState('');
    const [memberSuccess, setMemberSuccess] = useState('');

    const [inviteForm, setInviteForm] = useState({ daysForExpiry: 7, maxUses: 10, userRole: 'MEMBER' });
    const [redeemCode, setRedeemCode] = useState('');
    const [inviteError, setInviteError] = useState('');
    const [inviteSuccess, setInviteSuccess] = useState('');

    useEffect(() => { loadData(); }, [portfolioId]);

    useEffect(() => {
        if (!positions.length) return;
        setPnlMap({});
        positions.filter(p => p.assetType !== 'CASH').forEach(async (pos) => {
            try {
                const pnl = await api.get(`/portfolios/${portfolioId}/positions/${pos.symbol}/pnl`);
                setPnlMap(prev => ({ ...prev, [pos.symbol]: pnl }));
            } catch { /* silently skip */ }
        });
    }, [positions]);

    useEffect(() => {
        if (activeTab === 'transactions') loadTransactions();
    }, [activeTab]);

    useEffect(() => {
        if (activeTab === 'members') loadMembers();
    }, [activeTab]);

    useEffect(() => {
        if (activeTab === 'invites') loadInvites();
    }, [activeTab]);

    async function loadData() {
        try {
            setLoading(true);
            const [sum, pos] = await Promise.all([
                api.get(`/portfolios/${portfolioId}/summary`),
                api.get(`/portfolios/${portfolioId}/positions`),
            ]);
            setSummary(sum);
            setPositions(pos || []);
            setNameInput(sum?.portfolio?.name || '');
            setError('');
        } catch (e) {
            setError(e.message);
        } finally {
            setLoading(false);
        }
    }

    async function loadMembers() {
        try {
            const data = await api.get(`/portfolios/${portfolioId}/members`);
            setMembers(data || []);
        } catch (e) {
            setError(e.message);
        }
    }

    async function loadTransactions() {
        try {
            const data = await api.get(`/portfolios/${portfolioId}/transactions`);
            setTransactions((data || []).slice().reverse());
        } catch (e) {
            setTxError(e.message);
        }
    }

    async function loadInvites() {
        try {
            const data = await api.get(`/portfolios/${portfolioId}/invites/active`);
            setInvites(data || []);
        } catch (e) {
            setError(e.message);
        }
    }

    async function saveName() {
        if (!nameInput.trim()) return;
        try {
            await api.put(`/portfolios/${portfolioId}`, { name: nameInput.trim() });
            setSummary(prev => ({ ...prev, portfolio: { ...prev.portfolio, name: nameInput.trim() } }));
            setEditingName(false);
        } catch (e) {
            setError(e.message);
        }
    }

    async function handleDelete() {
        try {
            await api.del(`/portfolios/${portfolioId}`);
            navigate('/dashboard');
        } catch (e) {
            setError(e.message);
            setShowDeleteConfirm(false);
        }
    }

    function openTxForm(mode) {
        setTxMode(prev => prev === mode ? null : mode);
        setTxError('');
        setTxSuccess('');
        setTxForm((mode === 'buy' || mode === 'sell') ? { assetType: 'STOCK' } : {});
    }

    async function handleTransaction(e) {
        e.preventDefault();
        setTxLoading(true);
        setTxError('');
        try {
            let body;
            if (txMode === 'deposit' || txMode === 'withdraw') {
                body = { amount: parseFloat(txForm.amount) };
            } else {
                body = {
                    symbol: txForm.symbol?.toUpperCase(),
                    assetType: txForm.assetType,
                    quantity: parseFloat(txForm.quantity),
                };
            }
            await api.post(`/portfolios/${portfolioId}/transactions/${TX_ENDPOINT[txMode]}`, body);
            setTxMode(null);
            setTxForm({});
            const [sum, pos] = await Promise.all([
                api.get(`/portfolios/${portfolioId}/summary`),
                api.get(`/portfolios/${portfolioId}/positions`),
            ]);
            setSummary(sum);
            setPositions(pos || []);
            loadTransactions();
        } catch (e) {
            setTxError(e.message);
        } finally {
            setTxLoading(false);
        }
    }

    async function handleAddMember(e) {
        e.preventDefault();
        setMemberError('');
        setMemberSuccess('');
        try {
            await api.post(`/portfolios/${portfolioId}/members`, {
                userId: parseInt(memberForm.userId),
                role: memberForm.role,
            });
            setMemberForm({ userId: '', role: 'MEMBER' });
            setMemberSuccess('Member added!');
            loadMembers();
        } catch (e) {
            setMemberError(e.message);
        }
    }

    async function handleRemoveMember(memberId) {
        try {
            await api.del(`/portfolios/${portfolioId}/members/${memberId}`);
            setMembers(prev => prev.filter(m => m.user?.userId !== memberId));
        } catch (e) {
            setError(e.message);
        }
    }

    async function handleChangeRole(memberId, role) {
        try {
            await api.put(`/portfolios/${portfolioId}/members/${memberId}`, { role });
            setMembers(prev => prev.map(m => m.user?.userId === memberId ? { ...m, role } : m));
        } catch (e) {
            setError(e.message);
        }
    }

    async function handleCreateInvite(e) {
        e.preventDefault();
        setInviteError('');
        setInviteSuccess('');
        try {
            await api.post(`/portfolios/${portfolioId}/invites`, {
                userId: user?.userId ? parseInt(user.userId) : null,
                daysForExpiry: parseInt(inviteForm.daysForExpiry),
                maxUses: parseInt(inviteForm.maxUses),
                userRole: inviteForm.userRole,
            });
            setInviteSuccess('Invite created!');
            loadInvites();
        } catch (e) {
            setInviteError(e.message);
        }
    }

    async function handleRevokeInvite(inviteId) {
        try {
            await api.del(`/invites/${inviteId}`);
            setInvites(prev => prev.filter(i => i.inviteId !== inviteId));
        } catch (e) {
            setError(e.message);
        }
    }

    async function handleRedeemInvite(e) {
        e.preventDefault();
        setInviteError('');
        setInviteSuccess('');
        if (!user?.userId) { setInviteError('Cannot redeem — user ID unknown. Please re-login.'); return; }
        try {
            await api.post(`/invites/${redeemCode.trim()}/redeem/${user.userId}`);
            setInviteSuccess('Invite redeemed! You are now a member.');
            setRedeemCode('');
        } catch (e) {
            setInviteError(e.message);
        }
    }

    function copyToClipboard(text) {
        navigator.clipboard.writeText(text).catch(() => {});
    }

    if (loading) return (
        <div className="app-layout">
            <Navbar />
            <div className="page-container loading-state">Loading portfolio...</div>
        </div>
    );

    return (
        <div className="app-layout">
            <Navbar />
            <main className="page-container">

                {error && <p className="error-banner">{error}</p>}

                <div className="detail-header">
                    <button className="btn-back" onClick={() => navigate('/dashboard')}>← Portfolios</button>
                    <div className="portfolio-title-row">
                        {editingName ? (
                            <div className="inline-edit">
                                <input
                                    value={nameInput}
                                    onChange={e => setNameInput(e.target.value)}
                                    onKeyDown={e => { if (e.key === 'Enter') saveName(); if (e.key === 'Escape') setEditingName(false); }}
                                    autoFocus
                                />
                                <button className="btn-primary btn-sm" onClick={saveName}>Save</button>
                                <button className="btn-secondary btn-sm" onClick={() => setEditingName(false)}>Cancel</button>
                            </div>
                        ) : (
                            <h1
                                className="editable-title"
                                onClick={() => setEditingName(true)}
                                title="Click to edit"
                            >
                                {summary?.portfolio?.name}
                                <span className="edit-hint">✎</span>
                            </h1>
                        )}
                        <button className="btn-danger btn-sm" onClick={() => setShowDeleteConfirm(true)}>Delete</button>
                    </div>
                </div>

                <div className="summary-cards">
                    <div className="summary-card">
                        <span className="metric-label">Total Value</span>
                        <span className="metric-value">{fmt(summary?.totalValue)}</span>
                    </div>
                    <div className="summary-card">
                        <span className="metric-label">P&amp;L</span>
                        <span className={`metric-value ${pnlCls(summary?.pnl)}`}>{fmt(summary?.pnl)}</span>
                    </div>
                    <div className="summary-card">
                        <span className="metric-label">Cash</span>
                        <span className="metric-value">{fmt(positions.find(p => p.symbol === 'CASH')?.totalQuantity)}</span>
                    </div>
                </div>

                <div className="tabs">
                    {['positions', 'transactions', 'members', 'invites'].map(tab => (
                        <button
                            key={tab}
                            className={`tab-btn ${activeTab === tab ? 'active' : ''}`}
                            onClick={() => setActiveTab(tab)}
                        >
                            {tab.charAt(0).toUpperCase() + tab.slice(1)}
                        </button>
                    ))}
                </div>

                <div className="tab-content">

                    {/* ── POSITIONS ─────────────────────────────────────────── */}
                    {activeTab === 'positions' && (
                        positions.filter(p => p.symbol !== 'CASH').length === 0 ? (
                            <div className="empty-state">
                                No positions yet. Head to Transactions to deposit funds and start trading.
                            </div>
                        ) : (
                            <div className="table-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Symbol</th>
                                            <th>Type</th>
                                            <th>Quantity</th>
                                            <th>Avg Cost / Unit</th>
                                            <th>Total Cost</th>
                                            <th>P&amp;L</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {positions.filter(p => p.symbol !== 'CASH').map(pos => (
                                            <tr key={pos.positionId}>
                                                <td><strong>{pos.symbol}</strong></td>
                                                <td>
                                                    <span className={`badge badge-${pos.assetType.toLowerCase()}`}>
                                                        {pos.assetType}
                                                    </span>
                                                </td>
                                                <td>{fmtQty(pos.totalQuantity, pos.assetType)}</td>
                                                <td>{fmt(pos.averageCostPerUnit)}</td>
                                                <td>{fmt(pos.totalCost)}</td>
                                                <td className={pnlCls(pnlMap[pos.symbol])}>
                                                    {pos.assetType === 'CASH'
                                                        ? '—'
                                                        : pnlMap[pos.symbol] != null
                                                            ? fmt(pnlMap[pos.symbol])
                                                            : <span className="loading-dot">...</span>}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )
                    )}

                    {/* ── TRANSACTIONS ──────────────────────────────────────── */}
                    {activeTab === 'transactions' && (
                        <div className="transactions-section">
                            <div className="tx-action-buttons">
                                {['deposit', 'withdraw', 'buy', 'sell'].map(mode => (
                                    <button
                                        key={mode}
                                        className={`btn-tx btn-tx-${mode} ${txMode === mode ? 'active' : ''}`}
                                        onClick={() => openTxForm(mode)}
                                    >
                                        {mode.charAt(0).toUpperCase() + mode.slice(1)}
                                    </button>
                                ))}
                            </div>

                            {txMode && (
                                <div className="tx-form-container">
                                    <h3>{txMode.charAt(0).toUpperCase() + txMode.slice(1)}</h3>
                                    <form onSubmit={handleTransaction}>
                                        {txError && <span className="error">{txError}</span>}

                                        {(txMode === 'deposit' || txMode === 'withdraw') && (
                                            <div>
                                                <label>Amount (USD)</label>
                                                <input
                                                    type="number"
                                                    step="0.01"
                                                    min="0.01"
                                                    placeholder="0.00"
                                                    value={txForm.amount || ''}
                                                    onChange={e => setTxForm({ amount: e.target.value })}
                                                    required
                                                />
                                            </div>
                                        )}

                                        {(txMode === 'buy' || txMode === 'sell') && (<>
                                            <div>
                                                <label>Symbol</label>
                                                <input
                                                    type="text"
                                                    placeholder="e.g. AAPL or BTC"
                                                    value={txForm.symbol || ''}
                                                    onChange={e => setTxForm(prev => ({ ...prev, symbol: e.target.value.toUpperCase() }))}
                                                    required
                                                />
                                            </div>
                                            <div>
                                                <label>Asset Type</label>
                                                <select
                                                    value={txForm.assetType || 'STOCK'}
                                                    onChange={e => setTxForm(prev => ({ ...prev, assetType: e.target.value }))}
                                                >
                                                    <option value="STOCK">Stock</option>
                                                    <option value="CRYPTO">Crypto</option>
                                                </select>
                                            </div>
                                            <div>
                                                <label>Quantity</label>
                                                <input
                                                    type="number"
                                                    step="any"
                                                    min="0.000001"
                                                    placeholder="0"
                                                    value={txForm.quantity || ''}
                                                    onChange={e => setTxForm(prev => ({ ...prev, quantity: e.target.value }))}
                                                    required
                                                />
                                            </div>
                                        </>)}

                                        <div className="form-actions">
                                            <button
                                                type="button"
                                                className="btn-secondary"
                                                onClick={() => { setTxMode(null); setTxForm({}); setTxError(''); }}
                                            >
                                                Cancel
                                            </button>
                                            <button type="submit" disabled={txLoading}>
                                                {txLoading ? 'Processing...' : `Confirm ${txMode.charAt(0).toUpperCase() + txMode.slice(1)}`}
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            )}

                            {transactions.length > 0 ? (
                                <div className="table-wrapper" style={{ marginTop: '24px' }}>
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Type</th>
                                                <th>Symbol</th>
                                                <th>Asset</th>
                                                <th>Quantity</th>
                                                <th>Price / Unit</th>
                                                <th>Total</th>
                                                <th>Date</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {transactions.map(tx => (
                                                <tr key={tx.transactionId}>
                                                    <td>
                                                        <span className={`badge badge-${tx.transactionType.toLowerCase()}`}>
                                                            {tx.transactionType}
                                                        </span>
                                                    </td>
                                                    <td><strong>{tx.symbol}</strong></td>
                                                    <td>{tx.assetType}</td>
                                                    <td>{fmtQty(tx.quantity, tx.assetType)}</td>
                                                    <td>{tx.assetType === 'CASH' ? '—' : fmt(tx.pricePerUnit)}</td>
                                                    <td>{fmt(parseFloat(tx.quantity) * parseFloat(tx.pricePerUnit))}</td>
                                                    <td>{fmtDate(tx.timestamp)}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            ) : (
                                !txMode && <div className="empty-state">No transactions yet.</div>
                            )}
                        </div>
                    )}

                    {/* ── MEMBERS ───────────────────────────────────────────── */}
                    {activeTab === 'members' && (
                        <div className="members-section">
                            {members.length > 0 && (
                                <div className="table-wrapper">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>User ID</th>
                                                <th>Username</th>
                                                <th>Email</th>
                                                <th>Role</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {members.map(m => (
                                                <tr key={m.membershipId}>
                                                    <td>{m.user?.userId}</td>
                                                    <td>{m.user?.username}</td>
                                                    <td>{m.user?.email}</td>
                                                    <td>
                                                        <select
                                                            className="inline-select"
                                                            value={m.role}
                                                            onChange={e => handleChangeRole(m.user?.userId, e.target.value)}
                                                        >
                                                            {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                                                        </select>
                                                    </td>
                                                    <td>
                                                        <button
                                                            className="btn-danger-sm"
                                                            onClick={() => handleRemoveMember(m.user?.userId)}
                                                        >
                                                            Remove
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}

                            <div className="side-form">
                                <h3>Add Member</h3>
                                {memberError && <span className="error">{memberError}</span>}
                                {memberSuccess && <span className="success">{memberSuccess}</span>}
                                <form onSubmit={handleAddMember}>
                                    <div>
                                        <label>User ID</label>
                                        <input
                                            type="number"
                                            placeholder="Enter user ID"
                                            value={memberForm.userId}
                                            onChange={e => setMemberForm(prev => ({ ...prev, userId: e.target.value }))}
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label>Role</label>
                                        <select
                                            value={memberForm.role}
                                            onChange={e => setMemberForm(prev => ({ ...prev, role: e.target.value }))}
                                        >
                                            {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                                        </select>
                                    </div>
                                    <button type="submit">Add Member</button>
                                </form>
                            </div>
                        </div>
                    )}

                    {/* ── INVITES ───────────────────────────────────────────── */}
                    {activeTab === 'invites' && (
                        <div className="invites-section">
                            {inviteError && <span className="error">{inviteError}</span>}
                            {inviteSuccess && <span className="success">{inviteSuccess}</span>}

                            {invites.length > 0 && (
                                <div className="table-wrapper" style={{ marginBottom: '32px' }}>
                                    <h3 style={{ marginBottom: '12px' }}>Active Invites</h3>
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Code</th>
                                                <th>Role</th>
                                                <th>Uses</th>
                                                <th>Expires</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {invites.map(inv => (
                                                <tr key={inv.inviteId}>
                                                    <td>
                                                        <span className="invite-code">{inv.inviteCode}</span>
                                                        <button
                                                            className="btn-copy"
                                                            onClick={() => copyToClipboard(inv.inviteCode)}
                                                            title="Copy code"
                                                        >
                                                            ⎘
                                                        </button>
                                                    </td>
                                                    <td>{inv.role}</td>
                                                    <td>{inv.currentUses}/{inv.maxUses}</td>
                                                    <td>{fmtDate(inv.expiresAt)}</td>
                                                    <td>
                                                        <button
                                                            className="btn-danger-sm"
                                                            onClick={() => handleRevokeInvite(inv.inviteId)}
                                                        >
                                                            Revoke
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}

                            <div className="side-form">
                                <h3>Create Invite</h3>
                                <form onSubmit={handleCreateInvite}>
                                    <div>
                                        <label>Role Granted</label>
                                        <select
                                            value={inviteForm.userRole}
                                            onChange={e => setInviteForm(prev => ({ ...prev, userRole: e.target.value }))}
                                        >
                                            {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                                        </select>
                                    </div>
                                    <div>
                                        <label>Days Until Expiry</label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="365"
                                            value={inviteForm.daysForExpiry}
                                            onChange={e => setInviteForm(prev => ({ ...prev, daysForExpiry: e.target.value }))}
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label>Max Uses</label>
                                        <input
                                            type="number"
                                            min="1"
                                            value={inviteForm.maxUses}
                                            onChange={e => setInviteForm(prev => ({ ...prev, maxUses: e.target.value }))}
                                            required
                                        />
                                    </div>
                                    <button type="submit">Create Invite</button>
                                </form>
                            </div>
                        </div>
                    )}

                </div>
            </main>

            {showDeleteConfirm && (
                <div className="modal-overlay" onClick={() => setShowDeleteConfirm(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <h2>Delete Portfolio</h2>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>
                            Are you sure you want to delete <strong>{summary?.portfolio?.name}</strong>? This cannot be undone.
                        </p>
                        <div className="modal-actions">
                            <button className="btn-secondary" onClick={() => setShowDeleteConfirm(false)}>Cancel</button>
                            <button className="btn-danger" onClick={handleDelete}>Delete Portfolio</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
