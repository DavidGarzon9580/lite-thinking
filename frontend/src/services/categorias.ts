import { AxiosInstance } from 'axios';
import { Categoria } from '../types';

export const listarCategorias = async (api: AxiosInstance): Promise<Categoria[]> => {
  const { data } = await api.get<Categoria[]>('/categorias');
  return data;
};
