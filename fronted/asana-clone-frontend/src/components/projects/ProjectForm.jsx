// src/components/projects/ProjectForm.jsx
import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import Button from '../common/Button';
import Input from '../common/Input';
import Modal from '../common/Modal';
import Dropdown from '../common/Dropdown';

const ProjectForm = ({ isOpen, onClose, onSubmit, project = null, users = [] }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    color: '#6366F1',
    status: 'ACTIVE',
    deadline: '',
    memberIds: []
  });

  useEffect(() => {
    if (project) {
      setFormData({
        name: project.name || '',
        description: project.description || '',
        color: project.color || '#6366F1',
        status: project.status || 'ACTIVE',
        deadline: project.deadline ? project.deadline.substring(0, 16) : '',
        memberIds: project.members?.map(m => m.id) || []
      });
    } else {
      setFormData({
        name: '',
        description: '',
        color: '#6366F1',
        status: 'ACTIVE',
        deadline: '',
        memberIds: []
      });
    }
  }, [project]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleMemberToggle = (userId) => {
    setFormData(prev => ({
      ...prev,
      memberIds: prev.memberIds.includes(userId)
        ? prev.memberIds.filter(id => id !== userId)
        : [...prev.memberIds, userId]
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const submitData = {
      ...formData,
      deadline: formData.deadline ? new Date(formData.deadline).toISOString() : null
    };
    onSubmit(submitData);
  };

  const colors = [
    { value: '#6366F1', label: 'Índigo' },
    { value: '#8B5CF6', label: 'Violeta' },
    { value: '#EC4899', label: 'Rosa' },
    { value: '#F59E0B', label: 'Ámbar' },
    { value: '#10B981', label: 'Verde' },
    { value: '#3B82F6', label: 'Azul' },
    { value: '#EF4444', label: 'Rojo' },
    { value: '#6B7280', label: 'Gris' }
  ];

  const statuses = [
    { value: 'ACTIVE', label: 'Activo' },
    { value: 'ON_HOLD', label: 'En espera' },
    { value: 'COMPLETED', label: 'Completado' },
    { value: 'CANCELLED', label: 'Cancelado' }
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={project ? 'Editar Proyecto' : 'Nuevo Proyecto'}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Name */}
        <Input
          label="Nombre del proyecto"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="Mi proyecto"
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
            placeholder="Describe el proyecto..."
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Color */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Color
            </label>
            <div className="flex flex-wrap gap-2">
              {colors.map(color => (
                <button
                  key={color.value}
                  type="button"
                  onClick={() => setFormData({ ...formData, color: color.value })}
                  className={`w-8 h-8 rounded-full transition-transform ${
                    formData.color === color.value ? 'ring-2 ring-offset-2 ring-indigo-500 scale-110' : ''
                  }`}
                  style={{ backgroundColor: color.value }}
                  title={color.label}
                />
              ))}
            </div>
          </div>

          {/* Status */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Estado
            </label>
            <select
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
            >
              {statuses.map(status => (
                <option key={status.value} value={status.value}>
                  {status.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Deadline */}
        <Input
          label="Fecha límite"
          type="datetime-local"
          name="deadline"
          value={formData.deadline}
          onChange={handleChange}
        />

        {/* Members */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Miembros del equipo
          </label>
          <div className="max-h-48 overflow-y-auto border border-gray-300 rounded-lg p-3 space-y-2">
            {users.map(user => (
              <label
                key={user.id}
                className="flex items-center gap-2 p-2 hover:bg-gray-50 rounded cursor-pointer"
              >
                <input
                  type="checkbox"
                  checked={formData.memberIds.includes(user.id)}
                  onChange={() => handleMemberToggle(user.id)}
                  className="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
                />
                <span className="text-sm text-gray-900">
                  {user.firstName} {user.lastName}
                </span>
                <span className="text-xs text-gray-500">
                  {user.email}
                </span>
              </label>
            ))}
          </div>
          <p className="text-sm text-gray-500 mt-2">
            {formData.memberIds.length} miembro(s) seleccionado(s)
          </p>
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-3 pt-4 border-t">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary">
            {project ? 'Actualizar' : 'Crear'} Proyecto
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default ProjectForm;