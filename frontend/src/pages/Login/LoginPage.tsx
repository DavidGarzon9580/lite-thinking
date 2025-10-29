import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { isAxiosError } from 'axios';
import { login } from '../../services/auth';
import { useAuth } from '../../context/AuthContext';
import './LoginPage.css';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login: setToken } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const response = await login({ email, password });
      setToken(response.token);
      navigate('/empresas');
    } catch (err: unknown) {
      if (isAxiosError(err)) {
        const payload = err.response?.data as { message?: string };
        setError(payload?.message ?? 'No fue posible iniciar sesion. Verifica tus credenciales.');
      } else {
        setError('No fue posible iniciar sesion. Verifica tus credenciales.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="content" id="main-content" role="main">
      <div className="card card-narrow">
        <h2>Iniciar sesion</h2>
        <p>Ingresa con tu correo y contrasena registrados.</p>
        <form className="form-grid" onSubmit={handleSubmit} aria-describedby={error ? 'login-error' : undefined}>
          <div className="input-group">
            <label htmlFor="email">Correo</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              placeholder="tucorreo@empresa.com"
              autoComplete="username"
            />
          </div>

          <div className="input-group">
            <label htmlFor="password">Contrasena</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              placeholder="Contrasena"
              aria-required="true"
              autoComplete="current-password"
            />
          </div>

          {error && (
            <div className="feedback-banner error" id="login-error" role="alert" aria-live="assertive">
              {error}
            </div>
          )}

          <button className="btn-primary" type="submit" disabled={loading} aria-busy={loading}>
            {loading ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>
      </div>
    </main>
  );
};
