// ===================================
// SERVICIOS API - Configuración Base
// ===================================

// ========== src/services/api.js ==========
/**
 * Configuración base de Axios
 * - Interceptores para agregar token JWT
 * - Manejo de errores global
 * - Base URL del backend
 */

import axios from 'axios';
import toast from 'react-hot-toast';

// Crear instancia de axios con configuración base
const api = axios.create({
  baseURL: "/api",
  headers: {
    'Content-Type': 'application/json',
  },
});
// 🔥 INTERCEPTOR DE REQUEST: Añade el token a todas las peticiones
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('🔐 Token añadido al header:', token.substring(0, 20) + '...');
    } else {
      console.warn('⚠️ No hay token en localStorage');
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 🔥 INTERCEPTOR DE RESPONSE: Maneja errores de autenticación
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      console.error('❌ Error 401: No autenticado - Redirigiendo al login');
      
      // Limpiar localStorage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // Redirigir al login
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);
// ===== INTERCEPTOR DE REQUEST =====
// Agrega el token JWT a cada petición automáticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// ===== INTERCEPTOR DE RESPONSE =====
// Maneja errores globalmente
api.interceptors.response.use(
  (response) => {
    // Si la respuesta es exitosa, simplemente retornarla
    return response;
  },
  (error) => {
    // Manejo de errores HTTP
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          // No autorizado - Token inválido o expirado
          toast.error('Sesión expirada. Por favor inicia sesión nuevamente.');
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
          break;

        case 403:
          // Prohibido - Sin permisos
          toast.error('No tienes permisos para realizar esta acción.');
          break;

        case 404:
          // No encontrado
          toast.error(data.message || 'Recurso no encontrado.');
          break;

        case 400:
          // Bad Request - Errores de validación
          if (data.errors) {
            // Si hay múltiples errores (validación de formulario)
            Object.values(data.errors).forEach((errorMsg) => {
              toast.error(errorMsg);
            });
          } else {
            toast.error(data.message || 'Solicitud inválida.');
          }
          break;

        case 500:
          // Error del servidor
          toast.error('Error del servidor. Intenta nuevamente más tarde.');
          break;

        default:
          toast.error(data.message || 'Ocurrió un error inesperado.');
      }
    } else if (error.request) {
      // La petición se hizo pero no hubo respuesta
      toast.error('No se pudo conectar con el servidor.');
    } else {
      // Error al configurar la petición
      toast.error('Error al procesar la solicitud.');
    }

    return Promise.reject(error);
  }
);

export default api;
