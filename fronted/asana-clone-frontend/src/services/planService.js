// ===================================
// SERVICIOS API - SPRINT 4
// ===================================

// ========== src/services/planService.js ==========
/**
 * Servicio de Planes de Suscripción (N°12 - Sprint 4)
 */

import api from './api';

const planService = {
  /**
   * Obtener todos los planes
   * GET /api/plans
   */
  getAllPlans: async () => {
    const response = await api.get('/plans');
    return response.data;
  },

  /**
   * Obtener plan por ID
   * GET /api/plans/{id}
   */
  getPlanById: async (planId) => {
    const response = await api.get(`/plans/${planId}`);
    return response.data;
  },

  /**
   * Obtener plan más popular
   * GET /api/plans/popular
   */
  getMostPopularPlan: async () => {
    const response = await api.get('/plans/popular');
    return response.data;
  },
};

export default planService;