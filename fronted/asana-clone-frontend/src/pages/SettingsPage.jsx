import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../contexts/AuthContext';
import userService from '../services/userService';
import { User, Lock, Bell, Shield, Save, X, Trash2, UserCog } from 'lucide-react';
import toast from 'react-hot-toast';

// ===================================
// COMPONENTES COMUNES
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
// PESTAÑA: PERFIL
// ===================================
const ProfileTab = ({ user }) => {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    bio: user?.bio || '',
    phoneNumber: user?.phoneNumber || '',
    avatarUrl: user?.avatarUrl || '',
  });

  const updateMutation = useMutation({
    mutationFn: (data) => userService.updateUser(user.id, data),
    onSuccess: () => {
      toast.success('✅ Perfil actualizado correctamente');
      queryClient.invalidateQueries(['currentUser']);
    },
    onError: (error) => {
      toast.error('❌ Error al actualizar perfil');
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    updateMutation.mutate(formData);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="space-y-6">
      {/* Avatar */}
      <div className="flex items-center gap-6">
        <div className="w-24 h-24 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-3xl font-bold">
          {user?.firstName?.[0]}{user?.lastName?.[0]}
        </div>
        <div className="flex-1">
          <Input
            label="URL del Avatar"
            name="avatarUrl"
            value={formData.avatarUrl}
            onChange={handleChange}
            placeholder="https://ejemplo.com/avatar.jpg"
          />
          <p className="text-xs text-gray-500 mt-1">
            Puedes usar Gravatar, Imgur o cualquier URL de imagen pública
          </p>
        </div>
      </div>

      {/* Nombre y Apellido */}
      <div className="grid grid-cols-2 gap-4">
        <Input
          label="Nombre"
          name="firstName"
          value={formData.firstName}
          onChange={handleChange}
          required
        />
        <Input
          label="Apellido"
          name="lastName"
          value={formData.lastName}
          onChange={handleChange}
          required
        />
      </div>

      {/* Email (readonly) */}
      <Input
        label="Email"
        value={user?.email}
        disabled
        className="bg-gray-100"
      />

      {/* Teléfono */}
      <Input
        label="Teléfono"
        name="phoneNumber"
        value={formData.phoneNumber}
        onChange={handleChange}
        placeholder="+51 999 999 999"
      />

      {/* Bio */}
      <Textarea
        label="Biografía"
        name="bio"
        value={formData.bio}
        onChange={handleChange}
        rows={4}
        placeholder="Cuéntanos un poco sobre ti..."
        maxLength={500}
      />
      <p className="text-xs text-gray-500 -mt-3">{formData.bio.length}/500 caracteres</p>

      {/* Cambiar Contraseña */}
      <ChangePasswordForm userId={user?.id} />

      {/* Botón Guardar */}
      <Button
        onClick={handleSubmit}
        icon={Save}
        disabled={updateMutation.isLoading}
      >
        {updateMutation.isLoading ? 'Guardando...' : 'Guardar Cambios'}
      </Button>
    </div>
  );
};

// ===================================
// FORMULARIO: CAMBIAR CONTRASEÑA
// ===================================
const ChangePasswordForm = ({ userId }) => {
  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [showForm, setShowForm] = useState(false);

  const passwordMutation = useMutation({
    mutationFn: (data) => userService.changePassword(userId, data),
    onSuccess: () => {
      toast.success('✅ Contraseña cambiada correctamente');
      setPasswords({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setShowForm(false);
    },
    onError: (error) => {
      toast.error('❌ Contraseña actual incorrecta');
    },
  });

  const handleSubmit = () => {
    if (passwords.newPassword !== passwords.confirmPassword) {
      toast.error('❌ Las contraseñas no coinciden');
      return;
    }
    if (passwords.newPassword.length < 8) {
      toast.error('❌ La contraseña debe tener al menos 8 caracteres');
      return;
    }
    passwordMutation.mutate({
      currentPassword: passwords.currentPassword,
      newPassword: passwords.newPassword,
    });
  };

  if (!showForm) {
    return (
      <Button
        icon={Lock}
        variant="secondary"
        onClick={() => setShowForm(true)}
      >
        Cambiar Contraseña
      </Button>
    );
  }

  return (
    <Card className="border-2 border-blue-100 bg-blue-50">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Lock className="w-5 h-5" />
          Cambiar Contraseña
        </h3>
        <button onClick={() => setShowForm(false)} className="text-gray-500 hover:text-gray-700">
          <X className="w-5 h-5" />
        </button>
      </div>

      <div className="space-y-4">
        <Input
          label="Contraseña Actual"
          type="password"
          value={passwords.currentPassword}
          onChange={(e) => setPasswords({ ...passwords, currentPassword: e.target.value })}
          required
        />
        <Input
          label="Nueva Contraseña"
          type="password"
          value={passwords.newPassword}
          onChange={(e) => setPasswords({ ...passwords, newPassword: e.target.value })}
          required
        />
        <Input
          label="Confirmar Nueva Contraseña"
          type="password"
          value={passwords.confirmPassword}
          onChange={(e) => setPasswords({ ...passwords, confirmPassword: e.target.value })}
          required
        />

        <Button
          onClick={handleSubmit}
          icon={Lock}
          disabled={passwordMutation.isLoading}
        >
          {passwordMutation.isLoading ? 'Cambiando...' : 'Cambiar Contraseña'}
        </Button>
      </div>
    </Card>
  );
};

// ===================================
// PESTAÑA: NOTIFICACIONES (ESTÁTICA)
// ===================================
const NotificationsTab = () => {
  const [settings, setSettings] = useState({
    emailNotifications: true,
    taskAssignments: true,
    projectUpdates: true,
    comments: false,
    deadlines: true,
    weeklyDigest: false,
  });

  const handleToggle = (key) => {
    setSettings({ ...settings, [key]: !settings[key] });
    toast.success('✅ Configuración actualizada');
  };

  const NotificationToggle = ({ label, description, enabled, onToggle }) => (
    <div className="flex items-center justify-between py-3 border-b border-gray-200 last:border-0">
      <div>
        <p className="font-medium text-gray-900">{label}</p>
        <p className="text-sm text-gray-500">{description}</p>
      </div>
      <button
        onClick={onToggle}
        className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
          enabled ? 'bg-blue-600' : 'bg-gray-300'
        }`}
      >
        <span
          className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
            enabled ? 'translate-x-6' : 'translate-x-1'
          }`}
        />
      </button>
    </div>
  );

  return (
    <div className="space-y-6">
      <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
        <Bell className="w-5 h-5" />
        Preferencias de Notificaciones
      </h3>

      <Card>
        <NotificationToggle
          label="Notificaciones por Email"
          description="Recibir notificaciones en tu correo electrónico"
          enabled={settings.emailNotifications}
          onToggle={() => handleToggle('emailNotifications')}
        />
        <NotificationToggle
          label="Asignación de Tareas"
          description="Notificar cuando te asignen una tarea"
          enabled={settings.taskAssignments}
          onToggle={() => handleToggle('taskAssignments')}
        />
        <NotificationToggle
          label="Actualizaciones de Proyecto"
          description="Cambios importantes en tus proyectos"
          enabled={settings.projectUpdates}
          onToggle={() => handleToggle('projectUpdates')}
        />
        <NotificationToggle
          label="Comentarios"
          description="Notificar cuando alguien comente en tus tareas"
          enabled={settings.comments}
          onToggle={() => handleToggle('comments')}
        />
        <NotificationToggle
          label="Recordatorios de Fechas Límite"
          description="Alertas 24 horas antes del vencimiento"
          enabled={settings.deadlines}
          onToggle={() => handleToggle('deadlines')}
        />
        <NotificationToggle
          label="Resumen Semanal"
          description="Recibir un resumen de actividad cada semana"
          enabled={settings.weeklyDigest}
          onToggle={() => handleToggle('weeklyDigest')}
        />
      </Card>
    </div>
  );
};

// ===================================
// PESTAÑA: ADMINISTRACIÓN (SOLO ADMIN)
// ===================================
const AdminTab = () => {
  const { data: users, isLoading } = useQuery({
    queryKey: ['allUsers'],
    queryFn: userService.getAllUsers,
  });

  const queryClient = useQueryClient();

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

  const handleRoleChange = (userId, newRole) => {
    assignRoleMutation.mutate({ userId, roles: [newRole] });
  };

  if (isLoading) {
    return <div className="text-center py-8">Cargando usuarios...</div>;
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
                        <p className="font-medium text-gray-900">{user.firstName} {user.lastName}</p>
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-4 text-gray-600">{user.email}</td>
                  <td className="py-3 px-4">
                    <select
                      value={Array.from(user.roles || [])[0] || 'ROLE_MEMBER'}
                      onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      className="px-3 py-1 border border-gray-300 rounded-lg text-sm"
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
                  <td className="py-3 px-4 text-center">
                    <button
                      className="text-blue-600 hover:text-blue-800 mx-2"
                      title="Editar"
                    >
                      <UserCog className="w-4 h-4" />
                    </button>
                    <button
                      className="text-red-600 hover:text-red-800 mx-2"
                      title="Eliminar"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};

// ===================================
// COMPONENTE PRINCIPAL
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

      {/* Tabs */}
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

      {/* Content */}
      <Card>
        {activeTab === 'profile' && <ProfileTab user={user} />}
        {activeTab === 'notifications' && <NotificationsTab />}
        {activeTab === 'admin' && isAdmin && <AdminTab />}
      </Card>
    </div>
  );
};

export default SettingsPage;