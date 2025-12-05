// ========== src/components/common/ProgressBar.jsx ==========
/**
 * Componente ProgressBar
 */

import clsx from 'clsx';

const ProgressBar = ({
  value,
  max = 100,
  showLabel = true,
  size = 'md',
  color = 'primary',
  className = '',
}) => {
  const percentage = Math.min(100, Math.max(0, (value / max) * 100));

  const sizes = {
    sm: 'h-1',
    md: 'h-2',
    lg: 'h-3',
  };

  const colors = {
    primary: 'bg-primary-500',
    success: 'bg-green-500',
    warning: 'bg-yellow-500',
    danger: 'bg-red-500',
  };

  return (
    <div className={className}>
      <div className={clsx('w-full bg-gray-200 rounded-full overflow-hidden', sizes[size])}>
        <div
          className={clsx('h-full transition-all duration-300', colors[color])}
          style={{ width: `${percentage}%` }}
        />
      </div>
      {showLabel && (
        <p className="text-sm text-gray-600 mt-1">
          {value} / {max} ({percentage.toFixed(0)}%)
        </p>
      )}
    </div>
  );
};

export default ProgressBar;