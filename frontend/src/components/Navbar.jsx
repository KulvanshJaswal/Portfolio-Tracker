import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate('/login');
    }

    return (
        <nav className="navbar">
            <div className="navbar-brand">
                <Link to="/dashboard">Portfolio Tracker</Link>
            </div>
            <div className="navbar-links">
                <Link to="/dashboard">Portfolios</Link>
                <Link to="/settings">Settings</Link>
            </div>
            <div className="navbar-user">
                {user?.username && <span className="navbar-username">{user.username}</span>}
                <button className="btn-logout" onClick={handleLogout}>Logout</button>
            </div>
        </nav>
    );
}
