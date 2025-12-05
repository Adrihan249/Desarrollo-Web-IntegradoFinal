// src/components/tasks/TaskForm.jsx
import { useState, useEffect } from 'react';
import { X, Calendar, User, Tag, Clock, Percent } from 'lucide-react';
import Button from '../common/Button';
import Input from '../common/Input';
import Modal from '../common/Modal';
import Dropdown from '../common/Dropdown';

const TaskForm = ({ isOpen, onClose, onSubmit, task = null, processes = [], users = [] }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    processId: '',
    assigneeIds: [],
    priority: 'MEDIUM',
    dueDate: '',
    startDate: '',
    tags: [],
    estimatedHours: '',
    completionPercentage: 0,
    parentTaskId: null
  });

  const [tagInput, setTagInput] = useState('');

  useEffect(() => {
    if (task) {
      setFormData({
        title: task.title || '',
        description: task.description || '',
        processId: task.process?.id || '',
        assigneeIds: task.assignees?.map(a => a.id) || [],
        priority: task.priority || 'MEDIUM',
        dueDate: task.dueDate ? task.dueDate.substring(0, 16) : '',
        startDate: task.startDate ? task.startDate.substring(0, 16) : '',
        tags: task.tags || [],
        estimatedHours: task.estimatedHours || '',
        completionPercentage: task.completionPercentage || 0,
        parentTaskId: task.parentTask?.id || null
      });
    }
  }, [task]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleAssigneeToggle = (userId) => {
    setFormData(prev => ({
      ...prev,
      assigneeIds: prev.assigneeIds.includes(userId)
        ? prev.assigneeIds.filter(id => id !== userId)
        : [...prev.assigneeIds, userId]
    }));
  };

  const handleAddTag = (e) => {
    if (e.key === 'Enter' && tagInput.trim()) {
      e.preventDefault();
      if (!formData.tags.includes(tagInput.trim())) {
        setFormData(prev => ({
          ...prev,
          tags: [...prev.tags, tagInput.trim()]
        }));
      }
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const submitData = {
      ...formData,
      dueDate: formData.dueDate ? new Date(formData.dueDate).toISOString() : null,
      startDate: formData.startDate ? new Date(formData.startDate).toISOString() : null,
      estimatedHours: formData.estimatedHours ? parseInt(formData.estimatedHours) : null
    };
    onSubmit(submitData);
  };

  const priorityOptions = [
    { value: 'LOW', label: 'Baja', color: 'bg-gray-100 text-gray-800' },
    { value: 'MEDIUM', label: 'Media', color: 'bg-blue-100 text-blue-800' },
    { value: 'HIGH', label: 'Alta', color: 'bg-orange-100 text-orange-800' },
    { value: 'URGENT', label: 'Urgente', color: 'bg-red-100 text-red-800' }
  ];

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={task ? 'Editar Tarea' : 'Nueva Tarea'}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Title */}
        <Input
          label="Título"
          name="title"
          value={formData.title}
          onChange={handleChange}
          placeholder="Nombre de la tarea"
          required
        />

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Descripción
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            placeholder="Describe la tarea..."
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Process */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Columna
            </label>
            <select
              name="processId"
              value={formData.processId}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
              required
            >
              <option value="">Seleccionar...</option>
              {processes.map(process => (
                <option key={process.id} value={process.id}>
                  {process.name}
                </option>
              ))}
            </select>
          </div>

          {/* Priority */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Prioridad
            </label>
            <select
              name="priority"
              value={formData.priority}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
            >
              {priorityOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Assignees */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            <User className="inline w-4 h-4 mr-1" />
            Asignar a
          </label>
          <div className="flex flex-wrap gap-2">
            {users.map(user => (
              <button
                key={user.id}
                type="button"
                onClick={() => handleAssigneeToggle(user.id)}
                className={`px-3 py-1 rounded-full text-sm transition-colors ${
                  formData.assigneeIds.includes(user.id)
                    ? 'bg-indigo-100 text-indigo-700 border-2 border-indigo-500'
                    : 'bg-gray-100 text-gray-700 border-2 border-transparent hover:border-gray-300'
                }`}
              >
                {user.firstName} {user.lastName}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Start Date */}
          <Input
            label="Fecha de inicio"
            type="datetime-local"
            name="startDate"
            value={formData.startDate}
            onChange={handleChange}
            icon={<Calendar className="w-4 h-4" />}
          />

          {/* Due Date */}
          <Input
            label="Fecha de vencimiento"
            type="datetime-local"
            name="dueDate"
            value={formData.dueDate}
            onChange={handleChange}
            icon={<Calendar className="w-4 h-4" />}
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Estimated Hours */}
          <Input
            label="Horas estimadas"
            type="number"
            name="estimatedHours"
            value={formData.estimatedHours}
            onChange={handleChange}
            placeholder="8"
            icon={<Clock className="w-4 h-4" />}
          />

          {/* Completion Percentage */}
          <Input
            label="Porcentaje completado"
            type="number"
            name="completionPercentage"
            value={formData.completionPercentage}
            onChange={handleChange}
            min="0"
            max="100"
            icon={<Percent className="w-4 h-4" />}
          />
        </div>

        {/* Tags */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            <Tag className="inline w-4 h-4 mr-1" />
            Etiquetas
          </label>
          <Input
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
            onKeyPress={handleAddTag}
            placeholder="Escribe y presiona Enter"
          />
          <div className="flex flex-wrap gap-2 mt-2">
            {formData.tags.map((tag, index) => (
              <span
                key={index}
                className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-indigo-100 text-indigo-700"
              >
                {tag}
                <button
                  type="button"
                  onClick={() => handleRemoveTag(tag)}
                  className="ml-2 hover:text-indigo-900"
                >
                  <X className="w-3 h-3" />
                </button>
              </span>
            ))}
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-3 pt-4 border-t">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary">
            {task ? 'Actualizar' : 'Crear'} Tarea
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default TaskForm;