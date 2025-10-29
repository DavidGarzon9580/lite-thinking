import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Header: React.FC = () => {
  const { token, role, logout } = useAuth();

  return (
    <header className="header">
      <nav aria-label="Navegacion principal">
        <Link to={token ? '/home' : '/login'}>Lite Thinking</Link>
        {token && (
          <ul className="nav-links" role="list">
            <li>
              <NavLink to="/home">Inicio</NavLink>
            </li>
            <li>
              <NavLink to="/empresas">Empresas</NavLink>
            </li>
            {role === 'ADMIN' && (
              <>
                <li>
                  <NavLink to="/productos">Productos</NavLink>
                </li>
                <li>
                  <NavLink to="/inventario">Inventario</NavLink>
                </li>
              </>
            )}
          </ul>
        )}
        <div>
          {token ? (
            <button className="btn-secondary" onClick={logout}>
              Cerrar sesión
            </button>
          ) : (
            <Link to="/login" className="btn-primary">
              Ingresar
            </Link>
          )}
        </div>
      </nav>
    </header>
  );
};
