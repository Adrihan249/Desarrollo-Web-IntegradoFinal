// src/components/notifications/NotificationItem.jsx
import { 
  CheckCircle2, 
  AlertCircle, 
  Info, 
  MessageSquare, 
  UserPlus, 
  Clock,
  Trash2 
} from 'lucide-react';
import { formatDate } from '../../utils/formatters';

const NotificationItem = ({ notification, onRead, onDelete }) => {
  const getIcon = () => {
    switch (notification.type) {
      case 'TASK_ASSIGNED':
        return <CheckCircle2 className="w-5 h-5 text-blue-600" />;
      case 'TASK_COMPLETED':
        return <CheckCircle2 className="w-5 h-5 text-green-600" />;
      case 'COMMENT_MENTION':
        return <MessageSquare className="w-5 h-5 text-purple-600" />;
      case 'PROJECT_INVITATION':
        return <UserPlus className="w-5 h-5 text-indigo-600" />;
      case 'DEADLINE_APPROACHING':
        return <Clock className="w-5 h-5 text-orange-600" />;
      case 'TASK_OVERDUE':
        return <AlertCircle className="w-5 h-5 text-red-600" />;
      default:
        return <Info className="w-5 h-5 text-gray-600" />;
    }
  };

  const getPriorityColor = () => {
    switch (notification.priority) {
      case 'URGENT':
        return 'border-l-4 border-red-500';
      case 'HIGH':
        return 'border-l-4 border-orange-500';
      case 'NORMAL':
        return 'border-l-4 border-blue-500';
      default:
        return 'border-l-4 border-gray-300';
    }
  };

  const handleClick = () => {
    if (!notification.read) {
      onRead();
    }
    // Navigate to reference if exists
    if (notification.referenceId && notification.referenceType === 'TASK') {
      window.location.href = `/tasks/${notification.referenceId}`;
    }
  };

  return (
    <div
      onClick={handleClick}
      className={`p-4 hover:bg-gray-50 cursor-pointer transition-colors ${
        !notification.read ? 'bg-blue-50' : ''
      } ${getPriorityColor()}`}
    >
      <div className="flex gap-3">
        <div className="flex-shrink-0 mt-1">{getIcon()}</div>

        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div className="flex-1">
              <p className={`text-sm ${!notification.read ? 'font-semibold' : 'font-medium'} text-gray-900`}>
                {notification.title}
              </p>
              <p className="text-sm text-gray-600 mt-1">{notification.message}</p>
            </div>

            <button
              onClick={(e) => {
                e.stopPropagation();
                onDelete();
              }}
              className="flex-shrink-0 p-1 text-gray-400 hover:text-red-600 rounded"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>

          <div className="flex items-center gap-3 mt-2">
            <span className="text-xs text-gray-500">
              {formatDate(notification.createdAt)}
            </span>
            {!notification.read && (
              <span className="w-2 h-2 bg-blue-600 rounded-full"></span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotificationItem;