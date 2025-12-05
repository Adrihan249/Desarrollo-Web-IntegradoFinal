
// ===================================
// SERVICIOS API - SPRINT 1
// ===================================

// ========== src/services/authService.js ==========
/**
 * Servicio de Autenticación
 * - Login
 * - Register
 * - Logout
 * - Obtener usuario actual
 */

// ========== src/services/authService.js ==========

import api from './api';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';



const authService = {
  
  /**
   * Login - Autentica al usuario y guarda el token
   */
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    
    if (response.data.token) {
      // 🔥 CRÍTICO: Guardar token en localStorage
      localStorage.setItem(TOKEN_KEY, response.data.token);
      localStorage.setItem(USER_KEY, JSON.stringify(response.data.user));
    }
    
    return response.data;
  },

  /**
   * Register - Registra un nuevo usuario
   */
  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    
    if (response.data.token) {
      localStorage.setItem(TOKEN_KEY, response.data.token);
      localStorage.setItem(USER_KEY, JSON.stringify(response.data.user));
    }
    
    return response.data;
  },

  /**
   * Logout - Cierra sesión y limpia el localStorage
   */
  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },

  /**
   * Obtiene el token actual del localStorage
   */
  getToken: () => {
    return localStorage.getItem(TOKEN_KEY);
  },

  /**
   * Obtiene el usuario actual del localStorage
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Verifica si el usuario está autenticado
   */
  isAuthenticated: () => {
    return !!localStorage.getItem(TOKEN_KEY);
  },

  /**
   * Obtiene el usuario actual del backend (refresca los datos)
   */
  getCurrentUserFromAPI: async () => {
    const response = await api.get('/auth/me');
    
    if (response.data) {
      localStorage.setItem(USER_KEY, JSON.stringify(response.data));
    }
    
    return response.data;
  },
  /**
   * Iniciar sesión
   * POST /api/auth/login
   */
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    const { token, user } = response.data;
    
    // Guardar token y usuario en localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    
    return response.data;
  },

  /**
   * Registrar nuevo usuario
   * POST /api/auth/register
   */
  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    const { token, user } = response.data;
    
    // Guardar token y usuario en localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    
    return response.data;
  },

  /**
   * Cerrar sesión
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  },

  /**
   * Obtener usuario actual
   * GET /api/auth/me
   */
  getCurrentUser: async () => {
    const response = await api.get('/auth/me');
    localStorage.setItem('user', JSON.stringify(response.data));
    return response.data;
  },

  /**
   * Verificar si el usuario está autenticado
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  /**
   * Obtener token del localStorage
   */
  getToken: () => {
    return localStorage.getItem('token');
  },

  /**
   * Obtener usuario del localStorage
   */
  getUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },
};

export default authService;