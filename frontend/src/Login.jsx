import { useState } from 'react';
import { Link } from 'react-router-dom';

function Login() {
    const [input, setInput] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});

    const handleLogin = async (e) => {
        e.preventDefault();

        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        setErrors({});

        let token;
        try {
            const isEmail = input.includes("@");

            const response = await fetch(`http://localhost:8080/api/users/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: isEmail ? null : input,
                    email: isEmail ? input : null,
                    password: password
                })
            });

            if (response.ok) {
                token = await response.text();
                localStorage.setItem('token', token);
                console.log("Token successful ", token);
                window.location.href = "/dashboard";
            } else {
                try {
                    const errorData = await response.json();
                    setErrors({ backend: errorData.message || "Login failed" });
                } catch {
                    setErrors({ backend: "Login failed. Please try again." });
                }
            }
        } catch (error) {
            console.log("Error ", error);
        }
    };

    const validateForm = () => {
        const errors = {};
        if (input.trim().length < 1) {
            errors.input = "Username or email must be at least 1 character";
        }
        if (password.trim().length < 8) {
            errors.password = "Password must be at least 8 characters";
        }
        return errors;
    };

    return (
        <div className="login-container">
            <h1>Portfolio Tracker</h1>
            <h2>Login</h2>

            <form onSubmit={handleLogin}>
                {errors.backend && <span className="error">{errors.backend}</span>}

                <div>
                    <label>Username or Email:</label>
                    <input
                        type="text"
                        placeholder="Enter email or username"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                    />
                    {errors.input && <span className="error">{errors.input}</span>}
                </div>

                <div>
                    <label>Password:</label>
                    <input
                        type="password"
                        placeholder="Enter password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    {errors.password && <span className="error">{errors.password}</span>}
                </div>

                <button type="submit">Login</button>
            </form>

            <p>Don't have an account? <Link to="/register">Register</Link></p>
        </div>
    );
}

export default Login;