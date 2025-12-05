// src/components/notifications/NotificationList.jsx
import { useState, useEffect } from 'react';
import { CheckCheck, Settings, Trash2 } from 'lucide-react';
import NotificationItem from './NotificationItem';
import Button from '../common/Button';
import EmptyState from '../common/EmptyState';
import Spinner from '../common/Spinner';
import { useNotifications } from '../../hooks/useNotifications';

const NotificationList = ({ onClose }) => {
  const {
    notifications,
    isLoading,
    loadNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification
  } = useNotifications();

  const [filter, setFilter] = useState('all'); // all, unread

  useEffect(() => {
    loadNotifications();
  }, []);

  const filteredNotifications = filter === 'unread'
    ? notifications.filter(n => !n.read)
    : notifications;

  return (
    <div className="flex flex-col h-[500px]">
      {/* Header */}
      <div className="p-4 border-b">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-semibold text-gray-900">Notificaciones</h3>
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={markAllAsRead}
              title="Marcar todas como leídas"
            >
              <CheckCheck className="w-4 h-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => window.location.href = '/settings/notifications'}
              title="Configuración"
            >
              <Settings className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Filters */}
        <div className="flex gap-2">
          <button
            onClick={() => setFilter('all')}
            className={`px-3 py-1 text-sm rounded-lg transition-colors ${
              filter === 'all'
                ? 'bg-indigo-100 text-indigo-700'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            Todas
          </button>
          <button
            onClick={() => setFilter('unread')}
            className={`px-3 py-1 text-sm rounded-lg transition-colors ${
              filter === 'unread'
                ? 'bg-indigo-100 text-indigo-700'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            No leídas
          </button>
        </div>
      </div>

      {/* Notifications List */}
      <div className="flex-1 overflow-y-auto">
        {isLoading ? (
          <div className="flex justify-center items-center h-full">
            <Spinner />
          </div>
        ) : filteredNotifications.length === 0 ? (
          <EmptyState
            title={filter === 'unread' ? 'No hay notificaciones nuevas' : 'No hay notificaciones'}
            description="Te notificaremos cuando haya actualizaciones"
          />
        ) : (
          <div className="divide-y">
            {filteredNotifications.map(notification => (
              <NotificationItem
                key={notification.id}
                notification={notification}
                onRead={() => markAsRead(notification.id)}
                onDelete={() => deleteNotification(notification.id)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default NotificationList;