import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { AxiosError } from 'axios';
import { ProductosPage } from './ProductosPage';

const listarEmpresasMock = vi.hoisted(() => vi.fn());
const listarCategoriasMock = vi.hoisted(() => vi.fn());
const listarProductosMock = vi.hoisted(() => vi.fn());
const crearProductoMock = vi.hoisted(() => vi.fn());
const actualizarProductoMock = vi.hoisted(() => vi.fn());
const eliminarProductoMock = vi.hoisted(() => vi.fn());

vi.mock('../../hooks/useApi', () => ({
  useApi: () => ({})
}));

vi.mock('../../services/empresas', () => ({
  listarEmpresas: (api: unknown) => listarEmpresasMock(api)
}));

vi.mock('../../services/categorias', () => ({
  listarCategorias: (api: unknown) => listarCategoriasMock(api)
}));

vi.mock('../../services/productos', () => ({
  listarProductosPorEmpresa: (api: unknown, nit: string) => listarProductosMock(api, nit),
  crearProducto: (api: unknown, payload: unknown) => crearProductoMock(api, payload),
  actualizarProducto: (api: unknown, id: string, payload: unknown) => actualizarProductoMock(api, id, payload),
  eliminarProducto: (api: unknown, id: string) => eliminarProductoMock(api, id)
}));

const renderPage = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, cacheTime: 0 },
      mutations: { retry: false }
    }
  });

  render(
    <QueryClientProvider client={queryClient}>
      <ProductosPage />
    </QueryClientProvider>
  );

  return queryClient;
};

beforeEach(() => {
  listarEmpresasMock.mockResolvedValue([
    { nit: '900123456', nombre: 'Lite Thinking', direccion: '', telefono: '', productos: [] }
  ]);
  listarCategoriasMock.mockResolvedValue([{ id: '1', nombre: 'Tecnologia' }]);
  listarProductosMock.mockResolvedValue([
    {
      id: 'prod-1',
      codigo: 'PROD-01',
      nombre: 'Laptop',
      caracteristicas: '16GB RAM',
      empresaNit: '900123456',
      precios: [{ moneda: 'COP', valor: 4500000 }],
      categorias: ['Tecnologia']
    }
  ]);
  crearProductoMock.mockResolvedValue(undefined);
  actualizarProductoMock.mockResolvedValue(undefined);
  eliminarProductoMock.mockResolvedValue(undefined);
});

afterEach(() => {
  vi.clearAllMocks();
});

describe('ProductosPage', () => {
  it('lista productos y permite crear uno nuevo', async () => {
    renderPage();

    await waitFor(() => expect(listarProductosMock).toHaveBeenCalledWith({}, '900123456'));

    expect(await screen.findByText('Laptop')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText(/Codigo/i), { target: { value: 'PROD-02' } });
    fireEvent.change(screen.getByLabelText(/Nombre/i), { target: { value: 'Mouse MX' } });
    fireEvent.change(screen.getByLabelText(/Caracteristicas/i), { target: { value: 'Inalambrico' } });
    fireEvent.change(screen.getByLabelText(/Categorias/i), { target: { value: 'Perifericos' } });

    const monedaInput = screen.getAllByPlaceholderText(/Moneda/i)[0] as HTMLInputElement;
    const valorInput = screen.getAllByPlaceholderText(/Valor/i)[0] as HTMLInputElement;
    fireEvent.change(monedaInput, { target: { value: 'USD' } });
    fireEvent.change(valorInput, { target: { value: '99' } });

    fireEvent.click(screen.getByRole('button', { name: /Crear producto/i }));

    await waitFor(() => expect(crearProductoMock).toHaveBeenCalled());
    const [, payload] = crearProductoMock.mock.calls[0];
    expect(payload).toMatchObject({
      codigo: 'PROD-02',
      nombre: 'Mouse MX',
      caracteristicas: 'Inalambrico',
      precios: [{ moneda: 'USD', valor: 99 }],
      categorias: ['Perifericos']
    });
    await waitFor(() => expect(screen.getByText('Producto registrado correctamente.')).toBeInTheDocument());
  });

  it('muestra mensaje de error cuando la creacion falla', async () => {
    const axiosError = {
      isAxiosError: true,
      response: { data: { message: 'Codigo duplicado' } }
    } as AxiosError;
    crearProductoMock.mockRejectedValueOnce(axiosError);

    renderPage();
    await waitFor(() => expect(listarProductosMock).toHaveBeenCalled());

    fireEvent.change(screen.getByLabelText(/Codigo/i), { target: { value: 'PROD-01' } });
    fireEvent.change(screen.getByLabelText(/Nombre/i), { target: { value: 'Laptop' } });
    fireEvent.click(screen.getByRole('button', { name: /Crear producto/i }));

    await waitFor(() => expect(screen.getByText('Codigo duplicado')).toBeInTheDocument());
  });

  it('permite eliminar un producto existente', async () => {
    renderPage();
    await waitFor(() => expect(listarProductosMock).toHaveBeenCalled());

    const deleteButtons = await screen.findAllByRole('button', { name: /Eliminar/i });
    fireEvent.click(deleteButtons[0]);

    await waitFor(() => expect(eliminarProductoMock).toHaveBeenCalledWith({}, 'prod-1'));
    await waitFor(() => expect(screen.getByText('Producto eliminado.')).toBeInTheDocument());
  });
});
