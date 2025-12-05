// ========== src/components/common/Alert.jsx ==========
/**
 * Componente Alert para mensajes informativos
 */

import { AlertCircle, CheckCircle, Info, XCircle, X } from 'lucide-react';
import clsx from 'clsx';

const Alert = ({
  type = 'info',
  title,
  message,
  onClose,
  className = '',
}) => {
  const types = {
    success: {
      bg: 'bg-green-50',
      border: 'border-green-200',
      text: 'text-green-800',
      icon: CheckCircle,
      iconColor: 'text-green-500',
    },
    error: {
      bg: 'bg-red-50',
      border: 'border-red-200',
      text: 'text-red-800',
      icon: XCircle,
      iconColor: 'text-red-500',
    },
    warning: {
      bg: 'bg-yellow-50',
      border: 'border-yellow-200',
      text: 'text-yellow-800',
      icon: AlertCircle,
      iconColor: 'text-yellow-500',
    },
    info: {
      bg: 'bg-blue-50',
      border: 'border-blue-200',
      text: 'text-blue-800',
      icon: Info,
      iconColor: 'text-blue-500',
    },
  };

  const { bg, border, text, icon: Icon, iconColor } = types[type];

  return (
    <div
      className={clsx(
        'flex items-start gap-3 p-4 rounded-lg border',
        bg,
        border,
        className
      )}
    >
      <Icon className={clsx('w-5 h-5 flex-shrink-0', iconColor)} />
      <div className="flex-1">
        {title && <h4 className={clsx('font-medium mb-1', text)}>{title}</h4>}
        {message && <p className={clsx('text-sm', text)}>{message}</p>}
      </div>
      {onClose && (
        <button
          onClick={onClose}
          className={clsx('text-gray-400 hover:text-gray-600')}
        >
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
};

export default Alert;