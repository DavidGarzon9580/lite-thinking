import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { UserRole } from '../types';

type Props = {
  roles?: UserRole[];
  children: React.ReactElement;
};

export const ProtectedRoute: React.FC<Props> = ({ roles, children }) => {
  const { token, role } = useAuth();

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (roles && role && !roles.includes(role)) {
    return <Navigate to="/" replace />;
  }

  return children;
};
