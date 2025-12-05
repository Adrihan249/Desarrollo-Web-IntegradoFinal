// src/components/tasks/SubtaskList.jsx
import { useState } from 'react';
import { Plus, CheckCircle2, Circle, Trash2 } from 'lucide-react';
import Button from '../common/Button';
import Input from '../common/Input';
import ProgressBar from '../common/ProgressBar';
import { formatDate } from '../../utils/formatters';

const SubtaskList = ({ taskId, subtasks = [], onAdd, onToggle, onDelete }) => {
  const [isAdding, setIsAdding] = useState(false);
  const [newSubtaskTitle, setNewSubtaskTitle] = useState('');

  const handleAdd = async () => {
    if (newSubtaskTitle.trim()) {
      await onAdd({ title: newSubtaskTitle, parentTaskId: taskId });
      setNewSubtaskTitle('');
      setIsAdding(false);
    }
  };

  const completedCount = subtasks.filter(st => st.status === 'DONE').length;
  const progress = subtasks.length > 0 ? (completedCount / subtasks.length) * 100 : 0;

  return (
    <div className="space-y-4">
      {/* Progress */}
      {subtasks.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-2">
            <span className="text-sm font-medium text-gray-700">
              Progreso de subtareas
            </span>
            <span className="text-sm text-gray-500">
              {completedCount} de {subtasks.length} completadas
            </span>
          </div>
          <ProgressBar value={progress} />
        </div>
      )}

      {/* Subtasks List */}
      <div className="space-y-2">
        {subtasks.map(subtask => (
          <div
            key={subtask.id}
            className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors group"
          >
            <button
              onClick={() => onToggle(subtask.id)}
              className="mt-0.5 flex-shrink-0"
            >
              {subtask.status === 'DONE' ? (
                <CheckCircle2 className="w-5 h-5 text-green-600" />
              ) : (
                <Circle className="w-5 h-5 text-gray-400 hover:text-gray-600" />
              )}
            </button>

            <div className="flex-1 min-w-0">
              <p className={`text-sm font-medium ${
                subtask.status === 'DONE' ? 'line-through text-gray-500' : 'text-gray-900'
              }`}>
                {subtask.title}
              </p>
              {subtask.assignees?.length > 0 && (
                <div className="flex items-center gap-1 mt-1">
                  {subtask.assignees.map(user => (
                    <span key={user.id} className="text-xs text-gray-500">
                      {user.firstName}
                    </span>
                  ))}
                </div>
              )}
              {subtask.dueDate && (
                <p className="text-xs text-gray-500 mt-1">
                  Vence: {formatDate(subtask.dueDate)}
                </p>
              )}
            </div>

            <button
              onClick={() => onDelete(subtask.id)}
              className="flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <Trash2 className="w-4 h-4 text-red-600 hover:text-red-700" />
            </button>
          </div>
        ))}
      </div>

      {/* Add Subtask */}
      {isAdding ? (
        <div className="flex gap-2">
          <Input
            value={newSubtaskTitle}
            onChange={(e) => setNewSubtaskTitle(e.target.value)}
            placeholder="Título de la subtarea"
            onKeyPress={(e) => e.key === 'Enter' && handleAdd()}
            autoFocus
          />
          <Button onClick={handleAdd} variant="primary" size="sm">
            Agregar
          </Button>
          <Button onClick={() => setIsAdding(false)} variant="secondary" size="sm">
            Cancelar
          </Button>
        </div>
      ) : (
        <Button
          onClick={() => setIsAdding(true)}
          variant="ghost"
          size="sm"
          className="w-full"
        >
          <Plus className="w-4 h-4 mr-2" />
          Agregar subtarea
        </Button>
      )}
    </div>
  );
};

export default SubtaskList;