import { createContext, useContext, useEffect, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const raw = localStorage.getItem('focus_user');
    if (raw) {
      try { setUser(JSON.parse(raw)); } catch (_) { /* ignorar */ }
    }
  }, []);

  function login({ token, email, nombre, rol }) {
    localStorage.setItem('focus_token', token);
    const u = { email, nombre, rol };
    localStorage.setItem('focus_user', JSON.stringify(u));
    setUser(u);
  }

  function logout() {
    localStorage.removeItem('focus_token');
    localStorage.removeItem('focus_user');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}
