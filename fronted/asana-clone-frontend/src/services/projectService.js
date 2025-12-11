// ========== src/services/projectService.js (CORREGIDO) ==========
import api from './api';

const projectService = {
  
  getAllProjects: async () => {
    const response = await api.get('/projects');
    return response.data;
  },

  // Función agregada para compatibilidad
  getMyProjects: async () => {
    return projectService.getAllProjects();
  },

  /**
   * Obtener proyecto por ID
   * ✅ CORREGIDO: Template literal con sintaxis correcta
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
   * ✅ CORREGIDO: Template literal con sintaxis correcta
   */
  updateProject: async (projectId, projectData) => {
    const response = await api.put(`/projects/${projectId}`, projectData);
    return response.data;
  },

  /**
   * Archivar proyecto
   * ✅ CORREGIDO: Template literal con sintaxis correcta
   */
  archiveProject: async (projectId) => {
    const response = await api.put(`/projects/${projectId}/archive`);
    return response.data;
  },

  /**
   * Desarchivar proyecto
   * ✅ CORREGIDO: Template literal con sintaxis correcta
   */
  unarchiveProject: async (projectId) => {
    const response = await api.put(`/projects/${projectId}/unarchive`);
    return response.data;
  },

  /**
   * Eliminar proyecto
   * ✅ CORREGIDO: Template literal con sintaxis correcta
   */
  deleteProject: async (projectId) => {
    const response = await api.delete(`/projects/${projectId}`);
    return response.data;
  },

  /**
   * Agregar miembro al proyecto (método antiguo por ID)
   * ✅ CORREGIDO: Template literal con sintaxis correcta
   */
  addMember: async (projectId, userId) => {
    const response = await api.post(`/projects/${projectId}/members/${userId}`);
    return response.data;
  },

  /**
   * Remover miembro del proyecto
   * ✅ CORREGIDO: Template literal con sintaxis correcta
   */
  removeMember: async (projectId, userId) => {
    const response = await api.delete(`/projects/${projectId}/members/${userId}`);
    return response.data;
  },

  /**
   * INVITAR MIEMBRO POR EMAIL
   * POST /api/projects/{projectId}/invite
   * ✅ CORREGIDO: Template literal con sintaxis correcta
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

  /**
   * 🔥 NUEVO: Fetch para recalcular estado
   */
  fetchProjectById: async (projectId) => {
    const response = await api.get(`/projects/${projectId}`);
    return response.data;
  },
};

export default projectService;
