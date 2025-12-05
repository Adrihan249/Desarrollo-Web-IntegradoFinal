// src/components/projects/MemberList.jsx
import { useState } from 'react';
import { UserPlus, MoreVertical, Crown, Shield, User, Trash2 } from 'lucide-react';
import Avatar from '../common/Avatar';
import Button from '../common/Button';
import Badge from '../common/Badge';

const MemberList = ({ members = [], projectId, onAddMember, onRemoveMember, onUpdateRole, currentUserId }) => {
  const [showActions, setShowActions] = useState(null);

  const getRoleIcon = (role) => {
    if (role?.includes('ADMIN')) return <Crown className="w-4 h-4" />;
    if (role?.includes('MANAGER')) return <Shield className="w-4 h-4" />;
    return <User className="w-4 h-4" />;
  };

  const getRoleBadge = (roles) => {
    if (roles?.includes('ROLE_ADMIN')) return { label: 'Admin', color: 'bg-purple-100 text-purple-700' };
    if (roles?.includes('ROLE_PROJECT_MANAGER')) return { label: 'Manager', color: 'bg-blue-100 text-blue-700' };
    return { label: 'Miembro', color: 'bg-gray-100 text-gray-700' };
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200">
      {/* Header */}
      <div className="p-4 border-b flex items-center justify-between">
        <div>
          <h3 className="font-semibold text-gray-900">Miembros del equipo</h3>
          <p className="text-sm text-gray-500">{members.length} miembros</p>
        </div>
        <Button
          variant="primary"
          size="sm"
          onClick={onAddMember}
        >
          <UserPlus className="w-4 h-4 mr-2" />
          Agregar
        </Button>
      </div>

      {/* Members List */}
      <div className="divide-y">
        {members.map(member => {
          const roleBadge = getRoleBadge(member.roles);
          const isCurrentUser = member.id === currentUserId;

          return (
            <div
              key={member.id}
              className="p-4 hover:bg-gray-50 transition-colors flex items-center justify-between group"
            >
              <div className="flex items-center gap-3 flex-1">
                <Avatar user={member} size="md" />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-medium text-gray-900 truncate">
                      {member.firstName} {member.lastName}
                      {isCurrentUser && (
                        <span className="text-sm text-gray-500 ml-2">(Tú)</span>
                      )}
                    </p>
                    <Badge className={roleBadge.color}>
                      {getRoleIcon(member.roles)}
                      <span className="ml-1">{roleBadge.label}</span>
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-500 truncate">{member.email}</p>
                </div>
              </div>

              {/* Actions */}
              {!isCurrentUser && (
                <div className="relative">
                  <button
                    onClick={() => setShowActions(showActions === member.id ? null : member.id)}
                    className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <MoreVertical className="w-5 h-5" />
                  </button>

                  {showActions === member.id && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border py-1 z-10">
                      <button
                        onClick={() => {
                          onRemoveMember(member.id);
                          setShowActions(null);
                        }}
                        className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-2"
                      >
                        <Trash2 className="w-4 h-4" />
                        Remover del proyecto
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default MemberList;