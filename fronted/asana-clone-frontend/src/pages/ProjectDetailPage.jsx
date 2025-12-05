// ========== src/pages/ProjectDetailPage.jsx (ACTUALIZADO) ==========

import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext'; 
// Asegúrate de que esta ruta sea correcta
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import projectService from '../services/projectService';
import processService from '../services/processService';
import taskService from '../services/taskService';
import authService from '../services/authService';
import InviteMemberModal from '../components/projects/InviteMemberModal';
import ProjectMembersModal from '../components/projects/ProjectMembersModal';
import {
  ArrowLeft,
  UserPlus,
  Users,
  MoreVertical,
  Activity,
} from 'lucide-react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Tabs from '../components/common/Tabs';
import KanbanBoard from '../components/tasks/KanbanBoard';
import Spinner from '../components/common/Spinner';
import toast from 'react-hot-toast';

const ProjectDetailPage = () => {
  
  const { projectId } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState(0);
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);

  // Obtener usuario actual del localStorage
const { user: currentUser } = useAuth(); // ✅ USAR HOOK DEL CONTEXTO
  // Obtener proyecto
  const {
    data: project,
    isLoading: projectLoading,
    error: projectError,
  } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectService.getProjectById(projectId),
  });

  // Obtener procesos (columnas Kanban)
  const { data: processes, isLoading: processesLoading } = useQuery({
    queryKey: ['processes', projectId],
    queryFn: () => processService.getProcesses(projectId),
    enabled: !!projectId,
  });

  // Obtener tareas
  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks', projectId],
    queryFn: () => taskService.getProjectTasks(projectId),
    enabled: !!projectId,
  });

  // Crear procesos por defecto si no existen
  const createDefaultProcessesMutation = useMutation({
    mutationFn: () => processService.createDefaultProcesses(projectId),
    onSuccess: () => {
      queryClient.invalidateQueries(['processes', projectId]);
      toast.success('Columnas creadas');
    },
  });

  // Si no hay procesos, crear los por defecto
  useEffect(() => {
    if (processes && processes.length === 0 && !processesLoading) {
      createDefaultProcessesMutation.mutate();
    }
  }, [processes, processesLoading]);

  if (projectLoading || processesLoading || tasksLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Spinner size="lg" />
      </div>
    );
  }

  if (projectError || !project) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Proyecto no encontrado
        </h2>
        <Button onClick={() => navigate('/projects')}>
          Volver a Proyectos
        </Button>
      </div>
    );
  }

  // Verificar si el usuario actual es el creador
  const isCreator = project.createdBy?.id === currentUser?.id;

  // Pestañas del proyecto
  const tabs = [
    {
      label: 'Tablero',
      content: (
        <KanbanBoard
          projectId={projectId}
          processes={processes || []}
          tasks={tasks || []}
          projectMembers={project.members || []}
          isCreator={isCreator}
        />
      ),
    },
    {
      label: 'Lista',
      content: <div className="py-8 text-center text-gray-500">Vista de lista próximamente</div>,
    },
    {
      label: 'Timeline',
      content: <div className="py-8 text-center text-gray-500">Timeline próximamente</div>,
    },
    {
      label: 'Actividad',
      content: <div className="py-8 text-center text-gray-500">Actividad próximamente</div>,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            icon={ArrowLeft}
            onClick={() => navigate('/projects')}
          >
            Volver
          </Button>
          <div>
            <div className="flex items-center gap-3">
              <div
                className="w-4 h-4 rounded-full"
                style={{ backgroundColor: project.color }}
              />
              <h1 className="text-3xl font-bold text-gray-900">{project.name}</h1>
            </div>
            {project.description && (
              <p className="text-gray-600 mt-1">{project.description}</p>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* BOTÓN PARA INVITAR MIEMBRO - Solo el creador */}
          {isCreator && (
            <Button 
              variant="primary" 
              icon={UserPlus} 
              onClick={() => setIsInviteModalOpen(true)}
            >
              Invitar Miembro
            </Button>
          )}
          
          {/* BOTÓN DE MIEMBROS - Abre el modal */}
          <Button 
            variant="secondary" 
            icon={Users}
            onClick={() => setIsMembersModalOpen(true)}
          >
            Miembros ({project.members?.length || 0})
          </Button>
          
          <Button variant="secondary" icon={MoreVertical} />
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-50 rounded-lg">
              <Activity className="w-5 h-5 text-blue-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Total Tareas</p>
              <p className="text-2xl font-bold">{tasks?.length || 0}</p>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-green-50 rounded-lg">
              <Activity className="w-5 h-5 text-green-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Completadas</p>
              <p className="text-2xl font-bold">
                {tasks?.filter((t) => t.status === 'DONE').length || 0}
              </p>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-yellow-50 rounded-lg">
              <Activity className="w-5 h-5 text-yellow-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">En Progreso</p>
              <p className="text-2xl font-bold">
                {tasks?.filter((t) => t.status === 'IN_PROGRESS').length || 0}
              </p>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-purple-50 rounded-lg">
              <Users className="w-5 h-5 text-purple-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Miembros</p>
              <p className="text-2xl font-bold">{project.members?.length || 0}</p>
            </div>
          </div>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs tabs={tabs} defaultTab={activeTab} onChange={setActiveTab} />
    
      {/* MODAL DE INVITACIÓN */}
      <InviteMemberModal
        isOpen={isInviteModalOpen}
        onClose={() => setIsInviteModalOpen(false)}
        projectId={projectId}
        projectName={project.name}
      />

      {/* MODAL DE MIEMBROS */}
      <ProjectMembersModal
        isOpen={isMembersModalOpen}
        onClose={() => setIsMembersModalOpen(false)}
        project={project}
        currentUserId={currentUser?.id}
      />
    </div>
  );
};

export default ProjectDetailPage;