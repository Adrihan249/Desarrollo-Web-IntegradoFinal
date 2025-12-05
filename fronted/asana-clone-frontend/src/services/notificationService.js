import api from './api';

const notificationService = {
  /**
   * Obtener todas las notificaciones del usuario
   */
  getNotifications: async () => {
    const response = await api.get('/notifications');
    return response.data;
  },

  /**
   * Obtener notificaciones no leídas
   */
  getUnreadNotifications: async () => {
    const response = await api.get('/notifications?unread=true');
    return response.data;
  },

  /**
   * Contar notificaciones no leídas
   */
  getUnreadCount: async () => {
    const response = await api.get('/notifications/unread/count');
    return response.data;
  },

  /**
   * Marcar una notificación como leída
   */
  markAsRead: async (notificationId) => {
    const response = await api.put(`/notifications/${notificationId}/read`);
    return response.data;
  },

  /**
   * Marcar todas las notificaciones como leídas
   */
  markAllAsRead: async () => {
    const response = await api.put('/notifications/mark-all-read');
    return response.data;
  },

  /**
   * Eliminar una notificación
   */
  deleteNotification: async (notificationId) => {
    const response = await api.delete(`/notifications/${notificationId}`);
    return response.data;
  },

  /**
   * Archivar una notificación
   */
  archiveNotification: async (notificationId) => {
    const response = await api.put(`/notifications/${notificationId}/archive`);
    return response.data;
  },
};

export default notificationService;