// ========== src/services/activityService.js ==========
/**
 * Servicio de Timeline de Actividades (N°7 - Sprint 3)
 */

import api from './api';

const activityService = {
  /**
   * Obtener actividades de un proyecto
   * GET /api/projects/{projectId}/activity
   */
  getProjectActivity: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/activity`);
    return response.data;
  },

  /**
   * Obtener timeline agrupado por fecha
   * GET /api/projects/{projectId}/activity/timeline
   */
  getTimeline: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/activity/timeline`);
    return response.data;
  },

  /**
   * Obtener estadísticas de actividad
   * GET /api/projects/{projectId}/activity/stats
   */
  getActivityStats: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/activity/stats`);
    return response.data;
  },

  /**
   * Obtener actividades de un usuario
   * GET /api/projects/{projectId}/activity/user/{userId}
   */
  getUserActivity: async (projectId, userId) => {
    const response = await api.get(`/projects/${projectId}/activity/user/${userId}`);
    return response.data;
  },
};

export default activityService;