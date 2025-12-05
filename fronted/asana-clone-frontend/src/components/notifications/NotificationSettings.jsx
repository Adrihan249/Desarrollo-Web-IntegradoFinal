// src/components/notifications/NotificationSettings.jsx
import { useState, useEffect } from 'react';
import { Bell, Clock, Mail, Moon } from 'lucide-react';
import Button from '../common/Button';
import Card from '../common/Card';
import Alert from '../common/Alert';
import settingsService from '../../services/settingsService';

const NotificationSettings = () => {
  const [settings, setSettings] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const data = await settingsService.getNotificationSettings();
      setSettings(data);
    } catch (error) {
      console.error('Error loading settings:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggle = (key) => {
    setSettings({
      ...settings,
      [key]: !settings[key]
    });
  };

  const handleDoNotDisturb = async () => {
    try {
      await settingsService.setDoNotDisturb(
        !settings.doNotDisturbEnabled,
        settings.doNotDisturbStart || 22,
        settings.doNotDisturbEnd || 8
      );
      setSettings({
        ...settings,
        doNotDisturbEnabled: !settings.doNotDisturbEnabled
      });
      setMessage({ type: 'success', text: 'Configuración actualizada' });
    } catch (error) {
      setMessage({ type: 'error', text: 'Error al actualizar configuración' });
    }
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await settingsService.updateNotificationSettings(settings);
      setMessage({ type: 'success', text: 'Configuración guardada correctamente' });
    } catch (error) {
      setMessage({ type: 'error', text: 'Error al guardar configuración' });
    } finally {
      setIsSaving(false);
    }
  };

  const handleReset = async () => {
    if (!window.confirm('¿Restablecer configuración por defecto?')) return;

    try {
      const data = await settingsService.resetNotificationSettings();
      setSettings(data);
      setMessage({ type: 'success', text: 'Configuración restablecida' });
    } catch (error) {
      setMessage({ type: 'error', text: 'Error al restablecer configuración' });
    }
  };

  if (isLoading) return <div>Cargando...</div>;

  const notificationTypes = [
    { key: 'taskAssigned', label: 'Tareas asignadas', icon: <Bell /> },
    { key: 'taskCompleted', label: 'Tareas completadas', icon: <Bell /> },
    { key: 'taskDueSoon', label: 'Tareas próximas a vencer', icon: <Clock /> },
    { key: 'commentMention', label: 'Menciones en comentarios', icon: <Mail /> },
    { key: 'projectInvitation', label: 'Invitaciones a proyectos', icon: <Bell /> },
  ];

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {message && (
        <Alert variant={message.type} onClose={() => setMessage(null)}>
          {message.text}
        </Alert>
      )}

      {/* Do Not Disturb */}
      <Card>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Moon className="w-5 h-5 text-gray-600" />
            <div>
              <h3 className="font-semibold text-gray-900">No molestar</h3>
              <p className="text-sm text-gray-600">
                Silenciar notificaciones durante ciertas horas
              </p>
            </div>
          </div>
          <button
            onClick={handleDoNotDisturb}
            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
              settings.doNotDisturbEnabled ? 'bg-indigo-600' : 'bg-gray-200'
            }`}
          >
            <span
              className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                settings.doNotDisturbEnabled ? 'translate-x-6' : 'translate-x-1'
              }`}
            />
          </button>
        </div>

        {settings.doNotDisturbEnabled && (
          <div className="mt-4 grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Desde
              </label>
              <input
                type="time"
                value={`${settings.doNotDisturbStart || 22}:00`}
                onChange={(e) => setSettings({
                  ...settings,
                  doNotDisturbStart: parseInt(e.target.value.split(':')[0])
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Hasta
              </label>
              <input
                type="time"
                value={`${settings.doNotDisturbEnd || 8}:00`}
                onChange={(e) => setSettings({
                  ...settings,
                  doNotDisturbEnd: parseInt(e.target.value.split(':')[0])
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
          </div>
        )}
      </Card>

      {/* Notification Types */}
      <Card title="Tipos de notificaciones">
        <div className="space-y-4">
          {notificationTypes.map(type => (
            <div key={type.key} className="flex items-center justify-between py-3 border-b last:border-b-0">
              <div className="flex items-center gap-3">
                {type.icon}
                <span className="text-gray-900">{type.label}</span>
              </div>
              <button
                onClick={() => handleToggle(type.key)}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                  settings[type.key] ? 'bg-indigo-600' : 'bg-gray-200'
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                    settings[type.key] ? 'translate-x-6' : 'translate-x-1'
                  }`}
                />
              </button>
            </div>
          ))}
        </div>
      </Card>

      {/* Email Settings */}
      <Card title="Notificaciones por email">
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-gray-900">Resumen diario</span>
            <button
              onClick={() => handleToggle('dailySummary')}
              className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                settings.dailySummary ? 'bg-indigo-600' : 'bg-gray-200'
              }`}
            >
              <span
                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                  settings.dailySummary ? 'translate-x-6' : 'translate-x-1'
                }`}
              />
            </button>
          </div>

          <div className="flex items-center justify-between">
            <span className="text-gray-900">Resumen semanal</span>
            <button
              onClick={() => handleToggle('weeklySummary')}
              className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                settings.weeklySummary ? 'bg-indigo-600' : 'bg-gray-200'
              }`}
            >
              <span
                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                  settings.weeklySummary ? 'translate-x-6' : 'translate-x-1'
                }`}
              />
            </button>
          </div>
        </div>
      </Card>

      {/* Actions */}
      <div className="flex justify-between">
        <Button variant="secondary" onClick={handleReset}>
          Restablecer
        </Button>
        <Button
          variant="primary"
          onClick={handleSave}
          isLoading={isSaving}
        >
          Guardar cambios
        </Button>
      </div>
    </div>
  );
};

export default NotificationSettings;