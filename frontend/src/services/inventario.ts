import { AxiosInstance } from 'axios';

export const descargarInventario = async (api: AxiosInstance, empresaNit: string): Promise<Blob> => {
  const { data } = await api.get(`/inventory/${empresaNit}/pdf`, {
    responseType: 'blob'
  });
  return data;
};

export const enviarInventarioPorCorreo = async (
  api: AxiosInstance,
  empresaNit: string,
  emailDestino: string
): Promise<void> => {
  await api.post(`/inventory/${empresaNit}/email`, { emailDestino });
};
