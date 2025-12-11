import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../contexts/AuthContext';
import userService from '../services/userService';
import { 
  User, Lock, Bell, Shield, Save, X, Trash2, UserCog, 
  Edit, UserCheck, UserX, AlertCircle 
} from 'lucide-react';
import toast from 'react-hot-toast';

// ===================================
// COMPONENTES COMUNES (sin cambios)
// ===================================
const Card = ({ children, className = '' }) => (
  <div className={`bg-white rounded-xl shadow-lg p-6 border border-gray-100 ${className}`}>
    {children}
  </div>
);

const Button = ({ children, onClick, icon: Icon, disabled, variant = 'primary', className = '' }) => {
  const baseClasses = "font-medium rounded-lg text-sm px-4 py-2 flex items-center justify-center transition duration-150";
  const variants = {
    primary: "bg-blue-600 text-white hover:bg-blue-700 disabled:bg-blue-400",
    danger: "bg-red-600 text-white hover:bg-red-700 disabled:bg-red-400",
    secondary: "bg-gray-200 text-gray-800 hover:bg-gray-300 disabled:bg-gray-100",
    success: "bg-green-600 text-white hover:bg-green-700 disabled:bg-green-400",
  };

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`${baseClasses} ${variants[variant]} ${className} ${disabled ? 'cursor-not-allowed opacity-75' : ''}`}
    >
      {Icon && <Icon className="w-4 h-4 mr-2" />}
      {children}
    </button>
  );
};

const Input = ({ label, error, ...props }) => (
  <div className="mb-4">
    {label && <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>}
    <input
      {...props}
      className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
        error ? 'border-red-500' : 'border-gray-300'
      }`}
    />
    {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
  </div>
);

const Textarea = ({ label, error, ...props }) => (
  <div className="mb-4">
    {label && <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>}
    <textarea
      {...props}
      className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
        error ? 'border-red-500' : 'border-gray-300'
      }`}
    />
    {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
  </div>
);

// ===================================
// MODAL GENÉRICO
// ===================================
const Modal = ({ isOpen, onClose, title, children, footer }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl w-full max-w-md shadow-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-bold text-gray-900">{title}</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
            <X className="w-5 h-5" />
          </button>
        </div>
        <div className="p-6">{children}</div>
        {footer && (
          <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
};

// ===================================
// PESTAÑA: ADMINISTRACIÓN (COMPLETA)
// ===================================
const AdminTab = () => {
  const { user: currentUser } = useAuth();
  const queryClient = useQueryClient();
  
  const [editingUser, setEditingUser] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editFormData, setEditFormData] = useState({
    firstName: '',
    lastName: '',
    bio: '',
    phoneNumber: '',
    avatarUrl: '',
  });

  // 📥 Cargar usuarios
  const { data: users, isLoading } = useQuery({
    queryKey: ['allUsers'],
    queryFn: userService.getAllUsers,
  });

  // 🔄 Mutación: Actualizar rol
  const assignRoleMutation = useMutation({
    mutationFn: ({ userId, roles }) => userService.assignRoles(userId, roles),
    onSuccess: () => {
      toast.success('✅ Rol actualizado correctamente');
      queryClient.invalidateQueries(['allUsers']);
    },
    onError: () => {
      toast.error('❌ Error al actualizar rol');
    },
  });

  // 🔄 Mutación: Editar usuario
  const updateUserMutation = useMutation({
    mutationFn: ({ userId, data }) => userService.updateUser(userId, data),
    onSuccess: () => {
      toast.success('✅ Usuario actualizado correctamente');
      queryClient.invalidateQueries(['allUsers']);
      setShowEditModal(false);
      setEditingUser(null);
    },
    onError: () => {
      toast.error('❌ Error al actualizar usuario');
    },
  });

  // 🔄 Mutación: Desactivar usuario
  const deleteUserMutation = useMutation({
    mutationFn: (userId) => userService.deleteUser(userId),
    onSuccess: () => {
      toast.success('✅ Usuario desactivado correctamente');
      queryClient.invalidateQueries(['allUsers']);
    },
    onError: () => {
      toast.error('❌ Error al desactivar usuario');
    },
  });

  // 🔄 Mutación: Activar usuario
  const activateUserMutation = useMutation({
    mutationFn: (userId) => userService.activateUser(userId),
    onSuccess: () => {
      toast.success('✅ Usuario activado correctamente');
      queryClient.invalidateQueries(['allUsers']);
    },
    onError: () => {
      toast.error('❌ Error al activar usuario');
    },
  });

  // Handlers
  const handleRoleChange = (userId, newRole) => {
    assignRoleMutation.mutate({ userId, roles: [newRole] });
  };

  const handleEditClick = (user) => {
    setEditingUser(user);
    setEditFormData({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      bio: user.bio || '',
      phoneNumber: user.phoneNumber || '',
      avatarUrl: user.avatarUrl || '',
    });
    setShowEditModal(true);
  };

  const handleEditSubmit = () => {
    if (!editingUser) return;
    updateUserMutation.mutate({
      userId: editingUser.id,
      data: editFormData,
    });
  };

  const handleDeleteClick = (user) => {
    if (user.id === currentUser.id) {
      toast.error('❌ No puedes desactivar tu propia cuenta');
      return;
    }

    if (window.confirm(`¿Desactivar al usuario "${user.firstName} ${user.lastName}"?\n\nEsta acción no eliminará al usuario permanentemente, solo lo desactivará.`)) {
      deleteUserMutation.mutate(user.id);
    }
  };

  const handleActivateClick = (user) => {
    if (window.confirm(`¿Reactivar al usuario "${user.firstName} ${user.lastName}"?`)) {
      activateUserMutation.mutate(user.id);
    }
  };

  if (isLoading) {
    return (
      <div className="text-center py-8">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4" />
        <p className="text-gray-600">Cargando usuarios...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Shield className="w-5 h-5" />
          Gestión de Usuarios
        </h3>
        <span className="text-sm text-gray-500">{users?.length || 0} usuarios registrados</span>
      </div>

      <Card>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Usuario</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Email</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Rol</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Estado</th>
                <th className="text-center py-3 px-4 font-semibold text-gray-700">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {users?.map((user) => (
                <tr key={user.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-bold">
                        {user.firstName?.[0]}{user.lastName?.[0]}
                      </div>
                      <div>
                        <p className="font-medium text-gray-900">
                          {user.firstName} {user.lastName}
                        </p>
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-4 text-gray-600">{user.email}</td>
                  <td className="py-3 px-4">
                    <select
                      value={Array.from(user.roles || [])[0] || 'ROLE_MEMBER'}
                      onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      disabled={user.id === currentUser.id}
                      className="px-3 py-1 border border-gray-300 rounded-lg text-sm disabled:bg-gray-100"
                    >
                      <option value="ROLE_ADMIN">Admin</option>
                      <option value="ROLE_PROJECT_MANAGER">Project Manager</option>
                      <option value="ROLE_MEMBER">Member</option>
                      <option value="ROLE_VIEWER">Viewer</option>
                    </select>
                  </td>
                  <td className="py-3 px-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      user.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                    }`}>
                      {user.active ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    <div className="flex items-center justify-center gap-2">
                      {/* Botón Editar */}
                      <button
                        onClick={() => handleEditClick(user)}
                        className="text-blue-600 hover:text-blue-800 p-1 rounded hover:bg-blue-50 transition"
                        title="Editar usuario"
                      >
                        <Edit className="w-4 h-4" />
                      </button>

                      {/* Botón Desactivar/Activar */}
                      {user.active ? (
                        <button
                          onClick={() => handleDeleteClick(user)}
                          disabled={user.id === currentUser.id}
                          className="text-red-600 hover:text-red-800 p-1 rounded hover:bg-red-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
                          title="Desactivar usuario"
                        >
                          <UserX className="w-4 h-4" />
                        </button>
                      ) : (
                        <button
                          onClick={() => handleActivateClick(user)}
                          className="text-green-600 hover:text-green-800 p-1 rounded hover:bg-green-50 transition"
                          title="Activar usuario"
                        >
                          <UserCheck className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Modal Editar Usuario */}
      <Modal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setEditingUser(null);
        }}
        title={`Editar Usuario: ${editingUser?.firstName} ${editingUser?.lastName}`}
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => {
                setShowEditModal(false);
                setEditingUser(null);
              }}
            >
              Cancelar
            </Button>
            <Button
              onClick={handleEditSubmit}
              disabled={updateUserMutation.isLoading}
            >
              {updateUserMutation.isLoading ? 'Guardando...' : 'Guardar Cambios'}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Nombre"
              value={editFormData.firstName}
              onChange={(e) => setEditFormData({ ...editFormData, firstName: e.target.value })}
              required
            />
            <Input
              label="Apellido"
              value={editFormData.lastName}
              onChange={(e) => setEditFormData({ ...editFormData, lastName: e.target.value })}
              required
            />
          </div>

          <Input
            label="Teléfono"
            value={editFormData.phoneNumber}
            onChange={(e) => setEditFormData({ ...editFormData, phoneNumber: e.target.value })}
            placeholder="+51 999 999 999"
          />

          <Input
            label="URL del Avatar"
            value={editFormData.avatarUrl}
            onChange={(e) => setEditFormData({ ...editFormData, avatarUrl: e.target.value })}
            placeholder="https://ejemplo.com/avatar.jpg"
          />

          <Textarea
            label="Biografía"
            value={editFormData.bio}
            onChange={(e) => setEditFormData({ ...editFormData, bio: e.target.value })}
            rows={4}
            placeholder="Biografía del usuario..."
            maxLength={500}
          />

          <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 flex items-start gap-2">
            <AlertCircle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
            <div className="text-sm text-amber-800">
              <p className="font-medium">Nota:</p>
              <p>No se puede cambiar el email desde aquí. El usuario debe actualizar su email desde su perfil.</p>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
};

// ===================================
// COMPONENTE PRINCIPAL (sin cambios)
// ===================================
const SettingsPage = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('profile');

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  const tabs = [
    { id: 'profile', label: 'Perfil', icon: User },
    { id: 'notifications', label: 'Notificaciones', icon: Bell },
    ...(isAdmin ? [{ id: 'admin', label: 'Administración', icon: Shield }] : []),
  ];

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-3xl font-bold text-gray-900 mb-6">⚙️ Configuración</h1>

      <div className="flex gap-2 mb-6 border-b border-gray-200">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-3 font-medium transition-colors flex items-center gap-2 ${
                activeTab === tab.id
                  ? 'text-blue-600 border-b-2 border-blue-600'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <Icon className="w-4 h-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      <Card>
        {activeTab === 'admin' && isAdmin && <AdminTab />}
      </Card>
    </div>
  );
};

export default SettingsPage;
