// ========== src/components/common/Input.jsx ==========
/**
 * Componente Input con validación y mensajes de error
 */

import { forwardRef } from 'react';
import clsx from 'clsx';

const Input = forwardRef(
  (
    {
      label,
      error,
      helperText,
      icon: Icon,
      fullWidth = true,
      className = '',
      ...props
    },
    ref
  ) => {
    return (
      <div className={clsx(fullWidth && 'w-full')}>
        {/* Label */}
        {label && (
          <label className="block text-sm font-medium text-gray-700 mb-1">
            {label}
            {props.required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}

        {/* Input Container */}
        <div className="relative">
          {Icon && (
            <div className="absolute left-3 top-1/2 transform -translate-y-1/2">
              <Icon className="w-5 h-5 text-gray-400" />
            </div>
          )}

          <input
            ref={ref}
            className={clsx(
              'w-full px-3 py-2 border rounded-lg transition-all',
              'focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent',
              Icon && 'pl-10',
              error
                ? 'border-red-300 focus:ring-red-500'
                : 'border-gray-300',
              className
            )}
            {...props}
          />
        </div>

        {/* Helper Text / Error */}
        {(helperText || error) && (
          <p
            className={clsx(
              'text-sm mt-1',
              error ? 'text-red-500' : 'text-gray-500'
            )}
          >
            {error || helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;