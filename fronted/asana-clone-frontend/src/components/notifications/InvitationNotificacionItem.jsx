import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Mail, Check, X, Building2 } from 'lucide-react';
import Button from '../common/Button';
import Card from '../common/Card';
import toast from 'react-hot-toast';
import api from '../../services/api';

/**
 * Componente para mostrar una invitación a proyecto con botones de aceptar/rechazar
 */
const InvitationNotificationItem = ({ invitation, onRespond }) => {
  const queryClient = useQueryClient();

  const respondMutation = useMutation({
    mutationFn: (status) => 
      api.put(`/invitations/${invitation.id}/respond`, { status }),
    onSuccess: (_, status) => {
      const message = status === 'ACCEPTED'
        ? `¡Te has unido al proyecto "${invitation.project.name}"!`
        : 'Invitación rechazada';
      
      toast.success(message);
      
      // Refrescar datos
      queryClient.invalidateQueries(['invitations']);
      queryClient.invalidateQueries(['projects']);
      
      if (onRespond) onRespond(status);
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || 'Error al responder invitación');
    },
  });

  const handleAccept = () => {
    respondMutation.mutate('ACCEPTED');
  };

  const handleReject = () => {
    if (window.confirm('¿Estás seguro de rechazar esta invitación?')) {
      respondMutation.mutate('REJECTED');
    }
  };

  return (
    <Card className="hover:shadow-md transition-shadow">
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-start gap-3">
          <div className="p-2 bg-blue-50 rounded-lg">
            <Mail className="w-5 h-5 text-blue-600" />
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="font-semibold text-gray-900">
              Invitación a Proyecto
            </h3>
            <p className="text-sm text-gray-500 mt-1">
              {invitation.sender.firstName} {invitation.sender.lastName} te invitó
            </p>
          </div>
        </div>

        {/* Información del proyecto */}
        <div className="flex items-center gap-2 p-3 bg-gray-50 rounded-lg">
          <Building2 className="w-5 h-5 text-gray-400" />
          <div className="flex-1 min-w-0">
            <p className="font-medium text-gray-900 truncate">
              {invitation.project.name}
            </p>
            {invitation.project.description && (
              <p className="text-sm text-gray-600 truncate">
                {invitation.project.description}
              </p>
            )}
          </div>
        </div>

        {/* Botones de acción */}
        <div className="flex gap-3">
          <Button
            variant="primary"
            onClick={handleAccept}
            isLoading={respondMutation.isPending && respondMutation.variables === 'ACCEPTED'}
            disabled={respondMutation.isPending}
            className="flex-1"
          >
            <Check className="w-4 h-4 mr-2" />
            Aceptar
          </Button>
          <Button
            variant="secondary"
            onClick={handleReject}
            isLoading={respondMutation.isPending && respondMutation.variables === 'REJECTED'}
            disabled={respondMutation.isPending}
            className="flex-1"
          >
            <X className="w-4 h-4 mr-2" />
            Rechazar
          </Button>
        </div>

        {/* Fecha de envío */}
        <p className="text-xs text-gray-400">
          Enviada el {new Date(invitation.sentAt).toLocaleDateString('es-ES', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
          })}
        </p>
      </div>
    </Card>
  );
};

export default InvitationNotificationItem;