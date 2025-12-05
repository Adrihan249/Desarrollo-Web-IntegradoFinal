// ========== src/pages/DashboardPage.jsx ==========
/**
 * Página principal del Dashboard
 */

import { useAuth } from '../contexts/AuthContext';
import { useProjects } from '../hooks/useProjects';
import { useTasks } from '../hooks/useTasks';
import { useNotifications } from '../contexts/NotificationContext';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import { 
  Folder, 
  CheckSquare, 
  Bell, 
  TrendingUp,
  Plus 
} from 'lucide-react';
import { Link } from 'react-router-dom';

const DashboardPage = () => {
  const { user } = useAuth();
  const { projects, isLoading: projectsLoading } = useProjects();
  const { unreadCount } = useNotifications();

  // Calcular estadísticas
  const stats = [
    {
      name: 'Proyectos Activos',
      value: projects?.filter(p => p.status === 'ACTIVE').length || 0,
      icon: Folder,
      color: 'text-blue-500',
      bg: 'bg-blue-50',
      link: '/projects',
    },
    {
      name: 'Tareas Pendientes',
      value: 0, // TODO: Calcular desde API
      icon: CheckSquare,
      color: 'text-green-500',
      bg: 'bg-green-50',
      link: '/tasks',
    },
    {
      name: 'Notificaciones',
      value: unreadCount,
      icon: Bell,
      color: 'text-yellow-500',
      bg: 'bg-yellow-50',
      link: '/notifications',
    },
    {
      name: 'En Progreso',
      value: 0, // TODO: Calcular desde API
      icon: TrendingUp,
      color: 'text-purple-500',
      bg: 'bg-purple-50',
      link: '/tasks',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            ¡Hola, {user?.firstName}! 👋
          </h1>
          <p className="text-gray-600 mt-1">
            Aquí está el resumen de tu trabajo
          </p>
        </div>
        <Link to="/projects">
          <Button icon={Plus}>Nuevo Proyecto</Button>
        </Link>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => (
          <Link key={stat.name} to={stat.link}>
            <Card hover className="cursor-pointer">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">{stat.name}</p>
                  <p className="text-3xl font-bold text-gray-900 mt-2">
                    {stat.value}
                  </p>
                </div>
                <div className={`p-3 rounded-lg ${stat.bg}`}>
                  <stat.icon className={`w-6 h-6 ${stat.color}`} />
                </div>
              </div>
            </Card>
          </Link>
        ))}
      </div>

      {/* Recent Projects */}
      <Card title="Proyectos Recientes">
        {projectsLoading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500 mx-auto" />
          </div>
        ) : projects && projects.length > 0 ? (
          <div className="space-y-4">
            {projects.slice(0, 5).map((project) => (
              <Link
                key={project.id}
                to={`/projects/${project.id}`}
                className="flex items-center justify-between p-4 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: project.color }}
                  />
                  <div>
                    <h3 className="font-medium text-gray-900">
                      {project.name}
                    </h3>
                    <p className="text-sm text-gray-500">
                      {project.members?.length || 0} miembros
                    </p>
                  </div>
                </div>
                <span className="text-sm text-gray-500">
                  {project.status}
                </span>
              </Link>
            ))}
          </div>
        ) : (
          <div className="text-center py-8">
            <p className="text-gray-500 mb-4">
              No tienes proyectos aún
            </p>
            <Link to="/projects">
              <Button icon={Plus}>Crear Proyecto</Button>
            </Link>
          </div>
        )}
      </Card>
    </div>
  );
};

export default DashboardPage;
