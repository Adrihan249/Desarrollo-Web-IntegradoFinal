// src/components/projects/ProjectList.jsx
import { useState, useEffect } from 'react';
import { Plus, Search, Filter } from 'lucide-react';
import ProjectCard from './ProjectCard';
import ProjectForm from './ProjectForm';
import Button from '../common/Button';
import Input from '../common/Input';
import EmptyState from '../common/EmptyState';
import Spinner from '../common/Spinner';
import { useProjects } from '../../hooks/useProjects';

const ProjectList = () => {
  const {
    projects,
    isLoading,
    loadProjects,
    createProject,
    updateProject,
    deleteProject,
    archiveProject
  } = useProjects();

  const [showForm, setShowForm] = useState(false);
  const [selectedProject, setSelectedProject] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    loadProjects();
  }, []);

  const handleSubmit = async (projectData) => {
    if (selectedProject) {
      await updateProject(selectedProject.id, projectData);
    } else {
      await createProject(projectData);
    }
    setShowForm(false);
    setSelectedProject(null);
  };

  const handleEdit = (project) => {
    setSelectedProject(project);
    setShowForm(true);
  };

  const handleDelete = async (projectId) => {
    if (window.confirm('¿Estás seguro de eliminar este proyecto?')) {
      await deleteProject(projectId);
    }
  };

  const handleArchive = async (projectId) => {
    if (window.confirm('¿Archivar este proyecto?')) {
      await archiveProject(projectId);
    }
  };

  // Filter projects
  const filteredProjects = projects.filter(project => {
    const matchesSearch = project.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         project.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || project.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Proyectos</h1>
          <p className="text-gray-600 mt-1">
            Gestiona y organiza tus proyectos
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => {
            setSelectedProject(null);
            setShowForm(true);
          }}
        >
          <Plus className="w-5 h-5 mr-2" />
          Nuevo Proyecto
        </Button>
      </div>

      {/* Filters */}
      <div className="flex gap-4">
        <div className="flex-1">
          <Input
            placeholder="Buscar proyectos..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<Search className="w-5 h-5" />}
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
        >
          <option value="all">Todos los estados</option>
          <option value="ACTIVE">Activo</option>
          <option value="ON_HOLD">En espera</option>
          <option value="COMPLETED">Completado</option>
          <option value="CANCELLED">Cancelado</option>
        </select>
      </div>

      {/* Projects Grid */}
      {filteredProjects.length === 0 ? (
        <EmptyState
          title="No hay proyectos"
          description="Crea tu primer proyecto para comenzar"
          action={
            <Button
              variant="primary"
              onClick={() => setShowForm(true)}
            >
              <Plus className="w-5 h-5 mr-2" />
              Crear Proyecto
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.map(project => (
            <ProjectCard
              key={project.id}
              project={project}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onArchive={handleArchive}
            />
          ))}
        </div>
      )}

      {/* Project Form Modal */}
      {showForm && (
        <ProjectForm
          isOpen={showForm}
          onClose={() => {
            setShowForm(false);
            setSelectedProject(null);
          }}
          onSubmit={handleSubmit}
          project={selectedProject}
        />
      )}
    </div>
  );
};

export default ProjectList;