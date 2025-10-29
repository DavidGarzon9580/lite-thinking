import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import type { AxiosError } from 'axios';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../../context/AuthContext';
import { LoginPage } from './LoginPage';

const loginMock = vi.fn();
const navigateMock = vi.fn();

vi.mock('../../services/auth', () => ({
  login: (payload: { email: string; password: string }) => loginMock(payload)
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock
  };
});

const renderWithProviders = () =>
  render(
    <MemoryRouter>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>
  );

const createToken = (email: string, role: 'ADMIN' | 'VIEWER') => {
  const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64url');
  const payload = Buffer.from(
    JSON.stringify({
      sub: email,
      role,
      exp: Math.floor(Date.now() / 1000) + 3600
    })
  ).toString('base64url');
  return `${header}.${payload}.signature`;
};

describe('LoginPage', () => {
  beforeEach(() => {
    loginMock.mockReset();
    navigateMock.mockReset();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('debe mostrar formulario vacio por defecto', () => {
    renderWithProviders();

    const emailInput = screen.getByLabelText(/correo/i) as HTMLInputElement;
    const passwordInput = screen.getByLabelText(/contrasena/i) as HTMLInputElement;

    expect(emailInput.value).toBe('');
    expect(passwordInput.value).toBe('');
    expect(screen.getByRole('button', { name: /ingresar/i })).toBeEnabled();
  });

  it('debe iniciar sesion correctamente y redirigir a empresas', async () => {
    const token = createToken('admin@litethinking.com', 'ADMIN');
    loginMock.mockResolvedValue({ token, expiresInMinutes: 60 });

    renderWithProviders();

    fireEvent.change(screen.getByLabelText(/correo/i), { target: { value: 'admin@litethinking.com' } });
    fireEvent.change(screen.getByLabelText(/contrasena/i), { target: { value: 'Admin123*' } });

    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));

    await waitFor(() => {
      expect(loginMock).toHaveBeenCalledWith({ email: 'admin@litethinking.com', password: 'Admin123*' });
      expect(navigateMock).toHaveBeenCalledWith('/empresas');
    });
  });

  it('debe mostrar el mensaje de error del backend al fallar el login', async () => {
    const axiosError = {
      isAxiosError: true,
      response: { data: { message: 'Credenciales incorrectas' } }
    } as AxiosError;
    loginMock.mockRejectedValueOnce(axiosError);

    renderWithProviders();

    fireEvent.change(screen.getByLabelText(/correo/i), { target: { value: 'admin@litethinking.com' } });
    fireEvent.change(screen.getByLabelText(/contrasena/i), { target: { value: 'Admin123*' } });
    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));

    await waitFor(() => {
      expect(screen.getByText('Credenciales incorrectas')).toBeInTheDocument();
    });
    expect(navigateMock).not.toHaveBeenCalled();
  });
});
