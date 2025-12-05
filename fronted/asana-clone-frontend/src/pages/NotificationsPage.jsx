// ========== src/pages/NotificationsPage.jsx ==========

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Check, CheckCheck, Trash2, Mail, Bell, Inbox } from 'lucide-react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import InvitationNotificationItem from '../components/notifications/InvitationNotificacionItem';
import notificationService from '../services/notificationService';
import api from '../services/api';
import toast from 'react-hot-toast';

const NotificationsPage = () => {
  const [activeTab, setActiveTab] = useState('notifications'); // 'notifications' | 'invitations'
  const queryClient = useQueryClient();

  // ========== NOTIFICACIONES ==========
  const {
    data: notifications,
    isLoading: notificationsLoading,
  } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationService.getNotifications(),
  });

  const markAsReadMutation = useMutation({
    mutationFn: (notificationId) => 
      notificationService.markAsRead(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries(['notifications']);
      toast.success('Notificación marcada como leída');
    },
  });

  const markAllAsReadMutation = useMutation({
    mutationFn: () => notificationService.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries(['notifications']);
      toast.success('Todas las notificaciones marcadas como leídas');
    },
  });

  const deleteNotificationMutation = useMutation({
    mutationFn: (notificationId) => 
      notificationService.deleteNotification(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries(['notifications']);
      toast.success('Notificación eliminada');
    },
  });

  // ========== INVITACIONES ==========
  const {
    data: invitations,
    isLoading: invitationsLoading,
  } = useQuery({
    queryKey: ['invitations'],
    queryFn: async () => {
      const response = await api.get('/invitations/pending');
      return response.data;
    },
  });

  const getNotificationIcon = (type) => {
    const icons = {
      TASK_ASSIGNED: '📋',
      TASK_COMMENTED: '💬',
      MENTIONED_IN_COMMENT: '👤',
      PROJECT_ADDED_AS_MEMBER: '🎯',
      PROJECT_INVITATION_RECEIVED: '📧',
  
    PROJECT_CREATED: '🚀',
      SUBSCRIPTION_RENEWAL: '💳',
      REMINDER: '🔔',
      EXPORT_READY: '📥',
    };
    return icons[type] || '📢';
  };

  const formatRelativeTime = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Justo ahora';
    if (diffMins < 60) return `Hace ${diffMins} minuto${diffMins > 1 ? 's' : ''}`;
    if (diffHours < 24) return `Hace ${diffHours} hora${diffHours > 1 ? 's' : ''}`;
    if (diffDays < 7) return `Hace ${diffDays} día${diffDays > 1 ? 's' : ''}`;
    
    return date.toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  };

  const unreadCount = notifications?.filter(n => !n.read)?.length || 0;
  const invitationsCount = invitations?.length || 0;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Centro de Notificaciones</h1>
          <p className="text-gray-600 mt-1">
            Mantente al día con todas tus actualizaciones e invitaciones
          </p>
        </div>
        {activeTab === 'notifications' && notifications?.length > 0 && (
          <Button
            variant="secondary"
            icon={CheckCheck}
            onClick={() => markAllAsReadMutation.mutate()}
            isLoading={markAllAsReadMutation.isPending}
          >
            Marcar todas como leídas
          </Button>
        )}
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b">
        <button
          onClick={() => setActiveTab('notifications')}
          className={`px-4 py-2 font-medium transition-colors relative ${
            activeTab === 'notifications'
              ? 'text-blue-600 border-b-2 border-blue-600'
              : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          <div className="flex items-center gap-2">
            <Bell className="w-5 h-5" />
            Notificaciones
            {unreadCount > 0 && (
              <Badge className="bg-red-100 text-red-700 ml-2">
                {unreadCount}
              </Badge>
            )}
          </div>
        </button>
        <button
          onClick={() => setActiveTab('invitations')}
          className={`px-4 py-2 font-medium transition-colors relative ${
            activeTab === 'invitations'
              ? 'text-blue-600 border-b-2 border-blue-600'
              : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          <div className="flex items-center gap-2">
            <Mail className="w-5 h-5" />
            Invitaciones
            {invitationsCount > 0 && (
              <Badge className="bg-blue-100 text-blue-700 ml-2">
                {invitationsCount}
              </Badge>
            )}
          </div>
        </button>
      </div>

      {/* ========== TAB: NOTIFICACIONES ========== */}
      {activeTab === 'notifications' && (
        <>
          {notificationsLoading ? (
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto" />
            </div>
          ) : notifications && notifications.length > 0 ? (
            <div className="space-y-2">
              {notifications.map((notification) => (
                <Card
                  key={notification.id}
                  className={`${
                    !notification.read ? 'bg-blue-50 border-blue-200' : ''
                  }`}
                >
                  <div className="flex items-start gap-4">
                    {/* Icon */}
                    <div className="text-3xl">
                      {getNotificationIcon(notification.type)}
                    </div>

                    {/* Content */}
                    <div className="flex-1">
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="font-medium text-gray-900">
                            {notification.title}
                          </h3>
                          <p className="text-sm text-gray-600 mt-1">
                            {notification.message}
                          </p>
                          <p className="text-xs text-gray-400 mt-2">
                            {formatRelativeTime(notification.createdAt)}
                          </p>
                        </div>

                        {/* Priority Badge */}
                        {(notification.priority === 'HIGH' ||
                          notification.priority === 'URGENT') && (
                          <Badge
                            className={
                              notification.priority === 'URGENT'
                                ? 'bg-red-100 text-red-700'
                                : 'bg-yellow-100 text-yellow-700'
                            }
                          >
                            {notification.priority}
                          </Badge>
                        )}
                      </div>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center gap-2">
                      {!notification.read && (
                        <button
                          onClick={() => markAsReadMutation.mutate(notification.id)}
                          className="p-2 text-gray-400 hover:text-blue-500 hover:bg-blue-50 rounded transition-colors"
                          title="Marcar como leída"
                        >
                          <Check className="w-5 h-5" />
                        </button>
                      )}
                      <button
                        onClick={() => deleteNotificationMutation.mutate(notification.id)}
                        className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded transition-colors"
                        title="Eliminar"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
                <Inbox className="w-8 h-8 text-gray-400" />
              </div>
              <h2 className="text-xl font-semibold text-gray-900 mb-2">
                No tienes notificaciones
              </h2>
              <p className="text-gray-600">
                Cuando recibas notificaciones, aparecerán aquí
              </p>
            </div>
          )}
        </>
      )}

      {/* ========== TAB: INVITACIONES ========== */}
      {activeTab === 'invitations' && (
        <>
          {invitationsLoading ? (
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto" />
            </div>
          ) : invitations && invitations.length > 0 ? (
            <div className="space-y-4">
              {invitations.map((invitation) => (
                <InvitationNotificationItem
                  key={invitation.id}
                  invitation={invitation}
                  onRespond={() => {
                    queryClient.invalidateQueries(['invitations']);
                    queryClient.invalidateQueries(['projects']);
                  }}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
                <Mail className="w-8 h-8 text-gray-400" />
              </div>
              <h2 className="text-xl font-semibold text-gray-900 mb-2">
                No tienes invitaciones pendientes
              </h2>
              <p className="text-gray-600">
                Cuando alguien te invite a un proyecto, aparecerá aquí
              </p>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default NotificationsPage;