// ========== src/components/tasks/TaskCard.jsx (CORREGIDO) ==========
/**
 * Tarjeta de tarea en el Kanban
 */

import { Calendar, MessageSquare, Paperclip, User } from 'lucide-react';
import Badge from '../common/Badge';
import Avatar from '../common/Avatar';
import { formatRelativeTime } from '../../utils/formatters';

const TaskCard = ({ task, projectId, onDragStart }) => {
  const getPriorityColor = (priority) => {
    const colors = {
      LOW: 'default',
      MEDIUM: 'warning',
      HIGH: 'danger',
      URGENT: 'danger',
    };
    return colors[priority] || 'default';
  };

  // El componente usa task.description y task.dueDate (lo cual es correcto).
  // Asegúrate de que los contadores coincidan con el DTO del Backend (TaskDTO.Response).

  return (
    <div
      draggable
      onDragStart={onDragStart}
      className="bg-white p-4 rounded-lg border border-gray-200 hover:shadow-md transition-shadow cursor-move"
    >
      {/* Title */}
      <h4 className="font-medium text-gray-900 mb-2">{task.title}</h4>

      {/* Description (Visible si task.description existe) */}
      {task.description && (
        <p className="text-sm text-gray-600 mb-3 line-clamp-2">{task.description}</p>
      )}

      {/* Priority */}
      {task.priority && (
        <Badge variant={getPriorityColor(task.priority)} size="sm" className="mb-3">
          {task.priority}
        </Badge>
      )}

      {/* Meta Info */}
      <div className="flex items-center justify-between text-sm text-gray-500">
        <div className="flex items-center gap-3">
          {/* Due Date (Visible si task.dueDate existe) */}
          {task.dueDate && (
            <div className="flex items-center gap-1">
              <Calendar className="w-4 h-4" />
              <span>{formatRelativeTime(task.dueDate)}</span>
            </div>
          )}
          
          {/* Contadores (CORRECCIÓN AQUÍ para usar 'commentCount' y 'attachmentCount' del DTO) */}
          {task.commentCount > 0 && ( // <--- CAMBIO: de commentsCount a commentCount
            <div className="flex items-center gap-1">
              <MessageSquare className="w-4 h-4" />
              <span>{task.commentCount}</span>
            </div>
          )}
          {task.attachmentCount > 0 && ( // <--- CAMBIO: de attachmentsCount a attachmentCount
            <div className="flex items-center gap-1">
              <Paperclip className="w-4 h-4" />
              <span>{task.attachmentCount}</span>
            </div>
          )}
        </div>

        {/* Assignees */}
        {task.assignees && task.assignees.length > 0 && (
          <div className="flex -space-x-2">
            {task.assignees.slice(0, 3).map((assignee) => (
              <Avatar
                key={assignee.id}
                name={`${assignee.firstName} ${assignee.lastName}`}
                size="sm"
                className="border-2 border-white"
              />
            ))}
            {task.assignees.length > 3 && (
              <div className="w-8 h-8 rounded-full bg-gray-200 border-2 border-white flex items-center justify-center text-xs font-medium">
                +{task.assignees.length - 3}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default TaskCard;