// ===================================
// SERVICIOS API - SPRINT 2
// ===================================

// ========== src/services/processService.js ==========
/**
 * Servicio de Procesos/Columnas Kanban (N°5 - Sprint 2)
 */

import api from './api';

const processService = {
  /**
   * Obtener procesos de un proyecto
   * GET /api/projects/{projectId}/processes
   */
  getProcesses: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/processes`);
    return response.data;
  },

  /**
   * Crear proceso
   * POST /api/projects/{projectId}/processes
   */
  createProcess: async (projectId, processData) => {
    const response = await api.post(`/projects/${projectId}/processes`, processData);
    return response.data;
  },

  /**
   * Actualizar proceso
   * PUT /api/projects/{projectId}/processes/{id}
   */
  updateProcess: async (projectId, processId, processData) => {
    const response = await api.put(`/projects/${projectId}/processes/${processId}`, processData);
    return response.data;
  },

  /**
   * Reordenar proceso
   * POST /api/projects/{projectId}/processes/{id}/reorder
   */
  reorderProcess: async (projectId, processId, newPosition) => {
    const response = await api.post(`/projects/${projectId}/processes/${processId}/reorder`, {
      newPosition,
    });
    return response.data;
  },

  /**
   * Eliminar proceso
   * DELETE /api/projects/{projectId}/processes/{id}
   */
  deleteProcess: async (projectId, processId) => {
    const response = await api.delete(`/projects/${projectId}/processes/${processId}`);
    return response.data;
  },

  /**
   * Crear procesos por defecto
   * POST /api/projects/{projectId}/processes/defaults
   */
  createDefaultProcesses: async (projectId) => {
    const response = await api.post(`/projects/${projectId}/processes/defaults`);
    return response.data;
  },
};

export default processService;