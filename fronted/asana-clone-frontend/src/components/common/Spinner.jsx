// ========== src/components/common/Spinner.jsx ==========
/**
 * Componente Spinner de carga
 */

import { Loader2 } from 'lucide-react';
import clsx from 'clsx';

const Spinner = ({ size = 'md', className = '' }) => {
  const sizes = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <Loader2
      className={clsx('animate-spin text-primary-500', sizes[size], className)}
    />
  );
};

export default Spinner;