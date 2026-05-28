import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import ProtectedRoute from './ProtectedRoute';
import Login from './Login';
import Register from './Register';
import Dashboard from './Dashboard';
import PortfolioDetail from './PortfolioDetail';
import Settings from './Settings';
import './App.css';

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/" element={<Navigate to="/dashboard" />} />
                    <Route path="/dashboard" element={
                        <ProtectedRoute><Dashboard /></ProtectedRoute>
                    } />
                    <Route path="/portfolios/:portfolioId" element={
                        <ProtectedRoute><PortfolioDetail /></ProtectedRoute>
                    } />
                    <Route path="/settings" element={
                        <ProtectedRoute><Settings /></ProtectedRoute>
                    } />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
