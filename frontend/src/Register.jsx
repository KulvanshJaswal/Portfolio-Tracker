import { useState } from 'react';
import { Link } from 'react-router-dom';

function Register() {

    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errors, setErrors] = useState({});

    const handleRegister = async (e) => {
        e.preventDefault();

        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        setErrors({});

        let token;
        try {
            const response = await fetch(`http://localhost:8080/api/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    email: email,
                    password: password
                })
            });

            if(response.ok){
                token = await response.text();
                localStorage.setItem('token', token);
                console.log("Token retrieved");
                window.location.href = "/dashboard";
            } else {
            try {
                const errorData = await response.json();
                setErrors({ backend: errorData.message || "Registration failed" });
            } catch {
                setErrors({ backend: "Registration failed. Please try again." });
            }
        }
        } catch (error) {
            console.log("Error: ", error);
        }
    }

    const validateForm = () => {
        const errors = {};
        if (!email || !username || !password) {
            errors.all = "Missing required fields.";
        }

        if(password !== confirmPassword) {
            errors.confirm = "Passwords do not match";
        }

        if(password.length < 8) {
            errors.password = "Password must be at least 6 characters.";
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            errors.email = "Invalid email format";
        }
        if(email.length > 100){
            errors.email = "Email is too long";
        }

        const usernameRegex = /^[a-zA-Z0-9_-]+$/;
        if (!usernameRegex.test(username)) {
            errors.username = "Username can only contain letters, numbers, - and _";
        }
        if(username.length < 1 || username.length > 50) {
            errors.username = "Username has a max of 50 characters and a min of 1 character";
        }

        return errors;
    }
    return (
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
                        onChange={(e) => setUsername(e.target.value)}
                    />
                    {errors.input && <span className="error">{errors.input}</span>}
               </div>

                <div>
                    <label>Email</label>
                    <input
                        type="email"
                        name="email"
                        placeholder="Enter email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    {errors.email && <span className="error">{errors.email}</span>}
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

                <div>
                    <label>Confirm Password:</label>
                    <input
                        type="password"
                        placeholder="Confirm password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                    {errors.confirm && <span className="error">{errors.confirm}</span>}
                </div>

                <button type="submit">Create Account</button>
            </form>

            <p>Already have an account? <Link to="/login">Login</Link></p>
        </div>
    );
}

export default Register;