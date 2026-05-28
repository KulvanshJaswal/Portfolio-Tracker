const BASE = 'http://localhost:8080/api';

function getHeaders() {
    const token = localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return headers;
}

async function request(method, path, body) {
    const res = await fetch(`${BASE}${path}`, {
        method,
        headers: getHeaders(),
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) {
        const raw = await res.text();
        let errMsg;
        try {
            const errData = JSON.parse(raw);
            errMsg = errData.message || `Request failed: ${res.status}`;
        } catch {
            errMsg = raw || `Request failed: ${res.status}`;
        }
        throw new Error(errMsg);
    }
    const text = await res.text();
    if (!text) return null;
    try { return JSON.parse(text); } catch { return text; }
}

export const api = {
    get: (path) => request('GET', path),
    post: (path, body) => request('POST', path, body),
    put: (path, body) => request('PUT', path, body),
    del: (path) => request('DELETE', path),
};
