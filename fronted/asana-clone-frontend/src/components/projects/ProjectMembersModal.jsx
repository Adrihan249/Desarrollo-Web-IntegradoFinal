import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import projectService from '../../services/projectService';
import Modal from '../common/Modal';
import Button from '../common/Button';
import Avatar from '../common/Avatar';
import Badge from '../common/Badge';
import { Users, Crown, Trash2, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

/**
 * Modal para ver y gestionar los miembros del proyecto
 */
const ProjectMembersModal = ({ 
  isOpen, 
  onClose, 
  project,
  currentUserId 
}) => {
  const [memberToRemove, setMemberToRemove] = useState(null);
  const queryClient = useQueryClient();

  const removeMemberMutation = useMutation({
    mutationFn: (userId) => projectService.removeMember(project.id, userId),
    onSuccess: () => {
      toast.success('Miembro removido del proyecto');
      queryClient.invalidateQueries(['project', project.id]);
      setMemberToRemove(null);
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || 'Error al remover miembro');
    },
  });

  const handleRemoveMember = (member) => {
    if (window.confirm(`¿Estás seguro de remover a ${member.firstName} ${member.lastName} del proyecto?`)) {
      removeMemberMutation.mutate(member.id);
    }
  };

  const isCreator = (memberId) => project.createdBy?.id === memberId;
  const isCurrentUser = (memberId) => memberId === currentUserId;
  const canManageMembers = isCreator(currentUserId);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Miembros del Proyecto"
      icon={Users}
    >
      <div className="space-y-4">
        {/* Header con contador */}
        <div className="flex items-center justify-between pb-3 border-b">
          <p className="text-sm text-gray-600">
            {project.members?.length || 0} miembro{project.members?.length !== 1 ? 's' : ''} en el proyecto
          </p>
        </div>

        {/* Lista de miembros */}
        <div className="space-y-2 max-h-96 overflow-y-auto">
          {project.members?.map((member) => (
            <div
              key={member.id}
              className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors"
            >
              {/* Info del miembro */}
              <div className="flex items-center gap-3 flex-1 min-w-0">
                <Avatar user={member} size="md" />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-medium text-gray-900 truncate">
                      {member.firstName} {member.lastName}
                    </p>
                    
                    {/* Badge de Creador */}
                    {isCreator(member.id) && (
                      <Badge className="bg-yellow-100 text-yellow-700 flex items-center gap-1">
                        <Crown className="w-3 h-3" />
                        Creador
                      </Badge>
                    )}
                    
                    {/* Badge de "Tú" */}
                    {isCurrentUser(member.id) && (
                      <Badge className="bg-blue-100 text-blue-700">
                        Tú
                      </Badge>
                    )}
                  </div>
                  <p className="text-sm text-gray-500 truncate">
                    {member.email}
                  </p>
                </div>
              </div>

              {/* Botón de remover (solo si puedes gestionar y no es el creador) */}
              {canManageMembers && !isCreator(member.id) && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleRemoveMember(member)}
                  isLoading={removeMemberMutation.isPending}
                  className="text-red-600 hover:bg-red-50"
                >
                  <Trash2 className="w-4 h-4" />
                </Button>
              )}
            </div>
          ))}
        </div>

        {/* Mensaje de información */}
        {!canManageMembers && (
          <div className="flex items-start gap-2 p-3 bg-blue-50 rounded-lg">
            <AlertCircle className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
            <p className="text-sm text-blue-700">
              Solo el creador del proyecto puede gestionar los miembros.
            </p>
          </div>
        )}

        {/* Footer con botón cerrar */}
        <div className="flex justify-end pt-3 border-t">
          <Button onClick={onClose}>
            Cerrar
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default ProjectMembersModal;