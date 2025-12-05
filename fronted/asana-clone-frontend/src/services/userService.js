// ========== src/services/userService.js ==========
/**
 * Servicio de Usuarios (N°9 - Sprint 1)
 * - Listar usuarios
 * - Obtener usuario por ID
 * - Actualizar perfil
 * - Buscar usuarios
 * - Cambiar contraseña
 */

import api from './api';

const userService = {
  /**
   * Obtener todos los usuarios
   * GET /api/users
   */
  getAllUsers: async () => {
    const response = await api.get('/users');
    return response.data;
  },

  /**
   * Obtener usuario por ID
   * GET /api/users/{id}
   */
  getUserById: async (userId) => {
    const response = await api.get(`/users/${userId}`);
    return response.data;
  },

  /**
   * Buscar usuarios por palabra clave
   * GET /api/users/search?keyword={keyword}
   */
  searchUsers: async (keyword) => {
    const response = await api.get('/users/search', {
      params: { keyword },
    });
    return response.data;
  },

  /**
   * Actualizar perfil de usuario
   * PUT /api/users/{id}
   */
  updateUser: async (userId, userData) => {
    const response = await api.put(`/users/${userId}`, userData);
    return response.data;
  },

  /**
   * Cambiar contraseña
   * POST /api/users/{id}/change-password
   */
  changePassword: async (userId, passwords) => {
    const response = await api.post(`/users/${userId}/change-password`, passwords);
    return response.data;
  },

  /**
   * Asignar roles (ADMIN)
   * PUT /api/users/{id}/roles
   */
  assignRoles: async (userId, roles) => {
    const response = await api.put(`/users/${userId}/roles`, roles);
    return response.data;
  },
};

export default userService;
