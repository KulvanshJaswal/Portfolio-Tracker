import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

function Login() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [input, setInput] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();

        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        setErrors({});
        setLoading(true);

        try {
            const isEmail = input.includes('@');

            const response = await fetch('http://localhost:8080/api/users/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: isEmail ? null : input,
                    email: isEmail ? input : null,
                    password,
                }),
            });

            if (response.ok) {
                const text = await response.text();
                let token = text;
                let userId = null;
                let username = null;

                try {
                    const parsed = JSON.parse(text);
                    token = parsed.token || text;
                    userId = parsed.userId || null;
                    username = parsed.username || null;
                } catch { /* plain string token */ }

                login(token, userId, username);
                navigate('/dashboard');
            } else {
                try {
                    const errorData = await response.json();
                    setErrors({ backend: errorData.message || 'Login failed' });
                } catch {
                    setErrors({ backend: 'Login failed. Please try again.' });
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
        if (input.trim().length < 1) errs.input = 'Username or email is required';
        if (password.trim().length < 8) errs.password = 'Password must be at least 8 characters';
        return errs;
    };

    return (
        <div className="auth-page">
            <div className="login-container">
                <h1>Portfolio Tracker</h1>
                <h2>Login</h2>

                <form onSubmit={handleLogin}>
                    {errors.backend && <span className="error">{errors.backend}</span>}

                    <div>
                        <label>Username or Email</label>
                        <input
                            type="text"
                            placeholder="Enter email or username"
                            value={input}
                            onChange={e => setInput(e.target.value)}
                        />
                        {errors.input && <span className="error">{errors.input}</span>}
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

                    <button type="submit" disabled={loading}>{loading ? 'Logging in...' : 'Login'}</button>
                </form>

                <p>Don&apos;t have an account? <Link to="/register">Register</Link></p>
            </div>
        </div>
    );
}

export default Login;
