import { AxiosInstance } from 'axios';
import { Empresa } from '../types';

export type EmpresaInput = {
  nit: string;
  nombre: string;
  direccion: string;
  telefono: string;
};

export type EmpresaUpdateInput = Omit<EmpresaInput, 'nit'>;

export const listarEmpresas = async (api: AxiosInstance): Promise<Empresa[]> => {
  const { data } = await api.get<Empresa[]>('/empresas');
  return data;
};

export const crearEmpresa = async (api: AxiosInstance, payload: EmpresaInput): Promise<Empresa> => {
  const { data } = await api.post<Empresa>('/empresas', payload);
  return data;
};

export const actualizarEmpresa = async (
  api: AxiosInstance,
  nit: string,
  payload: EmpresaUpdateInput
): Promise<Empresa> => {
  const { data } = await api.put<Empresa>(`/empresas/${nit}`, payload);
  return data;
};

export const eliminarEmpresa = async (api: AxiosInstance, nit: string): Promise<void> => {
  await api.delete(`/empresas/${nit}`);
};
