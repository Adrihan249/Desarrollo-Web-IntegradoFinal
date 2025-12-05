// src/components/tasks/TaskDetail.jsx
import { useState } from 'react';
import { 
  X, Calendar, User, Tag, Clock, Paperclip, MessageSquare, 
  CheckCircle2, Edit2, Trash2, MoreVertical, Flag, Users
} from 'lucide-react';
import Button from '../common/Button';
import Badge from '../common/Badge';
import ProgressBar from '../common/ProgressBar';
import Avatar from '../common/Avatar';
import SubtaskList from './SubtaskList';
import CommentList from '../comments/CommentList';
import AttachmentList from '../attachments/AttachmentList';
import { formatDate } from '../../utils/formatters';

const TaskDetail = ({ task, onClose, onEdit, onDelete, onStatusChange }) => {
  const [activeTab, setActiveTab] = useState('details');
  const [showActions, setShowActions] = useState(false);

  if (!task) return null;

  const priorityColors = {
    LOW: 'bg-gray-100 text-gray-800',
    MEDIUM: 'bg-blue-100 text-blue-800',
    HIGH: 'bg-orange-100 text-orange-800',
    URGENT: 'bg-red-100 text-red-800'
  };

  const statusColors = {
    TODO: 'bg-gray-100 text-gray-800',
    IN_PROGRESS: 'bg-blue-100 text-blue-800',
    IN_REVIEW: 'bg-purple-100 text-purple-800',
    BLOCKED: 'bg-red-100 text-red-800',
    DONE: 'bg-green-100 text-green-800',
    CANCELLED: 'bg-gray-100 text-gray-800'
  };

  const tabs = [
    { id: 'details', label: 'Detalles', icon: <CheckCircle2 className="w-4 h-4" /> },
    { id: 'subtasks', label: 'Subtareas', icon: <CheckCircle2 className="w-4 h-4" />, count: task.subtasks?.length },
    { id: 'comments', label: 'Comentarios', icon: <MessageSquare className="w-4 h-4" />, count: task.comments?.length },
    { id: 'attachments', label: 'Archivos', icon: <Paperclip className="w-4 h-4" />, count: task.attachments?.length }
  ];

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose} />
      
      <div className="absolute right-0 top-0 bottom-0 w-full max-w-4xl bg-white shadow-xl overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 z-10 bg-white border-b px-6 py-4">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <h2 className="text-2xl font-bold text-gray-900">{task.title}</h2>
                <Badge className={priorityColors[task.priority]}>
                  <Flag className="w-3 h-3 mr-1" />
                  {task.priority}
                </Badge>
                <Badge className={statusColors[task.status]}>
                  {task.status}
                </Badge>
              </div>
              <p className="text-sm text-gray-500">
                {task.process?.name} • Creada {formatDate(task.createdAt)}
              </p>
            </div>

            <div className="flex items-center gap-2">
              <div className="relative">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setShowActions(!showActions)}
                >
                  <MoreVertical className="w-5 h-5" />
                </Button>
                {showActions && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border py-1 z-20">
                    <button
                      onClick={() => {
                        onEdit(task);
                        setShowActions(false);
                      }}
                      className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                    >
                      <Edit2 className="w-4 h-4" />
                      Editar
                    </button>
                    <button
                      onClick={() => {
                        onDelete(task.id);
                        setShowActions(false);
                      }}
                      className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-2"
                    >
                      <Trash2 className="w-4 h-4" />
                      Eliminar
                    </button>
                  </div>
                )}
              </div>
              <Button variant="ghost" size="sm" onClick={onClose}>
                <X className="w-5 h-5" />
              </Button>
            </div>
          </div>

          {/* Progress */}
          {task.completionPercentage > 0 && (
            <div className="mt-4">
              <ProgressBar 
                value={task.completionPercentage} 
                label={`${task.completionPercentage}% completado`}
              />
            </div>
          )}
        </div>

        {/* Tabs */}
        <div className="border-b px-6">
          <div className="flex gap-6">
            {tabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 px-1 py-3 border-b-2 transition-colors ${
                  activeTab === tab.id
                    ? 'border-indigo-600 text-indigo-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                {tab.icon}
                <span className="font-medium">{tab.label}</span>
                {tab.count > 0 && (
                  <span className="ml-1 px-2 py-0.5 text-xs rounded-full bg-gray-100">
                    {tab.count}
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>

        {/* Content */}
        <div className="p-6">
          {activeTab === 'details' && (
            <div className="space-y-6">
              {/* Description */}
              {task.description && (
                <div>
                  <h3 className="text-sm font-semibold text-gray-700 mb-2">Descripción</h3>
                  <p className="text-gray-600 whitespace-pre-wrap">{task.description}</p>
                </div>
              )}

              {/* Info Grid */}
              <div className="grid grid-cols-2 gap-6">
                {/* Assignees */}
                {task.assignees?.length > 0 && (
                  <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
                      <Users className="w-4 h-4" />
                      Asignado a
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {task.assignees.map(user => (
                        <div key={user.id} className="flex items-center gap-2 bg-gray-50 rounded-lg px-3 py-2">
                          <Avatar user={user} size="sm" />
                          <span className="text-sm text-gray-700">
                            {user.firstName} {user.lastName}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Dates */}
                <div className="space-y-3">
                  {task.startDate && (
                    <div>
                      <h3 className="text-sm font-semibold text-gray-700 mb-1 flex items-center gap-2">
                        <Calendar className="w-4 h-4" />
                        Fecha de inicio
                      </h3>
                      <p className="text-sm text-gray-600">{formatDate(task.startDate)}</p>
                    </div>
                  )}
                  {task.dueDate && (
                    <div>
                      <h3 className="text-sm font-semibold text-gray-700 mb-1 flex items-center gap-2">
                        <Calendar className="w-4 h-4" />
                        Fecha de vencimiento
                      </h3>
                      <p className="text-sm text-gray-600">{formatDate(task.dueDate)}</p>
                    </div>
                  )}
                </div>

                {/* Time Tracking */}
                {(task.estimatedHours || task.actualHours) && (
                  <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
                      <Clock className="w-4 h-4" />
                      Tiempo
                    </h3>
                    <div className="space-y-1 text-sm text-gray-600">
                      {task.estimatedHours && (
                        <p>Estimado: {task.estimatedHours}h</p>
                      )}
                      {task.actualHours && (
                        <p>Actual: {task.actualHours}h</p>
                      )}
                    </div>
                  </div>
                )}

                {/* Tags */}
                {task.tags?.length > 0 && (
                  <div>
                    <h3 className="text-sm font-semibold text-gray-700 mb-2 flex items-center gap-2">
                      <Tag className="w-4 h-4" />
                      Etiquetas
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {task.tags.map((tag, index) => (
                        <Badge key={index} variant="secondary">
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'subtasks' && (
            <SubtaskList taskId={task.id} subtasks={task.subtasks} />
          )}

          {activeTab === 'comments' && (
            <CommentList taskId={task.id} />
          )}

          {activeTab === 'attachments' && (
            <AttachmentList taskId={task.id} />
          )}
        </div>
      </div>
    </div>
  );
};

export default TaskDetail;