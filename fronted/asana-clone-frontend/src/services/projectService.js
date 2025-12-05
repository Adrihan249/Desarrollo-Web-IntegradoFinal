// ========== src/services/projectService.js (CORREGIDO) ==========

import api from './api';

const projectService = {
  
  getAllProjects: async () => {
    const response = await api.get('/projects');
    return response.data;
  },

    // 🔥 Función agregada para que el frontend no falle:
    getMyProjects: async () => {
      // Reutiliza la función que ya existe
      return projectService.getAllProjects();
    },

  /**
   * Obtener proyecto por ID
   */
  getProjectById: async (projectId) => {
    const response = await api.get(`/projects/${projectId}`);
    return response.data;
  },

  /**
   * Crear nuevo proyecto
   */
  createProject: async (projectData) => {
    const response = await api.post('/projects', projectData);
    return response.data;
  },

  /**
   * Actualizar proyecto
   */
  updateProject: async (projectId, projectData) => {
    const response = await api.put(`/projects/${projectId}`, projectData);
    return response.data;
  },

  /**
   * Archivar proyecto
   */
  archiveProject: async (projectId) => {
    const response = await api.put(`/projects/${projectId}/archive`);
    return response.data;
  },

  /**
   * 🔥 Desarchivar proyecto
   */
  unarchiveProject: async (projectId) => {
    const response = await api.put(`/projects/${projectId}/unarchive`);
    return response.data;
  },

  /**
   * Eliminar proyecto
   */
  deleteProject: async (projectId) => {
    const response = await api.delete(`/projects/${projectId}`);
    return response.data;
  },

  /**
   * Agregar miembro al proyecto (método antiguo por ID)
   */
  addMember: async (projectId, userId) => {
    const response = await api.post(`/projects/${projectId}/members/${userId}`);
    return response.data;
  },

  /**
   * Remover miembro del proyecto
   */
  removeMember: async (projectId, userId) => {
    const response = await api.delete(`/projects/${projectId}/members/${userId}`);
    return response.data;
  },

  /**
   * 🔥 INVITAR MIEMBRO POR EMAIL (MÉTODO CORRECTO)
   * POST /api/projects/{projectId}/invite
   * @param {number} projectId
   * @param {object} data - { invitedEmail: string }
   */
  inviteMember: async (projectId, data) => {
    const response = await api.post(`/projects/${projectId}/invite`, data);
    return response.data;
  },

  /**
   * Buscar proyectos
   */
  searchProjects: async (keyword) => {
    const response = await api.get('/projects/search', {
      params: { keyword },
    });
    return response.data;
  },

  /**
   * Filtrar por estado
   */
  getProjectsByStatus: async (status) => {
    const response = await api.get('/projects/by-status', {
      params: { status },
    });
    return response.data;
  },

  /**
   * Proyectos con deadline próximo
   */
  getUpcomingDeadlines: async (days = 7) => {
    const response = await api.get('/projects/upcoming-deadlines', {
      params: { days },
    });
    return response.data;
  },
};

export default projectService;