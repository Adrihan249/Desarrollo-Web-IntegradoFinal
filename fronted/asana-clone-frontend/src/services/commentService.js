// ========== src/services/commentService.js ==========
import api from './api';

const commentService = {
  /**
   * Obtener comentarios de una tarea
   * GET /api/tasks/{taskId}/comments
   */
  getTaskComments: async (taskId) => {
    const response = await api.get(`/tasks/${taskId}/comments`);
    return response.data;
  },

  /**
   * Crear comentario
   * POST /api/tasks/{taskId}/comments
   */
  createComment: async (taskId, content) => {
    const response = await api.post(`/tasks/${taskId}/comments`, { content });
    return response.data;
  },

  /**
   * Eliminar comentario
   * DELETE /api/tasks/{taskId}/comments/{commentId}
   */
  deleteComment: async (taskId, commentId) => {
    const response = await api.delete(`/tasks/${taskId}/comments/${commentId}`);
    return response.data;
  },
};

export default commentService;