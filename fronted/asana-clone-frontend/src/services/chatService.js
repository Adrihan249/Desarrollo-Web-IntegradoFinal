// ========== src/services/chatService.js ==========
/**
 * Servicio de Chat del Proyecto (N°14 - Sprint 3)
 */

import api from './api';

const chatService = {
  /**
   * Enviar mensaje
   * POST /api/projects/{projectId}/chat/messages
   */
  sendMessage: async (projectId, messageData) => {
    const response = await api.post(`/projects/${projectId}/chat/messages`, messageData);
    return response.data;
  },

  /**
   * Obtener mensajes
   * GET /api/projects/{projectId}/chat/messages
   */
  getMessages: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/chat/messages`);
    return response.data;
  },

  /**
   * Obtener mensajes recientes
   * GET /api/projects/{projectId}/chat/messages/recent
   */
  getRecentMessages: async (projectId, limit = 50) => {
    const response = await api.get(`/projects/${projectId}/chat/messages/recent`, {
      params: { limit },
    });
    return response.data;
  },

  /**
   * Obtener respuestas de un mensaje (hilo)
   * GET /api/projects/{projectId}/chat/messages/{id}/replies
   */
  getMessageReplies: async (projectId, messageId) => {
    const response = await api.get(`/projects/${projectId}/chat/messages/${messageId}/replies`);
    return response.data;
  },

  /**
   * Agregar reacción
   * POST /api/projects/{projectId}/chat/messages/{id}/reactions
   */
  addReaction: async (projectId, messageId, emoji) => {
    const response = await api.post(`/projects/${projectId}/chat/messages/${messageId}/reactions`, {
      emoji,
    });
    return response.data;
  },

  /**
   * Eliminar reacción
   * DELETE /api/projects/{projectId}/chat/messages/{id}/reactions/{emoji}
   */
  removeReaction: async (projectId, messageId, emoji) => {
    const response = await api.delete(`/projects/${projectId}/chat/messages/${messageId}/reactions/${emoji}`);
    return response.data;
  },

  /**
   * Fijar mensaje
   * PUT /api/projects/{projectId}/chat/messages/{id}/pin
   */
  pinMessage: async (projectId, messageId) => {
    const response = await api.put(`/projects/${projectId}/chat/messages/${messageId}/pin`);
    return response.data;
  },

  /**
   * Desfijar mensaje
   * DELETE /api/projects/{projectId}/chat/messages/{id}/pin
   */
  unpinMessage: async (projectId, messageId) => {
    const response = await api.delete(`/projects/${projectId}/chat/messages/${messageId}/pin`);
    return response.data;
  },

  /**
   * Obtener mensajes fijados
   * GET /api/projects/{projectId}/chat/messages/pinned
   */
  getPinnedMessages: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/chat/messages/pinned`);
    return response.data;
  },

  /**
   * Buscar en el chat
   * GET /api/projects/{projectId}/chat/messages/search
   */
  searchMessages: async (projectId, query) => {
    const response = await api.get(`/projects/${projectId}/chat/messages/search`, {
      params: { query },
    });
    return response.data;
  },
};

export default chatService;