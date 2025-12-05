// ========== src/services/taskService.js ==========
/**
 * Servicio de Tareas (N°6, N°18 - Sprint 2)
 */

import api from './api';

const taskService = {
  /**
   * Obtener tareas de un proyecto
   * GET /api/projects/{projectId}/tasks
   */
  getProjectTasks: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/tasks`);
    return response.data;
  },

  /**
   * Obtener tareas de un proceso (columna Kanban)
   * GET /api/processes/{processId}/tasks
   */
  getProcessTasks: async (processId) => {
    const response = await api.get(`/processes/${processId}/tasks`);
    return response.data;
  },

  /**
   * Obtener tarea por ID
   * GET /api/projects/{projectId}/tasks/{id}
   */
  getTaskById: async (projectId, taskId) => {
    const response = await api.get(`/projects/${projectId}/tasks/${taskId}`);
    return response.data;
  },

  /**
   * Crear tarea
   * POST /api/projects/{projectId}/tasks
   */
  createTask: async (projectId, taskData) => {
    const response = await api.post(`/projects/${projectId}/tasks`, taskData);
    return response.data;
  },

  /**
   * Actualizar tarea
   * PUT /api/projects/{projectId}/tasks/{id}
   */
  updateTask: async (projectId, taskId, taskData) => {
    const response = await api.put(`/projects/${projectId}/tasks/${taskId}`, taskData);
    return response.data;
  },

  /**
   * Mover tarea a otro proceso (drag & drop)
   * POST /api/projects/{projectId}/tasks/{id}/move
   */
  moveTask: async (projectId, taskId, moveData) => {
    const response = await api.post(`/projects/${projectId}/tasks/${taskId}/move`, moveData);
    return response.data;
  },

  /**
   * Asignar usuario a tarea
   * POST /api/projects/{projectId}/tasks/{taskId}/assignees/{userId}
   */
  assignUser: async (projectId, taskId, userId) => {
    const response = await api.post(`/projects/${projectId}/tasks/${taskId}/assignees/${userId}`);
    return response.data;
  },

  /**
   * Desasignar usuario de tarea
   * DELETE /api/projects/{projectId}/tasks/{taskId}/assignees/{userId}
   */
  unassignUser: async (projectId, taskId, userId) => {
    const response = await api.delete(`/projects/${projectId}/tasks/${taskId}/assignees/${userId}`);
    return response.data;
  },

  /**
   * Obtener subtareas (N°18)
   * GET /api/projects/{projectId}/tasks/{id}/subtasks
   */
  getSubtasks: async (projectId, taskId) => {
    const response = await api.get(`/projects/${projectId}/tasks/${taskId}/subtasks`);
    return response.data;
  },

  /**
   * Buscar tareas
   * GET /api/projects/{projectId}/tasks/search?keyword={keyword}
   */
  searchTasks: async (projectId, keyword) => {
    const response = await api.get(`/projects/${projectId}/tasks/search`, {
      params: { keyword },
    });
    return response.data;
  },

  /**
   * Tareas próximas a vencer
   * GET /api/projects/{projectId}/tasks/upcoming?days={days}
   */
  getUpcomingTasks: async (projectId, days = 7) => {
    const response = await api.get(`/projects/${projectId}/tasks/upcoming`, {
      params: { days },
    });
    return response.data;
  },

  /**
   * Tareas vencidas
   * GET /api/projects/{projectId}/tasks/overdue
   */
  getOverdueTasks: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/tasks/overdue`);
    return response.data;
  },

  /**
   * Eliminar tarea
   * DELETE /api/projects/{projectId}/tasks/{id}
   */
  deleteTask: async (projectId, taskId) => {
    const response = await api.delete(`/projects/${projectId}/tasks/${taskId}`);
    return response.data;
  },
     getMyTasks: async () => {
  const response = await api.get('/tasks/my-tasks');
  return response.data;
},

};

export default taskService;