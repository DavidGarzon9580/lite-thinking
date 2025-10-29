import { describe, expect, vi, beforeEach, afterEach, it } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { EmpresasPage } from './EmpresasPage';
import type { Empresa } from '../../types';

const useAuthMock = vi.hoisted(() => vi.fn());

const empresasServiceMocks = vi.hoisted(() => ({
  listarEmpresas: vi.fn(),
  crearEmpresa: vi.fn(),
  actualizarEmpresa: vi.fn(),
  eliminarEmpresa: vi.fn()
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: useAuthMock
}));

vi.mock('../../hooks/useApi', () => ({
  useApi: () => ({})
}));

vi.mock('../../services/empresas', () => empresasServiceMocks);

const renderWithClient = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, cacheTime: 0 },
      mutations: { retry: false }
    }
  });

  render(
    <QueryClientProvider client={queryClient}>
      <EmpresasPage />
    </QueryClientProvider>
  );

  return queryClient;
};

describe('EmpresasPage', () => {
  const empresasStub: Empresa[] = [
    {
      nit: '900123456',
      nombre: 'Lite Thinking',
      direccion: 'Calle 1 #2-3',
      telefono: '+57 312 000 1111',
      productos: [
        {
          id: '1',
          codigo: 'PROD-01',
          nombre: 'Laptop'
        }
      ]
    }
  ];

  beforeEach(() => {
    useAuthMock.mockReturnValue({
      token: 'token',
      role: 'ADMIN',
      email: 'admin@litethinking.com',
      login: vi.fn(),
      logout: vi.fn()
    });
    empresasServiceMocks.listarEmpresas.mockResolvedValue(empresasStub);
    empresasServiceMocks.crearEmpresa.mockResolvedValue({
      ...empresasStub[0],
      nit: '901000999',
      nombre: 'Nueva Empresa'
    });
    empresasServiceMocks.actualizarEmpresa.mockResolvedValue(empresasStub[0]);
    empresasServiceMocks.eliminarEmpresa.mockResolvedValue(undefined);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('muestra la lista de empresas', async () => {
    const client = renderWithClient();

    await waitFor(() => expect(empresasServiceMocks.listarEmpresas).toHaveBeenCalled());

    expect(await screen.findByText('Lite Thinking')).toBeInTheDocument();
    expect(screen.getByText('Empresas registradas')).toBeVisible();
    client.clear();
  });

  it('permite registrar una nueva empresa y muestra mensaje de exito', async () => {
    const queryClient = renderWithClient();

    await waitFor(() => expect(empresasServiceMocks.listarEmpresas).toHaveBeenCalledTimes(1));

    fireEvent.change(screen.getByLabelText(/NIT/i), { target: { value: '901000999' } });
    fireEvent.change(screen.getByLabelText(/Nombre/i), { target: { value: 'Nueva Empresa' } });
    fireEvent.change(screen.getByLabelText(/Direccion/i), { target: { value: 'Calle 123' } });
    fireEvent.change(screen.getByLabelText(/Telefono/i), { target: { value: '3000000000' } });

    const form = screen.getByLabelText(/Nombre/i).closest('form');
    if (!form) {
      throw new Error('Formulario no encontrado');
    }
    fireEvent.submit(form);

    await waitFor(() => expect(empresasServiceMocks.crearEmpresa).toHaveBeenCalled());
    expect(empresasServiceMocks.crearEmpresa.mock.calls[0][1]).toEqual({
      nit: '901000999',
      nombre: 'Nueva Empresa',
      direccion: 'Calle 123',
      telefono: '3000000000'
    });

    await waitFor(() => expect(screen.getByText('Empresa creada correctamente.')).toBeInTheDocument());

    await waitFor(() => expect(empresasServiceMocks.listarEmpresas).toHaveBeenCalledTimes(2));

    queryClient.clear();
  });
});
