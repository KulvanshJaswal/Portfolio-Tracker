import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './Login';
import './App.css';
import Register from "./Register.jsx";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/" element={<Navigate to="/login" />} />

                <Route path="/register" element={<Register />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;