
// ========== src/hooks/useAuth.jsx (CORREGIDO) ==========

import { useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';

/**
 * Hook personalizado para acceder al contexto de autenticación
 * @returns {object} Objeto con user, login, logout, isAuthenticated
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
};

export default useAuth;