// ========== src/services/exportService.js ==========

import api from './api';

const exportService = {
  /**
   * Solicitar exportación de proyecto
   * POST /api/exports/project/{projectId}
   */
  requestProjectExport: async (projectId, exportData) => {
    const response = await api.post(`/exports/project/${projectId}`, exportData);
    return response.data;
  },
  
  /**
   * 🔥 Método principal usado por ExportsPage
   * Wrapper que llama a requestProjectExport
   */
  requestExport: async (request) => {
    const projectId = request.referenceId;
    const exportData = {
      type: request.type,
      format: request.format,
    };

    return exportService.requestProjectExport(projectId, exportData);
  },

  /**
   * Exportar datos de usuario (GDPR)
   * POST /api/exports/user-data
   */
  exportUserData: async (format = 'JSON') => {
    const response = await api.post('/exports/user-data', null, {
      params: { format },
    });
    return response.data;
  },

  /**
   * 🔥 CORREGIDO: Descomentar este método
   * Obtener mis exportaciones
   * GET /api/exports/my-exports
   */
  getMyExports: async () => {
    const response = await api.get('/exports/my-exports');
    return response.data;
  },

  /**
   * Obtener estado del export
   * GET /api/exports/{jobId}
   */
  getExportStatus: async (jobId) => {
    const response = await api.get(`/exports/${jobId}`);
    return response.data;
  },

  /**
   * Descargar export
   * GET /api/exports/{jobId}/download
   */
  downloadExport: async (jobId) => {
    const response = await api.get(`/exports/${jobId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  /**
   * Cancelar export
   * DELETE /api/exports/{jobId}
   */
  cancelExport: async (jobId) => {
    const response = await api.delete(`/exports/${jobId}`);
    return response.data;
  },

  /**
   * Exportación rápida de tareas
   * GET /api/projects/{projectId}/tasks/export
   */
  exportTasks: async (projectId, format = 'CSV') => {
    const response = await api.get(`/projects/${projectId}/tasks/export`, {
      params: { format },
    });
    return response.data;
  },
};

export default exportService;