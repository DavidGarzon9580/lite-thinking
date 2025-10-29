import { useAuth } from '../../context/AuthContext';

export const HomePage: React.FC = () => {
  const { role } = useAuth();

  return (
    <main className="content" id="main-content" role="main">
      <div className="card">
        <h1>Bienvenido a Lite Thinking Platform</h1>
        <p>
          Esta aplicacion permite gestionar empresas, productos y ordenes, ademas de generar inventarios en PDF y
          enviarlos a traves de AWS SES.
        </p>
        <ul>
          <li>Como visitante puedes consultar el listado de empresas.</li>
          <li>Los usuarios externos solo tienen acceso de lectura.</li>
          <li>Los administradores pueden crear y editar empresas y productos.</li>
          <li>Desde la seccion de inventario se genera el PDF y se puede enviar por correo.</li>
        </ul>
        <p>Rol actual: {role ?? 'Visitante'}</p>
      </div>
    </main>
  );
};
