// ========== src/services/reportService.js ==========
/**
 * Servicio de Reportes (N°19 - Sprint 4)
 * Solo para ADMIN
 */

import api from './api';

const reportService = {
  /**
   * Obtener dashboard de métricas
   * GET /api/reports/subscription/dashboard
   */
  getDashboard: async () => {
    const response = await api.get('/reports/subscription/dashboard');
    return response.data;
  },

  /**
   * Obtener reporte de crecimiento
   * GET /api/reports/subscription/growth
   */
  getGrowthReport: async (startDate, endDate) => {
    const response = await api.get('/reports/subscription/growth', {
      params: { startDate, endDate },
    });
    return response.data;
  },
};

export default reportService;