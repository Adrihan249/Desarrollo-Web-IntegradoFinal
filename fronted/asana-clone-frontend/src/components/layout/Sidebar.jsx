// ========== src/components/layout/Sidebar.jsx ==========
/**
 * Sidebar con navegación principal
 */

import { 
  Home, 
  Folder, 
  CheckSquare, 
  Bell, 
  MessageSquare,
  CreditCard,
  Clock,
  Download,
  BarChart3,
  Settings 
} from 'lucide-react';
import { NavLink } from 'react-router-dom';
import clsx from 'clsx';
import { useAuth } from '../../contexts/AuthContext';

const Sidebar = ({ isOpen, onClose }) => {
  const { isAdmin } = useAuth();

  const navigation = [
    { name: 'Dashboard', icon: Home, path: '/dashboard' },
    { name: 'Proyectos', icon: Folder, path: '/projects' },
    { name: 'Mis Tareas', icon: CheckSquare, path: '/tasks' },
    { name: 'Notificaciones', icon: Bell, path: '/notifications' },
    { name: 'Chat', icon: MessageSquare, path: '/chat' },
    { name: 'Suscripción', icon: CreditCard, path: '/subscription' },
    { name: 'Recordatorios', icon: Clock, path: '/reminders' },
    { name: 'Exportar', icon: Download, path: '/exports' },
  ];

  // Agregar reportes si es admin
  if (isAdmin()) {
    navigation.push({
      name: 'Reportes',
      icon: BarChart3,
      path: '/admin/reports',
    });
  }

  navigation.push({
    name: 'Configuración',
    icon: Settings,
    path: '/settings',
  });

  return (
    <>
      {/* Mobile Overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-30 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={clsx(
          'fixed top-16 left-0 bottom-0 w-64 bg-white border-r border-gray-200 z-30 transition-transform duration-300',
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
      >
        <nav className="p-4 space-y-1">
          {navigation.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              onClick={onClose}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-primary-50 text-primary-600'
                    : 'text-gray-700 hover:bg-gray-50'
                )
              }
            >
              <item.icon className="w-5 h-5" />
              {item.name}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
};

export default Sidebar;