// ========== src/components/common/Dropdown.jsx (CORRECCIÓN DE ESTILOS) ==========

import { useState, useRef, useEffect } from 'react';
import React from 'react'; // Importar React para usar cloneElement
import { ChevronDown } from 'lucide-react';
import clsx from 'clsx';

const Dropdown = ({
  trigger,
  children,
  align = 'left',
  className = '',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Cerrar al hacer click fuera
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Función para manejar el clic del trigger
  const handleTriggerClick = (event) => {
    // Detener la propagación para evitar conflictos con elementos padre (como la Card)
    event.stopPropagation(); 
    setIsOpen(prev => !prev);
  };


  const alignmentClasses = {
    left: 'left-0',
    right: 'right-0',
    center: 'left-1/2 transform -translate-x-1/2',
  };

  // 🚨 CORRECCIÓN CLAVE: Clonamos el elemento trigger
  // Al clonar, inyectamos nuestro onClick para abrir el menú, 
  // manteniendo las props originales (incluyendo el className y estilos).
  const triggerElement = trigger ? (
    React.cloneElement(trigger, {
      // Fusiona el onClick original del trigger (si existiera) con el nuestro
      onClick: (e) => {
        handleTriggerClick(e);
        if (trigger.props.onClick) {
            trigger.props.onClick(e);
        }
      },
    })
  ) : (
    // Trigger por defecto si no se pasa ninguno
    <button 
        onClick={handleTriggerClick}
        className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
    >
        Opciones
        <ChevronDown className="w-4 h-4" />
    </button>
  );


  return (
    <div className="relative inline-block" ref={dropdownRef}>
      {/* Trigger */}
      {triggerElement} 

      {/* Dropdown Menu */}
      {isOpen && (
        <div
          className={clsx(
            // Subí el z-index a 50 por si acaso
            'absolute z-50 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg min-w-[200px]',
            alignmentClasses[align],
            className
          )}
        >
          {children}
        </div>
      )}
    </div>
  );
};

// Dropdown Item (Se mantiene igual)
export const DropdownItem = ({ children, onClick, icon: Icon, danger = false }) => {
  // Aseguramos que el onClick del item también detenga la propagación
  const handleClick = (e) => {
    e.stopPropagation();
    if (onClick) {
      onClick(e);
    }
  };

  return (
    <button
      onClick={handleClick}
      className={clsx(
        'w-full flex items-center gap-2 px-4 py-2 text-sm text-left transition-colors',
        danger
          ? 'text-red-600 hover:bg-red-50'
          : 'text-gray-700 hover:bg-gray-50'
      )}
    >
      {Icon && <Icon className="w-4 h-4" />}
      {children}
    </button>
  );
};

// Dropdown Divider (Se mantiene igual)
export const DropdownDivider = () => {
  return <div className="border-t border-gray-200 my-1" />;
};

export default Dropdown;