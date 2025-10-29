import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { LoginPage } from './LoginPage';

const renderWithProviders = () =>
  render(
    <MemoryRouter>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>
  );

describe('LoginPage', () => {
  it('debe mostrar formulario vacío por defecto', () => {
    renderWithProviders();

    const emailInput = screen.getByLabelText(/correo/i) as HTMLInputElement;
    const passwordInput = screen.getByLabelText(/contraseña/i) as HTMLInputElement;

    expect(emailInput.value).toBe('');
    expect(passwordInput.value).toBe('');
    expect(screen.getByRole('button', { name: /ingresar/i })).toBeEnabled();
  });
});
