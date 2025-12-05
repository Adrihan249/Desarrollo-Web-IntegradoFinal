// src/components/activity/ActivityItem.jsx
import { 
  CheckCircle2, 
  Circle, 
  Edit, 
  Trash2, 
  UserPlus, 
  UserMinus,
  MessageSquare,
  Paperclip,
  ArrowRight
} from 'lucide-react';
import Avatar from '../common/Avatar';
import { formatDate } from '../../utils/formatters';

const ActivityItem = ({ activity, isLast }) => {
  const getIcon = () => {
    switch (activity.actionType) {
      case 'TASK_CREATED':
        return <Circle className="w-5 h-5 text-blue-600" />;
      case 'TASK_COMPLETED':
        return <CheckCircle2 className="w-5 h-5 text-green-600" />;
      case 'TASK_UPDATED':
        return <Edit className="w-5 h-5 text-orange-600" />;
      case 'TASK_DELETED':
        return <Trash2 className="w-5 h-5 text-red-600" />;
      case 'MEMBER_ADDED':
        return <UserPlus className="w-5 h-5 text-indigo-600" />;
      case 'MEMBER_REMOVED':
        return <UserMinus className="w-5 h-5 text-gray-600" />;
      case 'COMMENT_ADDED':
        return <MessageSquare className="w-5 h-5 text-purple-600" />;
      case 'ATTACHMENT_ADDED':
        return <Paperclip className="w-5 h-5 text-teal-600" />;
      default:
        return <Circle className="w-5 h-5 text-gray-600" />;
    }
  };

  const getDescription = () => {
    const user = `${activity.user.firstName} ${activity.user.lastName}`;
    
    switch (activity.actionType) {
      case 'TASK_CREATED':
        return `${user} creó la tarea "${activity.entityName}"`;
      case 'TASK_COMPLETED':
        return `${user} completó la tarea "${activity.entityName}"`;
      case 'TASK_UPDATED':
        return `${user} actualizó la tarea "${activity.entityName}"`;
      case 'TASK_DELETED':
        return `${user} eliminó una tarea`;
      case 'MEMBER_ADDED':
        return `${user} agregó un miembro al proyecto`;
      case 'MEMBER_REMOVED':
        return `${user} removió un miembro del proyecto`;
      case 'COMMENT_ADDED':
        return `${user} comentó en "${activity.entityName}"`;
      case 'ATTACHMENT_ADDED':
        return `${user} adjuntó un archivo a "${activity.entityName}"`;
      default:
        return activity.description || `${user} realizó una acción`;
    }
  };

  return (
    <div className="relative flex gap-3">
      {/* Icon */}
      <div className="relative z-10 flex-shrink-0 w-10 h-10 bg-white rounded-full border-2 border-gray-200 flex items-center justify-center">
        {getIcon()}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0 pb-4">
        <div className="bg-white rounded-lg border border-gray-200 p-4 hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between gap-3">
            <div className="flex-1 min-w-0">
              <p className="text-sm text-gray-900">{getDescription()}</p>
              
              {/* Old/New Values */}
              {activity.oldValue && activity.newValue && (
                <div className="mt-2 flex items-center gap-2 text-xs text-gray-600">
                  <span className="px-2 py-1 bg-red-50 text-red-700 rounded">
                    {activity.oldValue}
                  </span>
                  <ArrowRight className="w-3 h-3" />
                  <span className="px-2 py-1 bg-green-50 text-green-700 rounded">
                    {activity.newValue}
                  </span>
                </div>
              )}

              <div className="flex items-center gap-3 mt-2">
                <Avatar user={activity.user} size="xs" />
                <span className="text-xs text-gray-500">
                  {formatDate(activity.createdAt)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ActivityItem;