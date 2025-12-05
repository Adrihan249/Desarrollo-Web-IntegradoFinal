// ========== src/services/reminderService.js ==========
/**
 * Servicio de Recordatorios (N°17 - Sprint 4)
 */

import api from './api';

const reminderService = {
  /**
   * Crear recordatorio
   * POST /api/reminders
   */
  createReminder: async (reminderData) => {
    const response = await api.post('/reminders', reminderData);
    return response.data;
  },

  /**
   * Obtener recordatorios del usuario
   * GET /api/reminders
   */
  getReminders: async (status = null) => {
    const response = await api.get('/reminders', {
      params: { status },
    });
    return response.data;
  },

  /**
   * Obtener recordatorios de hoy
   * GET /api/reminders/today
   */
  getTodayReminders: async () => {
    const response = await api.get('/reminders/today');
    return response.data;
  },

  /**
   * Posponer recordatorio (snooze)
   * POST /api/reminders/{id}/snooze
   */
  snoozeReminder: async (reminderId, minutes) => {
    const response = await api.post(`/reminders/${reminderId}/snooze`, { minutes });
    return response.data;
  },

  /**
   * Descartar recordatorio
   * POST /api/reminders/{id}/dismiss
   */
  dismissReminder: async (reminderId) => {
    const response = await api.post(`/reminders/${reminderId}/dismiss`);
    return response.data;
  },

  /**
   * Eliminar recordatorio
   * DELETE /api/reminders/{id}
   */
  deleteReminder: async (reminderId) => {
    const response = await api.delete(`/reminders/${reminderId}`);
    return response.data;
  },

  /**
   * Crear recordatorio para tarea
   * POST /api/tasks/{taskId}/reminder
   */
  createTaskReminder: async (taskId, advanceMinutes = 60) => {
    const response = await api.post(`/tasks/${taskId}/reminder`, null, {
      params: { advanceMinutes },
    });
    return response.data;
  },
};

export default reminderService;