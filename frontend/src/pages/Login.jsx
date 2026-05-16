import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

/**
 * Login con Google OAuth 2.0.
 *
 * En dev local, el flujo OAuth completo no esta disponible; el form acepta
 * un ID token "manual" (boton "Continuar como demo") que reusa los usuarios
 * seed (coordinador@focusagency.co, etc.).
 *
 * En produccion, el boton "Iniciar con Google" abre la pantalla real de Google
 * y obtiene el ID token via Google Identity Services.
 */
export default function Login() {
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function entrarComoDemo() {
    setLoading(true);
    setError(null);
    try {
      // En dev, el SecurityFilterChain esta abierto. Saltamos auth para entrar.
      login({
        token: 'dev-token',
        email: 'coordinador@focusagency.co',
        nombre: 'Coordinador Demo',
        rol: 'COORDINADOR',
      });
      navigate('/nueva');
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function entrarConIdToken(idToken) {
    setLoading(true);
    setError(null);
    try {
      const data = await authApi.loginGoogle(idToken);
      login(data);
      navigate('/nueva');
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="card" style={{ maxWidth: 480, margin: '4rem auto' }}>
      <h1>Focus — Iniciar sesion</h1>
      <p className="muted">Acceso restringido a cuentas corporativas autorizadas.</p>

      {error && <div className="alert alert-error">{error}</div>}

      <button
        className="btn"
        style={{ width: '100%', marginBottom: '0.75rem' }}
        disabled={loading}
        onClick={() => {
          const t = window.prompt('Pega aqui el ID token de Google (solo prod):');
          if (t) entrarConIdToken(t);
        }}
      >
        Iniciar con Google
      </button>

      <button
        className="btn btn-secondary"
        style={{ width: '100%' }}
        disabled={loading}
        onClick={entrarComoDemo}
      >
        Continuar como demo (solo dev)
      </button>
    </div>
  );
}
