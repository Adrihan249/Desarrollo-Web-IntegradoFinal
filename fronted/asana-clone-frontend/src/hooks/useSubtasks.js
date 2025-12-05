import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import taskService from '../services/taskService';
import toast from 'react-hot-toast';

// Obtener subtareas de una tarea
export const useSubtasks = (projectId, taskId) => {
  const queryClient = useQueryClient();

  // GET subtareas
  const {
    data: subtasks,
    isLoading: subtasksLoading,
    error: subtasksError,
  } = useQuery({
    queryKey: ['subtasks', projectId, taskId],
    queryFn: () => taskService.getSubtasks(projectId, taskId),
    enabled: !!projectId && !!taskId,
  });

  // CREAR subtarea
  const createSubtaskMutation = useMutation({
    mutationFn: (subtaskData) =>
      taskService.createTask(projectId, {
        ...subtaskData,
        parentTaskId: taskId, // muy importante
      }),
    onSuccess: () => {
      queryClient.invalidateQueries(['subtasks', projectId, taskId]);
      toast.success('Subtarea creada');
    },
  });

  // ACTUALIZAR subtarea
  const updateSubtaskMutation = useMutation({
    mutationFn: ({ subtaskId, data }) =>
      taskService.updateTask(projectId, subtaskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['subtasks', projectId, taskId]);
      toast.success('Subtarea actualizada');
    },
  });

  return {
    subtasks: subtasks || [],
    subtasksLoading,
    subtasksError,
    createSubtask: createSubtaskMutation.mutate,
    updateSubtask: updateSubtaskMutation.mutate,
  };
};
