// ========== src/components/tasks/KanbanBoard.jsx (CON DEBUG) ==========

import { useState } from 'react';
import { Plus } from 'lucide-react';
import Button from '../common/Button';
import TaskCard from './TaskCard';
import CreateTaskModal from './CreateTaskModal';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import taskService from '../../services/taskService';
import toast from 'react-hot-toast';

const KanbanBoard = ({ projectId, processes, tasks }) => {
  const queryClient = useQueryClient();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedProcessId, setSelectedProcessId] = useState(null);
  const [draggedTask, setDraggedTask] = useState(null);

  const topLevelTasks = tasks.filter(task => !task.parentTaskId);

  const moveTaskMutation = useMutation({
    mutationFn: ({ taskId, targetProcessId }) => {
      // 🔥 VALIDACIÓN Y DEBUG
      console.log('🚀 Iniciando movimiento de tarea:');
      console.log('  - Task ID:', taskId, 'Tipo:', typeof taskId);
      console.log('  - Target Process ID:', targetProcessId, 'Tipo:', typeof targetProcessId);
      console.log('  - Project ID:', projectId, 'Tipo:', typeof projectId);

      // Asegurar que todos sean números
      const numericTaskId = Number(taskId);
      const numericProcessId = Number(targetProcessId);
      const numericProjectId = Number(projectId);

      // Validar que la conversión fue exitosa
      if (isNaN(numericTaskId) || isNaN(numericProcessId) || isNaN(numericProjectId)) {
        throw new Error('IDs inválidos detectados');
      }

      const moveData = {
        targetProcessId: numericProcessId,
        position: 0
      };

      console.log('📦 Payload a enviar:', moveData);
      console.log('🌐 URL:', `/projects/${numericProjectId}/tasks/${numericTaskId}/move`);

      return taskService.moveTask(numericProjectId, numericTaskId, moveData);
    },
    onSuccess: () => {
      console.log('✅ Tarea movida exitosamente');
      queryClient.invalidateQueries(['tasks', projectId]);
      queryClient.invalidateQueries(['project', projectId]);
      queryClient.invalidateQueries(['processes', projectId]);
      toast.success('Tarea movida exitosamente');
    },
    onError: (error) => {
      console.error('❌ Error completo:', error);
      console.error('❌ Response data:', error.response?.data);
      console.error('❌ Response status:', error.response?.status);
      
      const errorMessage = error.response?.data?.title || 
                          error.response?.data?.message || 
                          'Error al mover la tarea';
      
      toast.error(errorMessage);
    }
  });

  const handleDrop = (targetProcessId) => {
    console.log('🎯 handleDrop llamado con:', targetProcessId);
    console.log('📋 Tarea arrastrada:', draggedTask);

    if (!draggedTask) {
      console.warn('⚠️ No hay tarea arrastrada');
      return;
    }

    const numericTargetProcessId = Number(targetProcessId);
    
    if (draggedTask.processId === numericTargetProcessId) {
      console.log('ℹ️ Tarea ya está en este proceso, ignorando movimiento');
      setDraggedTask(null);
      return;
    }

    console.log('🔄 Ejecutando mutación...');
    moveTaskMutation.mutate({
      taskId: draggedTask.id,
      targetProcessId: numericTargetProcessId,
    });
    
    setDraggedTask(null);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDragStart = (task) => {
    console.log('🖱️ Drag start:', task);
    setDraggedTask(task);
  };

  return (
    <div className="flex gap-4 overflow-x-auto pb-4">
      {processes.map((process) => {
        const processTasks = topLevelTasks.filter((task) => task.processId === process.id);

        return (
          <div
            key={process.id}
            className="flex-shrink-0 w-80"
            onDragOver={handleDragOver}
            onDrop={() => handleDrop(process.id)}
          >
            <div className="bg-gray-100 rounded-lg p-4 mb-4">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: process.color }}
                  />
                  <h3 className="font-semibold text-gray-900">{process.name}</h3>
                  <span className="text-sm text-gray-500">({processTasks.length})</span>
                </div>
              </div>
              <Button
                size="sm"
                variant="ghost"
                fullWidth
                icon={Plus}
                onClick={() => {
                  setSelectedProcessId(process.id);
                  setIsCreateModalOpen(true);
                }}
              >
                Nueva Tarea
              </Button>
            </div>

            <div className="space-y-3">
              {processTasks.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  projectId={projectId}
                  onDragStart={() => handleDragStart(task)}
                />
              ))}
            </div>
          </div>
        );
      })}

      <CreateTaskModal
        isOpen={isCreateModalOpen}
        onClose={() => {
          setIsCreateModalOpen(false);
          setSelectedProcessId(null);
        }}
        projectId={projectId}
        processId={selectedProcessId}
      />
    </div>
  );
};

export default KanbanBoard;