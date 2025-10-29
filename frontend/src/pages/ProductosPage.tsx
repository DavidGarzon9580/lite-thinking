import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useApi } from '../hooks/useApi';
import { listarEmpresas } from '../services/empresas';
import { listarCategorias } from '../services/categorias';
import {
  listarProductosPorEmpresa,
  crearProducto,
  actualizarProducto,
  eliminarProducto
} from '../services/productos';
import { Categoria, Empresa, Producto } from '../types';
import { PrecioRequest } from '../services/types';
import '../styles/ProductosPage.css';

type FormState = {
  id?: string;
  codigo: string;
  nombre: string;
  caracteristicas: string;
  empresaNit: string;
  precios: PrecioRequest[];
  categorias: string;
};

const defaultPrice: PrecioRequest = { moneda: 'COP', valor: 0 };

const emptyForm: FormState = {
  codigo: '',
  nombre: '',
  caracteristicas: '',
  empresaNit: '',
  precios: [defaultPrice],
  categorias: ''
};

export const ProductosPage: React.FC = () => {
  const api = useApi();
  const queryClient = useQueryClient();
  const [form, setForm] = useState<FormState>(emptyForm);
  const [selectedEmpresa, setSelectedEmpresa] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const empresasQuery = useQuery({
    queryKey: ['empresas'],
    queryFn: () => listarEmpresas(api)
  });

  const categoriasQuery = useQuery({
    queryKey: ['categorias'],
    queryFn: () => listarCategorias(api)
  });

  const productosQuery = useQuery({
    queryKey: ['productos', selectedEmpresa],
    queryFn: () => listarProductosPorEmpresa(api, selectedEmpresa),
    enabled: Boolean(selectedEmpresa)
  });

  useEffect(() => {
    if (!selectedEmpresa && empresasQuery.data?.length) {
      const first = empresasQuery.data[0];
      setSelectedEmpresa(first.nit);
      setForm((prev) => ({ ...prev, empresaNit: first.nit }));
    }
  }, [empresasQuery.data, selectedEmpresa]);

  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handlePriceChange = (index: number, field: keyof PrecioRequest, value: string) => {
    setForm((prev) => {
      const precios = prev.precios.map((price, idx) =>
        idx === index ? { ...price, [field]: field === 'valor' ? Number(value) : value.toUpperCase() } : price
      );
      return { ...prev, precios };
    });
  };

  const addPrice = () => {
    setForm((prev) => ({
      ...prev,
      precios: [...prev.precios, { moneda: 'USD', valor: 0 }]
    }));
  };

  const removePrice = (index: number) => {
    setForm((prev) => ({
      ...prev,
      precios: prev.precios.filter((_, idx) => idx !== index)
    }));
  };

  const createMutation = useMutation({
    mutationFn: () =>
      crearProducto(api, {
        codigo: form.codigo,
        nombre: form.nombre,
        caracteristicas: form.caracteristicas,
        empresaNit: selectedEmpresa,
        precios: form.precios,
        categorias: form.categorias.split(',').map((cat) => cat.trim()).filter(Boolean)
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['productos', selectedEmpresa] });
      setForm((prev) => ({ ...emptyForm, empresaNit: selectedEmpresa || prev.empresaNit }));
      setSuccess('Producto registrado correctamente.');
      setError(null);
    },
    onError: () => {
      setError('No fue posible registrar el producto');
      setSuccess(null);
    }
  });

  const updateMutation = useMutation({
    mutationFn: () =>
      actualizarProducto(api, form.id!, {
        codigo: form.codigo,
        nombre: form.nombre,
        caracteristicas: form.caracteristicas,
        empresaNit: selectedEmpresa,
        precios: form.precios,
        categorias: form.categorias.split(',').map((cat) => cat.trim()).filter(Boolean)
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['productos', selectedEmpresa] });
      setForm((prev) => ({ ...emptyForm, empresaNit: selectedEmpresa || prev.empresaNit }));
      setSuccess('Producto actualizado correctamente.');
      setError(null);
    },
    onError: () => {
      setError('No fue posible actualizar el producto');
      setSuccess(null);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => eliminarProducto(api, id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['productos', selectedEmpresa] });
      setSuccess('Producto eliminado.');
      setError(null);
    },
    onError: () => {
      setError('No fue posible eliminar el producto');
      setSuccess(null);
    }
  });

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);

    if (!selectedEmpresa) {
      setError('Seleccione una empresa para asociar el producto');
      return;
    }

    if (form.id) {
      updateMutation.mutate();
    } else {
      createMutation.mutate();
    }
  };

  const handleEdit = (producto: Producto) => {
    setForm({
      id: producto.id,
      codigo: producto.codigo,
      nombre: producto.nombre,
      caracteristicas: producto.caracteristicas ?? '',
      empresaNit: producto.empresaNit,
      precios: producto.precios.map((price) => ({ moneda: price.moneda, valor: price.valor })),
      categorias: producto.categorias.join(', ')
    });
    setSelectedEmpresa(producto.empresaNit);
  };

  const categoriesSuggestions = useMemo(() => categoriasQuery.data?.map((cat) => cat.nombre) ?? [], [categoriasQuery.data]);

  return (
    <main className="content" id="main-content" role="main">
      <div className="card">
        <h2>Gestión de productos</h2>
        <p>Registra productos por empresa, define precios en diferentes monedas y categorízalos.</p>

          {error && (
            <p className="error" role="alert" aria-live="assertive" id="producto-error">
              {error}
            </p>
          )}
          {success && (
            <p className="text-success" role="status" aria-live="polite">
              {success}
            </p>
          )}

        <div className="input-group select-compact">
          <label htmlFor="empresaNit">Empresa</label>
          <select
            id="empresaNit"
            name="empresaNit"
            value={selectedEmpresa}
            onChange={(event) => {
              setSelectedEmpresa(event.target.value);
              setForm((prev) => ({ ...prev, empresaNit: event.target.value }));
            }}
          >
            <option value="">Seleccione una empresa</option>
            {empresasQuery.data?.map((empresa: Empresa) => (
              <option key={empresa.nit} value={empresa.nit}>
                {empresa.nombre}
              </option>
            ))}
          </select>
        </div>

        <form className="form-grid" onSubmit={handleSubmit} aria-describedby={error ? 'producto-error' : undefined}>
          <div className="input-group">
            <label htmlFor="codigo">Código</label>
            <input id="codigo" name="codigo" value={form.codigo} onChange={handleChange} required />
          </div>

          <div className="input-group">
            <label htmlFor="nombre">Nombre</label>
            <input id="nombre" name="nombre" value={form.nombre} onChange={handleChange} required />
          </div>

          <div className="input-group">
            <label htmlFor="caracteristicas">Características</label>
            <textarea
              id="caracteristicas"
              name="caracteristicas"
              value={form.caracteristicas}
              onChange={handleChange}
              rows={3}
            />
          </div>

          <div className="input-group">
            <label htmlFor="categorias">Categorías (separadas por coma)</label>
            <input
              id="categorias"
              name="categorias"
              value={form.categorias}
              onChange={handleChange}
              list="categorias-list"
            />
            <datalist id="categorias-list">
              {categoriesSuggestions.map((cat) => (
                <option key={cat} value={cat} />
              ))}
            </datalist>
          </div>

          <div>
            <strong>Precios</strong>
            {form.precios.map((precio, index) => (
              <div key={index} className="price-row">
                <input
                  placeholder="Moneda"
                  value={precio.moneda}
                  maxLength={3}
                  onChange={(event) => handlePriceChange(index, 'moneda', event.target.value)}
                  required
                />
                <input
                  placeholder="Valor"
                  type="number"
                  min={0}
                  value={precio.valor}
                  onChange={(event) => handlePriceChange(index, 'valor', event.target.value)}
                  required
                />
                {form.precios.length > 1 && (
                  <button className="btn-secondary" type="button" onClick={() => removePrice(index)}>
                    Quitar
                  </button>
                )}
              </div>
            ))}

            <button className="btn-secondary" type="button" onClick={addPrice}>
              Añadir precio
            </button>
          </div>

          <button
            className="btn-primary"
            type="submit"
            disabled={createMutation.isPending || updateMutation.isPending}
            aria-busy={createMutation.isPending || updateMutation.isPending}
          >
            {form.id ? 'Actualizar producto' : 'Crear producto'}
          </button>
        </form>

        <section className="section-spaced">
          <h3>Inventario de la empresa</h3>

          {productosQuery.isLoading && (
            <p role="status" aria-live="polite">
              Cargando productos...
            </p>
          )}

          <div className="table-wrapper">
            <table>
              <caption className="visually-hidden">Productos registrados por empresa</caption>
              <thead>
                <tr>
                  <th scope="col">Código</th>
                  <th scope="col">Nombre</th>
                  <th scope="col">Categorías</th>
                  <th scope="col">Precios</th>
                  <th scope="col">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {productosQuery.data?.map((producto: Producto) => (
                  <tr key={producto.id}>
                    <td>{producto.codigo}</td>
                    <td>{producto.nombre}</td>
                    <td>{producto.categorias.join(', ')}</td>
                    <td>
                      {producto.precios.map((precio) => (
                        <div key={precio.moneda}>
                          {precio.moneda}: {precio.valor}
                        </div>
                      ))}
                    </td>
                    <td className="table-actions">
                      <button className="btn-secondary" onClick={() => handleEdit(producto)}>
                        Editar
                      </button>
                      <button
                        className="btn-secondary btn-danger"
                        onClick={() => deleteMutation.mutate(producto.id)}
                      >
                        Eliminar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </main>
  );
};
