// ========== src/components/projects/ProjectCard.jsx (CORREGIDO) ==========

import { Calendar, Users, MoreVertical, Archive, Edit2, Trash2 } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Badge from '../common/Badge';
import { getProjectViewStatus } from '../../utils/projectStatus';

const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha límite';
  try {
    return new Date(dateString).toLocaleDateString();
  } catch (e) {
    return dateString;
  }
};

const ProjectCard = ({ project, currentUserId, onEdit, onDelete, onArchive }) => {
  const navigate = useNavigate();
  const [showActions, setShowActions] = useState(false);
  const actionRef = useRef(null);

  // 🔥 CALCULAR PROGRESO Y ESTADO
  const { progress, viewStatus } = getProjectViewStatus(project, currentUserId);
const isArchived = project.archived;
  const statusColors = {
    ACTIVE: 'success',
    IN_PROGRESS: 'warning', // 🔥 AGREGADO
    ON_HOLD: 'warning',
    DONE: 'primary',
    CANCELLED: 'danger',
    ARCHIVED: 'neutral'
  };

  const statusTexts = {
    ACTIVE: 'Activo',
    IN_PROGRESS: 'En Progreso', // 🔥 AGREGADO
    ON_HOLD: 'En Espera',
    DONE: 'Completado',
    CANCELLED: 'Cancelado',
    ARCHIVED: 'Archivado'
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (showActions && actionRef.current && !actionRef.current.contains(event.target)) {
        setShowActions(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showActions]);

  const handleClick = () => {
    if (!showActions) {
      if (isArchived) {
          return; 
      }
      navigate(`/projects/${project.id}`);
    }
  };

  const badgeVariant = statusColors[viewStatus] || 'default';
  const badgeText = statusTexts[viewStatus] || viewStatus;

  return (
    <div
      className="bg-white rounded-lg border border-gray-200 hover:shadow-lg transition-shadow cursor-pointer group flex flex-col"
      onClick={handleClick}
    >
      {/* Barra de color del proyecto */}
      <div
        className="h-2 rounded-t-lg"
        style={{ backgroundColor: project.color || '#ccc' }}
      />

      <div className="p-6 flex-grow">
        <div className="flex items-start justify-between mb-4">
          {/* Título y Badge */}
          <div className="flex flex-col pr-4">
            <div className="flex items-center gap-2 mb-1">
              <h2 className="text-xl font-bold text-gray-900 line-clamp-1">
                {project.name}
              </h2>
              <Badge variant={badgeVariant} className="flex-shrink-0">
                {badgeText}
              </Badge>
            </div>
            {project.description && (
              <p className="text-gray-600 text-sm line-clamp-2">
                {project.description}
              </p>
            )}
          </div>

          {/* Menú de Acciones */}
          <div className="relative" ref={actionRef}>
            <button
              onMouseDown={(e) => {
                e.stopPropagation();
                setShowActions((prev) => !prev);
              }}
              onClick={(e) => e.stopPropagation()}
              className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg opacity-100 transition-opacity"
            >
              <MoreVertical className="w-5 h-5" />
            </button>
{showActions && (
              <div className="absolute right-0 top-full mt-2 w-48 bg-white rounded-lg shadow-lg border py-1 z-10">
                    
                    {/* 🚨 CORRECCIÓN 2: Mostrar Editar SOLO si NO está archivado */}
                    {!isArchived && (
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onEdit(project);
                                setShowActions(false);
                            }}
                            className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                        >
                            <Edit2 className="w-4 h-4" /> Editar
                        </button>
                    )}
                    
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                      // 🚨 CORRECCIÓN 3: Pasar el ID y el estado actual al manejador
                    onArchive(project.id, isArchived); 
                    setShowActions(false);
                  }}
                  className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                >
                  <Archive className="w-4 h-4" /> 
                    {/* 🚨 CORRECCIÓN 4: Cambiar el texto del botón */}
                    {isArchived ? 'Desarchivar' : 'Archivar'}
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(project.id);
                    setShowActions(false);
                  }}
                  className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-2"
                >
                  <Trash2 className="w-4 h-4" /> Eliminar
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Stats */}
        <div className="flex items-center gap-4 mb-4 text-sm text-gray-600 pt-2">
          {project.deadline && (
            <div className="flex items-center gap-1">
              <Calendar className="w-4 h-4" />
              <span>{formatDate(project.deadline)}</span>
            </div>
          )}
          <div className="flex items-center gap-1">
            <Users className="w-4 h-4" />
            <span>{project.members?.length || 0} miembros</span>
          </div>
        </div>
      </div>

      {/* 🔥 BARRA DE PROGRESO */}
      <div className="px-6 pb-4 pt-2 border-t border-gray-100">
        <div className="flex justify-between items-center text-xs font-medium text-gray-600 mb-1">
          <span>Progreso:</span>
          <span className="font-bold">{Math.round(progress)}%</span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-2.5">
          <div
            className={`h-2.5 rounded-full transition-all duration-300 ${
              progress === 100 ? 'bg-green-500' : 'bg-blue-500'
            }`}
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>
    </div>
  );
};

export default ProjectCard;