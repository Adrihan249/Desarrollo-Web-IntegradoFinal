// ========== src/services/attachmentService.js ==========
/**
 * Servicio de Archivos Adjuntos (N°11 - Sprint 2)
 */

import api from './api';

const attachmentService = {
  /**
   * Obtener archivos de una tarea
   * GET /api/tasks/{taskId}/attachments
   */
  getTaskAttachments: async (taskId) => {
    const response = await api.get(`/tasks/${taskId}/attachments`);
    return response.data;
  },

  /**
   * Subir archivo (multipart/form-data)
   * POST /api/tasks/{taskId}/attachments
   */
  uploadAttachment: async (taskId, file, description = '') => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('description', description);

    const response = await api.post(`/tasks/${taskId}/attachments`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  /**
   * Actualizar descripción de archivo
   * PUT /api/tasks/{taskId}/attachments/{id}
   */
  updateAttachment: async (taskId, attachmentId, description) => {
    const response = await api.put(`/tasks/${taskId}/attachments/${attachmentId}`, null, {
      params: { description },
    });
    return response.data;
  },

  /**
   * Descargar archivo
   * GET /api/attachments/{id}/download
   */
  downloadAttachment: async (attachmentId) => {
    const response = await api.get(`/attachments/${attachmentId}/download`, {
      responseType: 'blob', // Importante para archivos
    });
    return response.data;
  },

  /**
   * Eliminar archivo
   * DELETE /api/tasks/{taskId}/attachments/{id}
   */
  deleteAttachment: async (taskId, attachmentId) => {
    const response = await api.delete(`/tasks/${taskId}/attachments/${attachmentId}`);
    return response.data;
  },
};

export default attachmentService;
