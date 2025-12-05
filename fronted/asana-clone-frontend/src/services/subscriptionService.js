// ========== src/services/subscriptionService.js ==========
/**
 * Servicio de Suscripciones (N°12 - Sprint 4)
 */

import api from './api';

const subscriptionService = {
  /**
   * Obtener suscripción actual
   * GET /api/subscriptions/current
   */
  getCurrentSubscription: async () => {
    const response = await api.get('/subscriptions/current');
    return response.data;
  },

  /**
   * Crear suscripción (contratar plan)
   * POST /api/subscriptions
   */
  createSubscription: async (subscriptionData) => {
    const response = await api.post('/subscriptions', subscriptionData);
    return response.data;
  },

  /**
   * Cambiar plan
   * PUT /api/subscriptions/change-plan
   */
  changePlan: async (planData) => {
    const response = await api.put('/subscriptions/change-plan', planData);
    return response.data;
  },

  /**
   * Cancelar suscripción
   * POST /api/subscriptions/cancel
   */
  cancelSubscription: async (cancelData) => {
    const response = await api.post('/subscriptions/cancel', cancelData);
    return response.data;
  },

  /**
   * Reactivar suscripción
   * POST /api/subscriptions/reactivate
   */
  reactivateSubscription: async () => {
    const response = await api.post('/subscriptions/reactivate');
    return response.data;
  },

  /**
   * Obtener resumen de uso
   * GET /api/subscriptions/usage
   */
  getUsage: async () => {
    const response = await api.get('/subscriptions/usage');
    return response.data;
  },
};

export default subscriptionService;