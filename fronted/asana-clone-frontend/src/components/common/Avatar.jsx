// ========== src/components/common/Avatar.jsx ==========
/**
 * Componente Avatar para mostrar imagen de usuario
 */

import { User } from 'lucide-react';
import clsx from 'clsx';

const Avatar = ({
  src,
  alt,
  name,
  size = 'md',
  className = '',
}) => {
  const sizes = {
    sm: 'w-8 h-8 text-xs',
    md: 'w-10 h-10 text-sm',
    lg: 'w-12 h-12 text-base',
    xl: 'w-16 h-16 text-lg',
  };

  // Obtener iniciales del nombre
  const getInitials = (name) => {
    if (!name) return '';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  return (
    <div
      className={clsx(
        'relative inline-flex items-center justify-center rounded-full bg-gray-200 overflow-hidden',
        sizes[size],
        className
      )}
    >
      {src ? (
        <img src={src} alt={alt || name} className="w-full h-full object-cover" />
      ) : name ? (
        <span className="font-medium text-gray-700">{getInitials(name)}</span>
      ) : (
        <User className="w-1/2 h-1/2 text-gray-400" />
      )}
    </div>
  );
};

export default Avatar;
