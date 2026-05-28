import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

function Register() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const handleRegister = async (e) => {
        e.preventDefault();

        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        setErrors({});
        setLoading(true);

        try {
            const response = await fetch('http://localhost:8080/api/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password }),
            });

            if (response.ok) {
                const text = await response.text();
                let userId = null;
                let uname = username;

                try {
                    const parsed = JSON.parse(text);
                    if (parsed.userId) {
                        userId = parsed.userId;
                        uname = parsed.username || username;
                    }
                } catch { /* ignore */ }

                // Registration returns a User, not a token — auto-login to get JWT
                const loginRes = await fetch('http://localhost:8080/api/users/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username: uname, password }),
                });
                if (loginRes.ok) {
                    const { token } = await loginRes.json();
                    login(token, userId, uname);
                    navigate('/dashboard');
                } else {
                    // Registered OK but auto-login failed — send them to login page
                    navigate('/login');
                }
            } else {
                try {
                    const errorData = await response.json();
                    setErrors({ backend: errorData.message || 'Registration failed' });
                } catch {
                    setErrors({ backend: 'Registration failed. Please try again.' });
                }
            }
        } catch {
            setErrors({ backend: 'Could not connect to server.' });
        } finally {
            setLoading(false);
        }
    };

    const validateForm = () => {
        const errs = {};
        if (!email || !username || !password) errs.all = 'All fields are required.';
        if (password !== confirmPassword) errs.confirm = 'Passwords do not match';
        if (password.length < 8) errs.password = 'Password must be at least 8 characters.';
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) errs.email = 'Invalid email format';
        if (email.length > 100) errs.email = 'Email is too long';
        const usernameRegex = /^[a-zA-Z0-9_-]+$/;
        if (!usernameRegex.test(username)) errs.username = 'Username can only contain letters, numbers, - and _';
        if (username.length < 1 || username.length > 50) errs.username = 'Username must be 1–50 characters';
        return errs;
    };

    return (
        <div className="auth-page">
            <div className="login-container">
                <h1>Portfolio Tracker</h1>
                <h2>Create Account</h2>
                <form onSubmit={handleRegister}>
                    {errors.backend && <span className="error">{errors.backend}</span>}
                    {errors.all && <span className="error">{errors.all}</span>}

                    <div>
                        <label>Username</label>
                        <input
                            type="text"
                            name="username"
                            placeholder="Enter username"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                        />
                        {errors.username && <span className="error">{errors.username}</span>}
                    </div>

                    <div>
                        <label>Email</label>
                        <input
                            type="email"
                            name="email"
                            placeholder="Enter email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                        />
                        {errors.email && <span className="error">{errors.email}</span>}
                    </div>

                    <div>
                        <label>Password</label>
                        <input
                            type="password"
                            placeholder="Enter password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                        />
                        {errors.password && <span className="error">{errors.password}</span>}
                    </div>

                    <div>
                        <label>Confirm Password</label>
                        <input
                            type="password"
                            placeholder="Confirm password"
                            value={confirmPassword}
                            onChange={e => setConfirmPassword(e.target.value)}
                        />
                        {errors.confirm && <span className="error">{errors.confirm}</span>}
                    </div>

                    <button type="submit" disabled={loading}>{loading ? 'Creating...' : 'Create Account'}</button>
                </form>

                <p>Already have an account? <Link to="/login">Login</Link></p>
            </div>
        </div>
    );
}

export default Register;
