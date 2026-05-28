import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

function tryDecodeJwt(token) {
    try {
        const payload = token.split('.')[1];
        return JSON.parse(atob(payload));
    } catch { return null; }
}

function buildUser(token) {
    const claims = token ? tryDecodeJwt(token) : null;
    return {
        token: token || '',
        userId: localStorage.getItem('userId') || claims?.userId || claims?.id
            || (!isNaN(claims?.sub) ? claims.sub : null) || null,
        username: localStorage.getItem('username') || claims?.username
            || (isNaN(claims?.sub) ? claims?.sub : null) || null,
    };
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const token = localStorage.getItem('token');
        const userId = localStorage.getItem('userId');
        if (!token && !userId) return null;
        return buildUser(token || '');
    });

    function login(token, userId, username) {
        if (token) localStorage.setItem('token', token);
        if (userId != null) localStorage.setItem('userId', String(userId));
        if (username) localStorage.setItem('username', username);
        setUser(buildUser(token || localStorage.getItem('token') || ''));
    }

    function logout() {
        ['token', 'userId', 'username'].forEach(k => localStorage.removeItem(k));
        setUser(null);
    }

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}
