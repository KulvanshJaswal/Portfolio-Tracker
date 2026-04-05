import { useState } from 'react';

function App() {
    const [input, setInput] = useState('')
    const [password, setPassword] = useState('');
    const handleLogin = async (e) => {
        e.preventDefault();

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
                </div>

                <div>
                    <label>Password:</label>
                    <input
                        type="password"
                        placeholder="Enter password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>

                <button type="submit">Login</button>
            </form>

            <p>Don't have an account? <a href="#">Register</a></p>
        </div>
    );
}

export default App;