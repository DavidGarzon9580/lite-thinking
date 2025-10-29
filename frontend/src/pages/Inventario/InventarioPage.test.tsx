import { beforeAll, beforeEach, afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { AxiosError } from 'axios';
import { InventarioPage } from './InventarioPage';

const listarEmpresasMock = vi.hoisted(() => vi.fn());
const descargarInventarioMock = vi.hoisted(() => vi.fn());
const enviarInventarioMock = vi.hoisted(() => vi.fn());

vi.mock('../../hooks/useApi', () => ({
  useApi: () => ({})
}));

vi.mock('../../services/empresas', () => ({
  listarEmpresas: (api: unknown) => listarEmpresasMock(api)
}));

vi.mock('../../services/inventario', () => ({
  descargarInventario: (api: unknown, empresaNit: string) => descargarInventarioMock(api, empresaNit),
  enviarInventarioPorCorreo: (api: unknown, empresaNit: string, correo: string) =>
    enviarInventarioMock(api, empresaNit, correo)
}));

let anchorClickMock: ReturnType<typeof vi.fn>;
let createObjectURLMock: ReturnType<typeof vi.fn>;
let revokeObjectURLMock: ReturnType<typeof vi.fn>;

beforeAll(() => {
  createObjectURLMock = vi.fn(() => 'blob:inventory');
  revokeObjectURLMock = vi.fn();
  anchorClickMock = vi.fn();

  Object.defineProperty(global.URL, 'createObjectURL', {
    writable: true,
    value: createObjectURLMock
  });
  Object.defineProperty(global.URL, 'revokeObjectURL', {
    writable: true,
    value: revokeObjectURLMock
  });

  vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(anchorClickMock);
});

beforeEach(() => {
  listarEmpresasMock.mockResolvedValue([
    { nit: '900123456', nombre: 'Lite Thinking', direccion: '', telefono: '', productos: [] }
  ]);
  descargarInventarioMock.mockReset();
  enviarInventarioMock.mockReset();
});

afterEach(() => {
  anchorClickMock.mockClear();
  createObjectURLMock.mockClear();
  revokeObjectURLMock.mockClear();
  vi.clearAllMocks();
});

afterAll(() => {
  vi.restoreAllMocks();
});

const renderWithClient = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, cacheTime: 0 },
      mutations: { retry: false }
    }
  });

  render(
    <QueryClientProvider client={queryClient}>
      <InventarioPage />
    </QueryClientProvider>
  );

  return queryClient;
};

describe('InventarioPage', () => {
  it('descarga correctamente el inventario y muestra mensaje de exito', async () => {
    descargarInventarioMock.mockResolvedValue(new Blob(['pdf']));

    const client = renderWithClient();
    await waitFor(() => expect(listarEmpresasMock).toHaveBeenCalled());
    const select = await screen.findByLabelText(/Empresa/i);
    await screen.findByRole('option', { name: 'Lite Thinking' });
    fireEvent.change(select, { target: { value: '900123456' } });

    const downloadButton = await screen.findByRole('button', { name: /Descargar PDF/i });
    await waitFor(() => expect(downloadButton).not.toBeDisabled());
    fireEvent.click(downloadButton);

    await waitFor(() =>
      expect(screen.getByText('Inventario descargado correctamente')).toBeInTheDocument()
    );
    expect(descargarInventarioMock).toHaveBeenCalledWith({}, '900123456');
    client.clear();
  });

  it('envia el inventario por correo y limpia el formulario', async () => {
    enviarInventarioMock.mockResolvedValue(undefined);
    const client = renderWithClient();
    await waitFor(() => expect(listarEmpresasMock).toHaveBeenCalled());
    const select = await screen.findByLabelText(/Empresa/i);
    await screen.findByRole('option', { name: 'Lite Thinking' });
    fireEvent.change(select, { target: { value: '900123456' } });

    const correoInput = screen.getByLabelText(/Enviar por correo/i) as HTMLInputElement;
    fireEvent.change(correoInput, { target: { value: 'destino@correo.com' } });

    const sendButton = await screen.findByRole('button', { name: /Enviar PDF/i });
    await waitFor(() => expect(sendButton).not.toBeDisabled());
    fireEvent.click(sendButton);

    await waitFor(() =>
      expect(
        screen.getByText('Solicitud enviada. Revisa los logs o tu correo dependiendo de la configuracion.')
      ).toBeInTheDocument()
    );
    expect(enviarInventarioMock).toHaveBeenCalledWith({}, '900123456', 'destino@correo.com');
    expect(correoInput.value).toBe('');
    client.clear();
  });

  it('muestra mensaje de error cuando la descarga falla', async () => {
    const axiosError = {
      isAxiosError: true,
      response: { data: { message: 'Fallo descarga' } }
    } as AxiosError;
    descargarInventarioMock.mockRejectedValueOnce(axiosError);

    const client = renderWithClient();
    await waitFor(() => expect(listarEmpresasMock).toHaveBeenCalled());
    const select = await screen.findByLabelText(/Empresa/i);
    await screen.findByRole('option', { name: 'Lite Thinking' });
    fireEvent.change(select, { target: { value: '900123456' } });

    const downloadButton = await screen.findByRole('button', { name: /Descargar PDF/i });
    await waitFor(() => expect(downloadButton).not.toBeDisabled());
    fireEvent.click(downloadButton);

    await waitFor(() => expect(screen.getByText('Fallo descarga')).toBeInTheDocument());
    client.clear();
  });
});
