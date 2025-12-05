// ========== src/hooks/useTasks.js ==========
/**
 * Hook para gestionar tareas
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import taskService from '../services/taskService';
import toast from 'react-hot-toast';

export const useTasks = (projectId) => {
  const queryClient = useQueryClient();

  // Obtener tareas del proyecto
  const {
    data: tasks,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['tasks', projectId],
    queryFn: () => taskService.getProjectTasks(projectId),
    enabled: !!projectId,
  });

  // Crear tarea
  const createTaskMutation = useMutation({
    mutationFn: (taskData) => taskService.createTask(projectId, taskData),
    onSuccess: () => {
      queryClient.invalidateQueries(['tasks', projectId]);
      toast.success('Tarea creada');
    },
  });

  // Actualizar tarea
  const updateTaskMutation = useMutation({
    mutationFn: ({ taskId, data }) =>
      taskService.updateTask(projectId, taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['tasks', projectId]);
      toast.success('Tarea actualizada');
    },
  });

  // Mover tarea (drag & drop)
  const moveTaskMutation = useMutation({
    mutationFn: ({ taskId, moveData }) =>
      taskService.moveTask(projectId, taskId, moveData),
    onSuccess: () => {
      queryClient.invalidateQueries(['tasks', projectId]);
    },
  });


  return {
    tasks: tasks || [],
    isLoading,
    error,
    createTask: createTaskMutation.mutate,
    updateTask: updateTaskMutation.mutate,
    moveTask: moveTaskMutation.mutate,
    useSubtasks,
  };
};