// ===================================
// PÁGINA DE RECORDATORIOS (SPRINT 4)
// ===================================

// ========== src/pages/RemindersPage.jsx ==========
/**
 * Página de gestión de recordatorios
 */

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import reminderService from '../services/reminderService';
import { Plus, Clock, Check, X, Bell } from 'lucide-react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import EmptyState from '../components/common/EmptyState';
import { useForm } from 'react-hook-form';
import { formatRelativeTime } from '../utils/formatters';
import toast from 'react-hot-toast';

const RemindersPage = () => {
  const queryClient = useQueryClient();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm();

  // Obtener recordatorios
  const { data: reminders, isLoading } = useQuery({
    queryKey: ['reminders'],
    queryFn: () => reminderService.getReminders(),
  });

  // Crear recordatorio
  const createReminderMutation = useMutation({
    mutationFn: reminderService.createReminder,
    onSuccess: () => {
      queryClient.invalidateQueries(['reminders']);
      toast.success('Recordatorio creado');
      reset();
      setIsCreateModalOpen(false);
    },
  });

  // Posponer recordatorio
  const snoozeMutation = useMutation({
    mutationFn: ({ id, minutes }) => reminderService.snoozeReminder(id, minutes),
    onSuccess: () => {
      queryClient.invalidateQueries(['reminders']);
      toast.success('Recordatorio pospuesto');
    },
  });

  // Descartar recordatorio
  const dismissMutation = useMutation({
    mutationFn: reminderService.dismissReminder,
    onSuccess: () => {
      queryClient.invalidateQueries(['reminders']);
      toast.success('Recordatorio descartado');
    },
  });

  const onSubmit = (data) => {
    createReminderMutation.mutate({
      ...data,
      type: 'CUSTOM',
      frequency: data.frequency || 'ONCE',
      emailNotification: data.emailNotification || false,
      inAppNotification: true,
    });
  };

  // Filtrar recordatorios pendientes
  const pendingReminders = reminders?.filter((r) => r.status === 'PENDING') || [];
  const completedReminders = reminders?.filter((r) => r.status !== 'PENDING') || [];

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Recordatorios</h1>
          <p className="text-gray-600 mt-1">
            Nunca olvides una tarea importante
          </p>
        </div>
        <Button icon={Plus} onClick={() => setIsCreateModalOpen(true)}>
          Nuevo Recordatorio
        </Button>
      </div>

      {/* Recordatorios Pendientes */}
      <Card title="Pendientes">
        {isLoading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500 mx-auto" />
          </div>
        ) : pendingReminders.length > 0 ? (
          <div className="space-y-3">
            {pendingReminders.map((reminder) => (
              <div
                key={reminder.id}
                className="flex items-start justify-between p-4 bg-gray-50 rounded-lg"
              >
                <div className="flex items-start gap-3 flex-1">
                  <Bell className="w-5 h-5 text-yellow-500 mt-1" />
                  <div className="flex-1">
                    <h3 className="font-medium text-gray-900">{reminder.title}</h3>
                    {reminder.message && (
                      <p className="text-sm text-gray-600 mt-1">{reminder.message}</p>
                    )}
                    <div className="flex items-center gap-4 mt-2">
                      <span className="text-xs text-gray-500">
                        <Clock className="w-3 h-3 inline mr-1" />
                        {formatRelativeTime(reminder.reminderDate)}
                      </span>
                      <Badge size="sm">{reminder.frequency}</Badge>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() =>
                      snoozeMutation.mutate({ id: reminder.id, minutes: 30 })
                    }
                    className="p-2 text-gray-400 hover:text-yellow-500 hover:bg-yellow-50 rounded transition-colors"
                    title="Posponer 30 min"
                  >
                    <Clock className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => dismissMutation.mutate(reminder.id)}
                    className="p-2 text-gray-400 hover:text-green-500 hover:bg-green-50 rounded transition-colors"
                    title="Completar"
                  >
                    <Check className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => dismissMutation.mutate(reminder.id)}
                    className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded transition-colors"
                    title="Descartar"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState
            icon={Bell}
            title="No hay recordatorios pendientes"
            description="Crea recordatorios para no olvidar tareas importantes"
          />
        )}
      </Card>

      {/* Recordatorios Completados */}
      {completedReminders.length > 0 && (
        <Card title="Completados">
          <div className="space-y-2">
            {completedReminders.slice(0, 5).map((reminder) => (
              <div
                key={reminder.id}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-lg opacity-60"
              >
                <div className="flex items-center gap-3">
                  <Check className="w-4 h-4 text-green-500" />
                  <span className="text-sm text-gray-700">{reminder.title}</span>
                </div>
                <span className="text-xs text-gray-500">
                  {formatRelativeTime(reminder.sentAt || reminder.dismissedAt)}
                </span>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Modal Crear Recordatorio */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => {
          setIsCreateModalOpen(false);
          reset();
        }}
        title="Nuevo Recordatorio"
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => {
                setIsCreateModalOpen(false);
                reset();
              }}
            >
              Cancelar
            </Button>
            <Button onClick={handleSubmit(onSubmit)}>Crear</Button>
          </>
        }
      >
        <form className="space-y-4">
          <Input
            label="Título"
            placeholder="¿Qué quieres recordar?"
            error={errors.title?.message}
            {...register('title', { required: 'El título es requerido' })}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Mensaje (opcional)
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows={3}
              placeholder="Detalles adicionales..."
              {...register('message')}
            />
          </div>

          <Input
            label="Fecha y Hora"
            type="datetime-local"
            error={errors.reminderDate?.message}
            {...register('reminderDate', { required: 'La fecha es requerida' })}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Frecuencia
            </label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              {...register('frequency')}
            >
              <option value="ONCE">Una vez</option>
              <option value="DAILY">Diario</option>
              <option value="WEEKLY">Semanal</option>
              <option value="MONTHLY">Mensual</option>
            </select>
          </div>

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="emailNotification"
              className="rounded border-gray-300 text-primary-500 focus:ring-primary-500"
              {...register('emailNotification')}
            />
            <label htmlFor="emailNotification" className="text-sm text-gray-700">
              Enviar notificación por email
            </label>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default RemindersPage;