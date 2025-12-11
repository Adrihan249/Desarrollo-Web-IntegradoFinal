// ========== src/services/userService.js (CORREGIDO) ==========
/**
 * Servicio de Usuarios (N°9 - Sprint 1)
 * - Listar usuarios
 * - Obtener usuario por ID
 * - Actualizar perfil
 * - Buscar usuarios
 * - Cambiar contraseña
 * - Gestión de usuarios (Admin)
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
   * ✅ CORREGIDO: Template literal
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
   * ✅ CORREGIDO: Template literal
   */
  updateUser: async (userId, userData) => {
    const response = await api.put(`/users/${userId}`, userData);
    return response.data;
  },

  /**
   * Cambiar contraseña
   * POST /api/users/{id}/change-password
   * ✅ CORREGIDO: Template literal
   */
  changePassword: async (userId, passwords) => {
    const response = await api.post(`/users/${userId}/change-password`, passwords);
    return response.data;
  },

  /**
   * Asignar roles (ADMIN)
   * PUT /api/users/{id}/roles
   * ✅ CORREGIDO: Template literal
   */
  assignRoles: async (userId, roles) => {
    const response = await api.put(`/users/${userId}/roles`, roles);
    return response.data;
  },

  /**
   * 🔥 NUEVO: Desactivar usuario (ADMIN)
   * DELETE /api/users/{id}
   */
  deleteUser: async (userId) => {
    const response = await api.delete(`/users/${userId}`);
    return response.data;
  },

  /**
   * 🔥 NUEVO: Activar usuario (ADMIN)
   * POST /api/users/{id}/activate
   */
  activateUser: async (userId) => {
    const response = await api.post(`/users/${userId}/activate`);
    return response.data;
  },
};

export default userService;
