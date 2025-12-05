/**
 * Contexto de Autenticación
 * - Gestiona el estado del usuario autenticado
 * - Provee funciones de login/logout/register
 * - Verifica autenticación en cada renderizado
 */

import React, { createContext, useContext, useState, useEffect } from 'react';
// 🛑 CORRECCIÓN: Se mantiene la carpeta 'services' (plural) y se asume que el archivo del servicio usa la convención de capitalización: 'AuthService'.
import authService from '../services/AuthService'; 
import toast from 'react-hot-toast';

// 🛑 CORRECCIÓN: Solo declaramos el contexto una vez.
const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  // Estado del usuario autenticado
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Verificar si hay usuario en localStorage al cargar la app
  useEffect(() => {
    const checkAuth = async () => {
      const token = authService.getToken();
      const storedUser = authService.getUser();

      if (token && storedUser) {
        setUser(storedUser);
        setIsAuthenticated(true);
        
        // Opcionalmente, verificar el token con el backend
        try {
          const currentUser = await authService.getCurrentUser();
          setUser(currentUser);
        } catch (error) {
          // Si el token es inválido, limpiar todo
          authService.logout();
          setUser(null);
          setIsAuthenticated(false);
        }
      }
      
      setLoading(false);
    };

    checkAuth();
  }, []);

  /**
   * Función de login
   */
  const login = async (credentials) => {
    try {
      const data = await authService.login(credentials);
      setUser(data.user);
      setIsAuthenticated(true);
      toast.success('¡Bienvenido de vuelta!');
      return data;
    } catch (error) {
      toast.error('Credenciales incorrectas');
      throw error;
    }
  };

  /**
   * Función de registro
   */
  const register = async (userData) => {
    try {
      const data = await authService.register(userData);
      setUser(data.user);
      setIsAuthenticated(true);
      toast.success('¡Cuenta creada exitosamente!');
      return data;
    } catch (error) {
      toast.error('Error al crear la cuenta');
      throw error;
    }
  };

  /**
   * Función de logout
   */
  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
    toast.success('Sesión cerrada');
  };

  /**
   * Verificar si el usuario tiene un rol específico
   */
  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    return user.roles.includes(role);
  };

  /**
   * Verificar si el usuario es admin
   */
  const isAdmin = () => hasRole('ROLE_ADMIN');

  // Objeto de valor del contexto que contiene el estado y las funciones
  const value = {
    user,
    setUser, // Incluimos setUser para permitir actualizaciones de perfil fuera del Provider
    loading,
    isAuthenticated,
    login,
    register,
    logout,
    hasRole,
    isAdmin,
  };

  // 🛑 CORRECCIÓN: Pasamos el objeto 'value' directamente, sin anidarlo.
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Hook personalizado para usar el contexto de autenticación
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de un AuthProvider');
  }
  return context;
};