// ===================================
// LAYOUT COMPONENTS
// ===================================

// ========== src/components/layout/Navbar.jsx ==========
/**
 * Navbar principal con notificaciones y perfil de usuario
 */

import { Bell, Menu, LogOut, Settings, User as UserIcon } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotifications } from '../../contexts/NotificationContext';
import Avatar from '../common/Avatar';
import Dropdown, { DropdownItem, DropdownDivider } from '../common/Dropdown';
import { Link } from 'react-router-dom';

const Navbar = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const { unreadCount } = useNotifications();

  return (
    <nav className="bg-white border-b border-gray-200 px-4 py-3 fixed top-0 left-0 right-0 z-40">
      <div className="flex items-center justify-between">
        {/* Left: Menu + Logo */}
        <div className="flex items-center gap-4">
          <button
            onClick={onMenuClick}
            className="lg:hidden p-2 rounded-lg hover:bg-gray-100"
          >
            <Menu className="w-6 h-6" />
          </button>
          <Link to="/dashboard" className="text-2xl font-bold text-primary-500">
            WorkSync
          </Link>
        </div>

        {/* Right: Notificaciones + Usuario */}
        <div className="flex items-center gap-4">
          {/* Notificaciones */}
          <Link
            to="/notifications"
            className="relative p-2 rounded-lg hover:bg-gray-100"
          >
            <Bell className="w-6 h-6 text-gray-600" />
            {unreadCount > 0 && (
              <span className="absolute top-1 right-1 w-5 h-5 bg-red-500 text-white text-xs font-bold rounded-full flex items-center justify-center">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </Link>

          {/* Usuario Dropdown */}
          <Dropdown
            trigger={
              <div className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 cursor-pointer">
                <Avatar
                  name={`${user?.firstName} ${user?.lastName}`}
                  size="sm"
                />
                <span className="hidden md:block text-sm font-medium">
                  {user?.firstName}
                </span>
              </div>
            }
            align="right"
          >
            <div className="px-4 py-3 border-b border-gray-200">
              <p className="text-sm font-medium text-gray-900">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-xs text-gray-500">{user?.email}</p>
            </div>
            <DropdownItem
              icon={UserIcon}
              onClick={() => window.location.href = '/profile'}
            >
              Mi Perfil
            </DropdownItem>
            <DropdownItem
              icon={Settings}
              onClick={() => window.location.href = '/settings'}
            >
              Configuración
            </DropdownItem>
            <DropdownDivider />
            <DropdownItem icon={LogOut} onClick={logout} danger>
              Cerrar Sesión
            </DropdownItem>
          </Dropdown>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
