import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { useApi } from '../../hooks/useApi';
import {
  listarEmpresas,
  crearEmpresa,
  actualizarEmpresa,
  eliminarEmpresa,
  EmpresaInput,
  EmpresaUpdateInput
} from '../../services/empresas';
import { useAuth } from '../../context/AuthContext';
import { Empresa } from '../../types';
import './EmpresasPage.css';

const emptyForm: EmpresaInput = {
  nit: '',
  nombre: '',
  direccion: '',
  telefono: ''
};

const resolveErrorMessage = (fallback: string, cause: unknown) => {
  if (isAxiosError(cause)) {
    const candidate = cause.response?.data as { message?: string };
    if (candidate?.message) {
      return candidate.message;
    }
  }
  return fallback;
};

export const EmpresasPage: React.FC = () => {
  const api = useApi();
  const queryClient = useQueryClient();
  const { role } = useAuth();
  const [form, setForm] = useState<EmpresaInput>(emptyForm);
  const [editingNit, setEditingNit] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const { data: empresas, isLoading } = useQuery({
    queryKey: ['empresas'],
    queryFn: () => listarEmpresas(api)
  });

  const resetForm = () => {
    setForm(emptyForm);
    setEditingNit(null);
    setError(null);
    setSuccess(null);
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const createMutation = useMutation({
    mutationFn: (payload: EmpresaInput) => crearEmpresa(api, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
      resetForm();
      setSuccess('Empresa creada correctamente.');
    },
    onError: (cause) => {
      setError(resolveErrorMessage('No fue posible crear la empresa', cause));
      setSuccess(null);
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ nit, data }: { nit: string; data: EmpresaUpdateInput }) => actualizarEmpresa(api, nit, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
      resetForm();
      setSuccess('Empresa actualizada correctamente.');
    },
    onError: (cause) => {
      setError(resolveErrorMessage('No fue posible actualizar la empresa', cause));
      setSuccess(null);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (nit: string) => eliminarEmpresa(api, nit),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresas'] });
      setSuccess('Empresa eliminada.');
      setError(null);
    },
    onError: (cause) => {
      setError(resolveErrorMessage('No fue posible eliminar la empresa', cause));
      setSuccess(null);
    }
  });

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    if (editingNit) {
      const { nit, ...rest } = form;
      updateMutation.mutate({ nit: editingNit, data: rest });
    } else {
      createMutation.mutate(form);
    }
  };

  const handleEdit = (empresa: Empresa) => {
    setForm({
      nit: empresa.nit,
      nombre: empresa.nombre,
      direccion: empresa.direccion,
      telefono: empresa.telefono
    });
    setEditingNit(empresa.nit);
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending;
  const hasFeedback = Boolean(error || success);
  const feedbackClass = useMemo(() => {
    if (error) return 'feedback-banner error';
    if (success) return 'feedback-banner success';
    return undefined;
  }, [error, success]);

  return (
    <main className="content" id="main-content" role="main">
      <div className="card">
        <h2>Empresas registradas</h2>
        <p>Visualiza y administra la informacion principal de las empresas.</p>

        {isLoading && (
          <p role="status" aria-live="polite">
            Cargando...
          </p>
        )}

        {hasFeedback && (
          <div
            className={feedbackClass}
            role={error ? 'alert' : 'status'}
            aria-live={error ? 'assertive' : 'polite'}
            id={error ? 'empresa-error' : undefined}
          >
            {error ?? success}
          </div>
        )}

        {role === 'ADMIN' && (
          <form
            className="form-grid two-cols"
            onSubmit={handleSubmit}
            aria-describedby={error ? 'empresa-error' : undefined}
          >
            <div className="input-group">
              <label htmlFor="nit">NIT</label>
              <input
                id="nit"
                name="nit"
                value={form.nit}
                onChange={handleChange}
                required
                disabled={Boolean(editingNit)}
              />
            </div>

            <div className="input-group">
              <label htmlFor="nombre">Nombre</label>
              <input id="nombre" name="nombre" value={form.nombre} onChange={handleChange} required />
            </div>

            <div className="input-group">
              <label htmlFor="direccion">Direccion</label>
              <input id="direccion" name="direccion" value={form.direccion} onChange={handleChange} required />
            </div>

            <div className="input-group">
              <label htmlFor="telefono">Telefono</label>
              <input id="telefono" name="telefono" value={form.telefono} onChange={handleChange} required />
            </div>

            <div className="form-actions">
              <button className="btn-primary" type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
                {editingNit ? 'Actualizar' : 'Crear'}
              </button>
              {editingNit && (
                <button type="button" className="btn-secondary" onClick={resetForm}>
                  Cancelar
                </button>
              )}
            </div>
          </form>
        )}

        <div className="table-wrapper">
          <table>
            <caption className="visually-hidden">Listado de empresas registradas</caption>
            <thead>
              <tr>
                <th scope="col">NIT</th>
                <th scope="col">Nombre</th>
                <th scope="col">Direccion</th>
                <th scope="col">Telefono</th>
                <th scope="col">Productos</th>
                {role === 'ADMIN' && <th scope="col">Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {empresas?.map((empresa) => (
                <tr key={empresa.nit}>
                  <td>{empresa.nit}</td>
                  <td>{empresa.nombre}</td>
                  <td>{empresa.direccion}</td>
                  <td>{empresa.telefono}</td>
                  <td>{empresa.productos.length}</td>
                  {role === 'ADMIN' && (
                    <td className="table-actions">
                      <button className="btn-secondary" onClick={() => handleEdit(empresa)}>
                        Editar
                      </button>
                      <button className="btn-secondary btn-danger" onClick={() => deleteMutation.mutate(empresa.nit)}>
                        Eliminar
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </main>
  );
};
