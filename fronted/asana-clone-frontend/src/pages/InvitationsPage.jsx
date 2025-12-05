import { useQuery } from '@tanstack/react-query';
import { Mail, Inbox } from 'lucide-react';
import InvitationNotificationItem from '../components/notifications/InvitationNotificacionItem';
import Spinner from '../components/common/Spinner';
import api from '../services/api';

/**
 * Página para mostrar las invitaciones pendientes del usuario
 */
const InvitationsPage = () => {
  const { data: invitations, isLoading } = useQuery({
    queryKey: ['invitations'],
    queryFn: async () => {
      const response = await api.get('/invitations/pending');
      return response.data;
    },
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <div className="p-3 bg-blue-50 rounded-lg">
          <Mail className="w-6 h-6 text-blue-600" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Invitaciones Pendientes
          </h1>
          <p className="text-gray-600">
            {invitations?.length || 0} invitación{invitations?.length !== 1 ? 'es' : ''} pendiente{invitations?.length !== 1 ? 's' : ''}
          </p>
        </div>
      </div>

      {/* Lista de invitaciones */}
      {invitations?.length > 0 ? (
        <div className="space-y-4">
          {invitations.map((invitation) => (
            <InvitationNotificationItem
              key={invitation.id}
              invitation={invitation}
            />
          ))}
        </div>
      ) : (
        /* Estado vacío */
        <div className="text-center py-12">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
            <Inbox className="w-8 h-8 text-gray-400" />
          </div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            No tienes invitaciones pendientes
          </h2>
          <p className="text-gray-600">
            Cuando alguien te invite a un proyecto, aparecerá aquí.
          </p>
        </div>
      )}
    </div>
  );
};

export default InvitationsPage;