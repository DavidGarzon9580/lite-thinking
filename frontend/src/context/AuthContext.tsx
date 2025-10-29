import { createContext, useContext, useEffect, useMemo, useState } from 'react';

type UserRole = 'ADMIN' | 'VIEWER';

type AuthState = {
  token: string | null;
  email: string | null;
  role: UserRole | null;
};

type AuthContextValue = AuthState & {
  login: (token: string) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const decodeToken = (token: string): { email: string; role: UserRole } | null => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      email: payload.sub,
      role: payload.role as UserRole
    };
  } catch (error) {
    console.error('Error decodificando token', error);
    return null;
  }
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>(() => {
    const savedToken = localStorage.getItem('lt:token');
    if (savedToken) {
      const decoded = decodeToken(savedToken);
      if (decoded) {
        return {
          token: savedToken,
          email: decoded.email,
          role: decoded.role
        };
      }
      localStorage.removeItem('lt:token');
    }
    return { token: null, email: null, role: null };
  });

  useEffect(() => {
    if (state.token) {
      localStorage.setItem('lt:token', state.token);
    } else {
      localStorage.removeItem('lt:token');
    }
  }, [state.token]);

  const value = useMemo<AuthContextValue>(() => ({
    ...state,
    login: (token: string) => {
      const decoded = decodeToken(token);
      if (!decoded) {
        throw new Error('Token inválido');
      }
      setState({
        token,
        email: decoded.email,
        role: decoded.role
      });
    },
    logout: () => setState({ token: null, email: null, role: null })
  }), [state]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextValue => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de un AuthProvider');
  }
  return context;
};
