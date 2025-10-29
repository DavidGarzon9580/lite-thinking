import { AxiosInstance } from 'axios';
import { Producto } from '../types';
import { PrecioRequest } from './types';

export type ProductoInput = {
  codigo: string;
  nombre: string;
  caracteristicas?: string;
  empresaNit: string;
  precios: PrecioRequest[];
  categorias: string[];
};

export const listarProductosPorEmpresa = async (
  api: AxiosInstance,
  empresaNit: string
): Promise<Producto[]> => {
  const { data } = await api.get<Producto[]>(`/productos`, { params: { empresaNit } });
  return data;
};

export const crearProducto = async (api: AxiosInstance, payload: ProductoInput): Promise<Producto> => {
  const { data } = await api.post<Producto>('/productos', payload);
  return data;
};

export const actualizarProducto = async (
  api: AxiosInstance,
  id: string,
  payload: ProductoInput
): Promise<Producto> => {
  const { data } = await api.put<Producto>(`/productos/${id}`, payload);
  return data;
};

export const eliminarProducto = async (api: AxiosInstance, id: string): Promise<void> => {
  await api.delete(`/productos/${id}`);
};
