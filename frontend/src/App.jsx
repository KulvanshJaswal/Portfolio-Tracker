import { useState } from 'react';

function App() {

    const [input, setInput] = useState('')
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});

    const handleLogin = async (e) => {
        e.preventDefault();

        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            return setErrors(validationErrors);
        }

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
                window.location.href="/dashboard";
            } else{
                console.log("Token not successful");
            }
        } catch (error){
            console.log("Error ", error);
        }
    }

    const validateForm = () => {
        const errors = {};
        if(input.trim().length < 1){
            errors.input = "Username or email must be at least 1 characters";
        }
        if(password.trim().length < 8){
            errors.password = "Password must be at least 8 characters";
        }
        return errors;
    }

    return (
        <div className="login-container">
            <h1>Portfolio Tracker</h1>
            <h2>Login</h2>

            <form onSubmit={handleLogin}>
                <div>
                    <label>Username or Email:</label>
                    <input
                        type="text"
                        placeholder="Enter email or username"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                    />
                    <br/>
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
                    <br/>
                    {errors.input && <span className="error">{errors.password}</span>}
                </div>

                <button type="submit">Login</button>
            </form>

            <p>Don't have an account? <a href="#">Register</a></p>
        </div>
    );
}

export default App;