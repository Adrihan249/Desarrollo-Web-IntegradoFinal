// src/components/tasks/ProcessColumn.jsx
import { useDrop } from 'react-dnd';
import { Plus, MoreVertical } from 'lucide-react';
import TaskCard from './TaskCard';
import Button from '../common/Button';

const ProcessColumn = ({ 
  process, 
  tasks = [], 
  onAddTask, 
  onTaskClick, 
  onTaskMove,
  onEditProcess,
  onDeleteProcess 
}) => {
  const [{ isOver }, drop] = useDrop({
    accept: 'TASK',
    drop: (item) => onTaskMove(item.taskId, process.id),
    collect: (monitor) => ({
      isOver: monitor.isOver()
    })
  });

  const taskCount = tasks.length;
  const isOverLimit = process.taskLimit && taskCount >= process.taskLimit;

  return (
    <div
      ref={drop}
      className={`flex-shrink-0 w-80 bg-gray-50 rounded-lg p-4 transition-colors ${
        isOver ? 'bg-indigo-50 ring-2 ring-indigo-500' : ''
      }`}
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <div
            className="w-3 h-3 rounded-full"
            style={{ backgroundColor: process.color || '#6B7280' }}
          />
          <h3 className="font-semibold text-gray-900">{process.name}</h3>
          <span className={`px-2 py-0.5 text-xs rounded-full ${
            isOverLimit ? 'bg-red-100 text-red-700' : 'bg-gray-200 text-gray-700'
          }`}>
            {taskCount}
            {process.taskLimit && `/${process.taskLimit}`}
          </span>
        </div>

        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onAddTask(process.id)}
          >
            <Plus className="w-4 h-4" />
          </Button>
          <Button variant="ghost" size="sm">
            <MoreVertical className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Description */}
      {process.description && (
        <p className="text-sm text-gray-600 mb-4">{process.description}</p>
      )}

      {/* Task Limit Warning */}
      {isOverLimit && (
        <div className="mb-4 p-2 bg-red-50 border border-red-200 rounded text-xs text-red-700">
          Límite WIP alcanzado ({process.taskLimit} tareas)
        </div>
      )}

      {/* Tasks */}
      <div className="space-y-3 min-h-[200px]">
        {tasks.map(task => (
          <TaskCard
            key={task.id}
            task={task}
            onClick={() => onTaskClick(task)}
          />
        ))}
      </div>

      {/* Add Task Button */}
      <Button
        variant="ghost"
        className="w-full mt-3"
        onClick={() => onAddTask(process.id)}
      >
        <Plus className="w-4 h-4 mr-2" />
        Nueva tarea
      </Button>
    </div>
  );
};

export default ProcessColumn;