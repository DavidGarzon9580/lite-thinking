import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { listarEmpresas } from '../services/empresas';
import { useApi } from '../hooks/useApi';
import { descargarInventario, enviarInventarioPorCorreo } from '../services/inventario';
import '../styles/InventarioPage.css';

export const InventarioPage: React.FC = () => {
  const api = useApi();
  const [empresaNit, setEmpresaNit] = useState<string>('');
  const [correo, setCorreo] = useState<string>('');
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const empresasQuery = useQuery({
    queryKey: ['empresas'],
    queryFn: () => listarEmpresas(api)
  });

  const downloadMutation = useMutation({
    mutationFn: () => descargarInventario(api, empresaNit),
    onSuccess: (blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `inventario-${empresaNit}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
      setMessage('Inventario descargado correctamente');
    },
    onError: () => setError('No fue posible descargar el inventario')
  });

  const emailMutation = useMutation({
    mutationFn: () => enviarInventarioPorCorreo(api, empresaNit, correo),
    onSuccess: () => {
      setMessage('Solicitud enviada. Revisa los logs o tu correo dependiendo de la configuración.');
      setCorreo('');
    },
    onError: () => setError('No fue posible enviar el inventario por correo')
  });

  const handleDownload = () => {
    setError(null);
    setMessage(null);
    if (!empresaNit) {
      setError('Selecciona una empresa');
      return;
    }
    downloadMutation.mutate();
  };

  const handleSendEmail = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setMessage(null);
    if (!empresaNit) {
      setError('Selecciona una empresa');
      return;
    }
    emailMutation.mutate();
  };

  return (
    <main className="content" id="main-content" role="main">
      <div className="card">
        <h2>Inventario por empresa</h2>
        <p>Genera el PDF del inventario y envíalo usando la integración con AWS SES (o el stub local).</p>

        <div className="input-group select-compact">
          <label htmlFor="empresaNit">Empresa</label>
          <select
            id="empresaNit"
            value={empresaNit}
            onChange={(event) => setEmpresaNit(event.target.value)}
          >
            <option value="">Seleccione una empresa</option>
            {empresasQuery.data?.map((empresa) => (
              <option key={empresa.nit} value={empresa.nit}>
                {empresa.nombre}
              </option>
            ))}
          </select>
        </div>

        <div className="inventory-actions">
          <button
            className="btn-primary"
            onClick={handleDownload}
            disabled={downloadMutation.isPending}
            aria-busy={downloadMutation.isPending}
          >
            Descargar PDF
          </button>
        </div>

        <form
          className="form-grid inventory-form"
          onSubmit={handleSendEmail}
          aria-describedby={error ? 'inventario-error' : undefined}
        >
          <div className="input-group">
            <label htmlFor="correo">Enviar por correo</label>
            <input
              id="correo"
              type="email"
              value={correo}
              onChange={(event) => setCorreo(event.target.value)}
              placeholder="destinatario@correo.com"
              required
            />
          </div>
          <button
            className="btn-secondary"
            type="submit"
            disabled={emailMutation.isPending}
            aria-busy={emailMutation.isPending}
          >
            Enviar PDF
          </button>
        </form>

        {message && (
          <p className="text-success" role="status" aria-live="polite">
            {message}
          </p>
        )}
        {error && (
          <p className="error" id="inventario-error" role="alert" aria-live="assertive">
            {error}
          </p>
        )}
      </div>
    </main>
  );
};
