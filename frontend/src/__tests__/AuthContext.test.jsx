import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { AuthProvider, useAuth } from '../context/AuthContext.jsx';

function Probe() {
  const { user, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="email">{user?.email || 'anon'}</span>
      <button onClick={() => login({ token: 't', email: 'a@b.co', nombre: 'A', rol: 'UNIDAD' })}>
        login
      </button>
      <button onClick={logout}>logout</button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('inicia sin usuario', () => {
    render(<AuthProvider><Probe /></AuthProvider>);
    expect(screen.getByTestId('email').textContent).toBe('anon');
  });

  it('login persiste en localStorage', () => {
    render(<AuthProvider><Probe /></AuthProvider>);
    act(() => { screen.getByText('login').click(); });
    expect(screen.getByTestId('email').textContent).toBe('a@b.co');
    expect(localStorage.getItem('focus_token')).toBe('t');
  });

  it('logout limpia el estado y localStorage', () => {
    render(<AuthProvider><Probe /></AuthProvider>);
    act(() => { screen.getByText('login').click(); });
    act(() => { screen.getByText('logout').click(); });
    expect(screen.getByTestId('email').textContent).toBe('anon');
    expect(localStorage.getItem('focus_token')).toBeNull();
  });
});
