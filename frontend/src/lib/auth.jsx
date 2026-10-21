import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { onSessionExpired, post, rememberSession, setAccessToken, tryRestoreSession } from './api.js';

const Ctx = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let alive = true;
    tryRestoreSession().then((s) => {
      if (!alive) return;
      if (s) setUser(s.user);
      setReady(true);
    });
    const off = onSessionExpired(() => setUser(null));
    return () => {
      alive = false;
      off();
    };
  }, []);

  const start = useCallback((session) => {
    setAccessToken(session.accessToken);
    rememberSession(true);
    setUser(session.user);
    return session.user;
  }, []);

  const login = useCallback(async (email, password) => start(await post('/api/auth/login', { email, password })), [start]);
  const register = useCallback(async (data) => start(await post('/api/auth/register', data)), [start]);
  const logout = useCallback(async () => {
    try {
      await post('/api/auth/logout');
    } finally {
      setAccessToken(null);
      rememberSession(false);
      setUser(null);
    }
  }, []);

  const value = useMemo(() => ({ user, ready, login, register, logout, setUser, isAdmin: user?.role === 'ADMIN' }),
    [user, ready, login, register, logout]);
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export const useAuth = () => useContext(Ctx);
