import { Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import Login from './pages/Login.jsx';
import NuevaSolicitud from './pages/NuevaSolicitud.jsx';
import MisTareas from './pages/MisTareas.jsx';
import Reportes from './pages/Reportes.jsx';
import Clientes from './pages/Clientes.jsx';

function Navbar() {
  const { user, logout } = useAuth();
  if (!user) return null;
  return (
    <nav className="navbar">
      <div>
        <NavLink to="/nueva" className={({ isActive }) => (isActive ? 'active' : '')}>
          Nueva solicitud
        </NavLink>
        <NavLink to="/tareas" className={({ isActive }) => (isActive ? 'active' : '')}>
          Mis tareas
        </NavLink>
        <NavLink to="/clientes" className={({ isActive }) => (isActive ? 'active' : '')}>
          Clientes
        </NavLink>
        <NavLink to="/reportes" className={({ isActive }) => (isActive ? 'active' : '')}>
          Reportes
        </NavLink>
      </div>
      <div>
        <span className="muted">{user.nombre} ({user.rol})</span>
        <button className="btn btn-secondary" style={{ marginLeft: '1rem' }} onClick={logout}>
          Salir
        </button>
      </div>
    </nav>
  );
}

function Protegida({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
    <>
      <Navbar />
      <main className="layout">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/nueva" element={<Protegida><NuevaSolicitud /></Protegida>} />
          <Route path="/tareas" element={<Protegida><MisTareas /></Protegida>} />
          <Route path="/clientes" element={<Protegida><Clientes /></Protegida>} />
          <Route path="/reportes" element={<Protegida><Reportes /></Protegida>} />
          <Route path="*" element={<Navigate to="/nueva" replace />} />
        </Routes>
      </main>
    </>
  );
}
