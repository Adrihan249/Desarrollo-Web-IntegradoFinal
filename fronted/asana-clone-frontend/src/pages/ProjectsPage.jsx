// ========== src/pages/ProjectsPage.jsx (SIN useAuth - TEMPORAL) ==========

import { useState, useMemo, useContext } from 'react';
import { useProjects } from '../hooks/useProjects';
import { useNavigate } from 'react-router-dom';
import { Plus, Search } from 'lucide-react';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Modal from '../components/common/Modal';
import EmptyState from '../components/common/EmptyState';
import ProjectCard from '../components/projects/ProjectCard';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { getProjectViewStatus } from '../utils/projectStatus';
import { useAuth } from '../contexts/AuthContext'; 

const ProjectsPage = () => {
    const { user } = useAuth(); // Esto funcionará sin el import extra
    const currentUserId = user?.id;

  const {
    projects,
    isLoading,
    toggleArchiveProject,
    createProject,
    updateProject,
    archiveProject,
    deleteProject,
  } = useProjects();

  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingProject, setEditingProject] = useState(null);

  const isEditModalOpen = !!editingProject;

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
  } = useForm();

  // Filtrado de proyectos
  const filteredProjects = useMemo(() => {
    if (!projects || !currentUserId) return [];
    return projects
      .map((project) => ({
        ...project,
        calculatedStatus: getProjectViewStatus(project, currentUserId).viewStatus,
      }))
      .filter((project) => {
        const matchesSearch = project.name.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesStatus = filterStatus === 'all' || project.calculatedStatus === filterStatus;
        return matchesSearch && matchesStatus;
      });
  }, [projects, searchQuery, filterStatus, currentUserId]);

 const onSubmitCreate = async (data) => {
    try {
      // Asegúrate de que 'createProject' maneje la promesa y la notificación
      // y que el payload se construya correctamente
      await createProject({
        ...data,
        color: data.color || '#3B82F6',
        memberIds: [], // Asumiendo que se crea sin miembros adicionales por defecto
      });
      
      // ✅ ÉXITO: La invalidación ocurre dentro de useProjects hook
      setIsCreateModalOpen(false);
      reset();
      toast.success('Proyecto creado exitosamente.');
    } catch (error) {
      // Usa el error retornado por la mutación
      toast.error(error.message || 'Error al crear el proyecto.');
    }
  };

  const onSubmitUpdate = async (data) => {
    try {
      await updateProject({
        projectId: editingProject.id,
        data: {
          ...data,
          deadline: data.deadline || null,
        },
      });
      setEditingProject(null);
      reset();
      toast.success('Proyecto actualizado exitosamente.');
    } catch (error) {
      toast.error('Error al actualizar el proyecto.');
    }
  };

  const handleEdit = (project) => {
    setEditingProject(project);
    setValue('name', project.name);
    setValue('description', project.description);
    setValue('color', project.color || '#3B82F6');
    setValue('deadline', project.deadline ? project.deadline.substring(0, 16) : '');
  };

  const handleArchive = (projectId, isCurrentlyArchived) => {
    const project = projects.find((p) => p.id === projectId);
    
    // Lógica para mostrar el mensaje correcto
    const actionText = isCurrentlyArchived ? 'desarchivar' : 'archivar';
    const actionVerb = isCurrentlyArchived ? 'Desarchivando' : 'Archivando';
    const successText = isCurrentlyArchived ? 'desarchivado y activo' : 'archivado';
    
    if (window.confirm(`¿Estás seguro de ${actionText} el proyecto "${project?.name}"?`)) {
        
        // 🚨 Llamada a la nueva función unificada
        toast.promise(toggleArchiveProject(projectId, isCurrentlyArchived), {
            loading: `${actionVerb}...`,
            success: `Proyecto ${successText}.`,
            error: `Error al ${actionText} el proyecto.`,
        });
    }
};
  const handleDelete = (projectId) => {
    const project = projects.find((p) => p.id === projectId);
    if (
      window.confirm(
        `ADVERTENCIA: ¿Estás seguro de eliminar PERMANENTEMENTE el proyecto "${project?.name}"?`
      )
    ) {
      toast.promise(deleteProject(projectId), {
        loading: 'Eliminando...',
        success: 'Proyecto eliminado permanentemente.',
        error: 'Error al eliminar el proyecto.',
      });
    }
  };

  // 🔥 MOSTRAR LOADING SI NO HAY USER
  if (!currentUserId) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto mb-4" />
          <p className="text-gray-600">Cargando datos del usuario...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Proyectos</h1>
          <p className="text-gray-600 mt-1">Gestiona todos tus proyectos en un solo lugar</p>
        </div>
        <Button
          icon={Plus}
          onClick={() => {
            reset();
            setIsCreateModalOpen(true);
          }}
        >
          Nuevo Proyecto
        </Button>
      </div>

      {/* Filtros y Búsqueda */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <Input
            icon={Search}
            placeholder="Buscar proyectos..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="all">Todos los estados</option>
          <option value="ACTIVE">Activos</option>
          <option value="IN_PROGRESS">En Progreso</option>
          <option value="DONE">Completados</option>
          <option value="CANCELLED">Cancelados</option>
          <option value="ARCHIVED">Archivados</option>
        </select>
      </div>

      {/* Lista de Proyectos */}
      {isLoading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto" />
        </div>
      ) : filteredProjects && filteredProjects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              currentUserId={currentUserId}
              onEdit={handleEdit}
              onArchive={handleArchive}
              onDelete={handleDelete}
            />
          ))}
        </div>
      ) : (
        <EmptyState
          title="No hay proyectos"
          description="Crea tu primer proyecto para comenzar a organizar tu trabajo"
          action={() => setIsCreateModalOpen(true)}
          actionLabel="Crear Proyecto"
        />
      )}

      {/* Modal Crear Proyecto */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => {
          setIsCreateModalOpen(false);
          reset();
        }}
        title="Nuevo Proyecto"
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
            <Button onClick={handleSubmit(onSubmitCreate)}>Crear Proyecto</Button>
          </>
        }
      >
        <form className="space-y-4">
          <Input
            label="Nombre del Proyecto"
            placeholder="Mi Proyecto Increíble"
            error={errors.name?.message}
            {...register('name', { required: 'El nombre es requerido' })}
          />
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows={4}
              placeholder="Describe tu proyecto..."
              {...register('description')}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Color</label>
            <input
              type="color"
              className="w-full h-10 rounded-lg cursor-pointer"
              defaultValue="#3B82F6"
              {...register('color')}
            />
          </div>
          <Input label="Fecha Límite (opcional)" type="datetime-local" {...register('deadline')} />
        </form>
      </Modal>

      {/* Modal Editar Proyecto */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => {
          setEditingProject(null);
          reset();
        }}
        title={`Editar Proyecto: ${editingProject?.name || ''}`}
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => {
                setEditingProject(null);
                reset();
              }}
            >
              Cancelar
            </Button>
            <Button onClick={handleSubmit(onSubmitUpdate)}>Guardar Cambios</Button>
          </>
        }
      >
        <form className="space-y-4">
          <Input
            label="Nombre del Proyecto"
            placeholder="Mi Proyecto Increíble"
            error={errors.name?.message}
            {...register('name', { required: 'El nombre es requerido' })}
          />
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows={4}
              placeholder="Describe tu proyecto..."
              {...register('description')}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Color</label>
            <input type="color" className="w-full h-10 rounded-lg cursor-pointer" {...register('color')} />
          </div>
          <Input label="Fecha Límite (opcional)" type="datetime-local" {...register('deadline')} />
        </form>
      </Modal>
    </div>
  );
};

export default ProjectsPage;