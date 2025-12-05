import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import exportService from '../services/exportService';
import projectService from '../services/projectService';
import { Download, FileText, CheckCircle, XCircle, Clock, FileWarning, ExternalLink, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

// ===================================
// UTILIDADES
// ===================================
const formatRelativeTime = (isoString) => {
  const date = new Date(isoString);
  const now = new Date();
  const diffInDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));

  if (diffInDays === 0) return 'hoy';
  if (diffInDays === 1) return 'ayer';
  if (diffInDays < 7) return `hace ${diffInDays} días`;
  return date.toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: 'numeric' });
};

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// ===================================
// COMPONENTES COMUNES
// ===================================
const Button = ({ children, onClick, icon: Icon, disabled, variant = 'primary', className = '' }) => {
  const baseClasses = "font-medium rounded-lg text-sm px-4 py-2 flex items-center justify-center transition duration-150 ease-in-out";
  const variantClasses = {
    primary: "bg-blue-600 text-white hover:bg-blue-700 disabled:bg-blue-400",
    success: "bg-green-600 text-white hover:bg-green-700 disabled:bg-green-400",
    danger: "bg-red-600 text-white hover:bg-red-700 disabled:bg-red-400",
    secondary: "bg-gray-200 text-gray-800 hover:bg-gray-300 disabled:bg-gray-100",
  };

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`${baseClasses} ${variantClasses[variant]} ${className} ${disabled ? 'cursor-not-allowed opacity-75' : ''}`}
    >
      {Icon && <Icon className="w-4 h-4 mr-2" />}
      {children}
    </button>
  );
};

const Card = ({ children, className = '' }) => (
  <div className={`bg-white rounded-xl shadow-lg p-6 border border-gray-100 ${className}`}>
    {children}
  </div>
);

const Badge = ({ children, variant = 'default', size = 'md', className = '' }) => {
  const colorClasses = {
    default: 'bg-gray-100 text-gray-700',
    primary: 'bg-blue-100 text-blue-700',
    success: 'bg-green-100 text-green-700',
    warning: 'bg-yellow-100 text-yellow-700',
    danger: 'bg-red-100 text-red-700',
  };
  const sizeClasses = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-0.5 text-sm',
  };
  return (
    <span className={`inline-flex items-center rounded-full ${colorClasses[variant]} ${sizeClasses[size]} ${className}`}>
      {children}
    </span>
  );
};

const EmptyState = ({ icon: Icon, title, description }) => (
  <div className="text-center py-16 px-4">
    <Icon className="w-12 h-12 text-gray-400 mx-auto" />
    <h3 className="mt-2 text-lg font-medium text-gray-900">{title}</h3>
    <p className="mt-1 text-sm text-gray-500">{description}</p>
  </div>
);

const ProgressBar = ({ value, max = 100, size = 'md', showLabel = true }) => {
  const heightClasses = { sm: 'h-1.5', md: 'h-2' };
  const percentage = Math.round((value / max) * 100);

  return (
    <div className="w-full">
      <div className={`w-full bg-gray-200 rounded-full ${heightClasses[size]}`}>
        <div
          className={`bg-blue-600 rounded-full ${heightClasses[size]}`}
          style={{ width: `${percentage}%` }}
          role="progressbar"
          aria-valuenow={value}
          aria-valuemin="0"
          aria-valuemax={max}
        />
      </div>
      {showLabel && (
        <p className="text-xs text-gray-500 mt-1">{percentage}%</p>
      )}
    </div>
  );
};

// ===================================
// LISTA DE PROYECTOS PARA EXPORTAR
// ===================================
const ProjectExportList = ({ onExportSuccess }) => {
  const { data: projects, isLoading: isProjectsLoading } = useQuery({
    queryKey: ['myProjectsToExport'],
    queryFn: () => projectService.getMyProjects(),
  });

  const queryClient = useQueryClient();
  const exportMutation = useMutation({
    mutationFn: (request) => exportService.requestExport(request),
    onSuccess: (data) => {
      toast.success(`✅ Exportación solicitada (Job #${data.id})`);
      queryClient.invalidateQueries(['exports']); 
      onExportSuccess();
    },
    onError: (error) => {
      const errorMsg = error.response?.data?.message || error.message || 'Error desconocido';
      toast.error(`❌ Error: ${errorMsg}`);
      console.error('Export error:', error);
    }
  });

  const handleRequestExport = (projectId, projectName) => {
    toast.loading(`⏳ Generando PDF para "${projectName}"...`, { id: 'export-loading' });
    
    const request = {
      type: 'PROJECT_FULL',
      format: 'PDF',
      referenceId: projectId,
    };
    
    exportMutation.mutate(request);
  };

  if (isProjectsLoading) {
    return (
      <div className="text-center py-8">
        <div className="animate-spin rounded-full h-10 w-10 border-b-4 border-blue-500 mx-auto" />
        <p className="mt-4 text-gray-600">Cargando proyectos...</p>
      </div>
    );
  }

  if (!projects || projects.length === 0) {
    return (
      <EmptyState 
        icon={ExternalLink} 
        title="No tienes proyectos" 
        description="Crea o únete a un proyecto para poder exportar su información." 
      />
    );
  }

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold border-b pb-2 mb-4 text-gray-700">
        📁 Proyectos Disponibles para Exportación
      </h2>
      {projects.map((project) => (
        <Card key={project.id} className="p-4 flex items-center justify-between shadow-sm hover:shadow-md transition">
          <div className='flex-1 min-w-0'>
            <h3 className="font-medium text-gray-800 truncate">{project.name}</h3>
            <p className="text-sm text-gray-500 truncate">{project.description || 'Sin descripción'}</p>
            <div className='text-xs text-gray-400 mt-1'>
              {project.memberCount || 0} Miembros • {project.taskCount || 0} Tareas
            </div>
          </div>
          <Button 
            icon={FileText}
            onClick={() => handleRequestExport(project.id, project.name)}
            disabled={exportMutation.isLoading}
            variant="primary"
            className="ml-4 flex-shrink-0"
          >
            {exportMutation.isLoading ? 'Procesando...' : 'Exportar PDF'}
          </Button>
        </Card>
      ))}
    </div>
  );
};

// ===================================
// PÁGINA PRINCIPAL
// ===================================
const ExportsPage = () => {
  const { data: exports, isLoading, error } = useQuery({
    queryKey: ['exports'],
    queryFn: exportService.getMyExports,
    refetchInterval: 5000, // Refrescar cada 5 segundos para actualizar el progreso
  });

  const queryClient = useQueryClient();

  const handleDownload = async (exportJob) => {
    try {
      toast.loading('⬇️ Descargando archivo...', { id: 'download' });
      
      const blob = await exportService.downloadExport(exportJob.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = exportJob.fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
      toast.success('✅ Descarga iniciada', { id: 'download' });
      queryClient.invalidateQueries(['exports']); 
    } catch (error) {
      toast.error('❌ Error al descargar archivo', { id: 'download' });
      console.error('Download error:', error);
    }
  };

  const getStatusBadge = (status) => {
    const badges = {
      PENDING: { variant: 'default', icon: Clock, text: 'Pendiente' },
      PROCESSING: { variant: 'warning', icon: Clock, text: 'Procesando' },
      COMPLETED: { variant: 'success', icon: CheckCircle, text: 'Completado' },
      FAILED: { variant: 'danger', icon: XCircle, text: 'Fallido' },
      EXPIRED: { variant: 'default', icon: FileWarning, text: 'Expirado' },
    };
    return badges[status] || badges.PENDING;
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 p-6 bg-gray-50 rounded-lg shadow-inner min-h-screen">
      
      {/* Header */}
      <div>
        <h1 className="text-4xl font-extrabold text-blue-800">📦 Centro de Exportaciones</h1>
        <p className="text-gray-600 mt-2">
          Exporta tus proyectos a PDF o gestiona tu historial de descargas.
        </p>
      </div>

      {/* Sección 1: Solicitud de Exportación de Proyectos */}
      <ProjectExportList onExportSuccess={() => {}} />

      {/* Separador visual */}
      <div className="h-px bg-gray-200 my-8" />

      {/* Sección 2: Historial de Exportaciones */}
      <h2 className="text-2xl font-semibold text-gray-700">📋 Historial de Exportaciones</h2>
      
      {error && (
        <Card className="border-red-200 bg-red-50">
          <div className="flex items-center gap-2 text-red-700">
            <AlertCircle className="w-5 h-5" />
            <p>Error al cargar exportaciones: {error.message}</p>
          </div>
        </Card>
      )}
      
      {isLoading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-blue-500 mx-auto" />
          <p className="mt-4 text-gray-600">Cargando historial...</p>
        </div>
      ) : exports && exports.length > 0 ? (
        <div className="space-y-4">
          {exports.map((exportJob) => {
            const statusInfo = getStatusBadge(exportJob.status);
            const StatusIcon = statusInfo.icon;

            return (
              <Card key={exportJob.id} className="p-4 border-l-4 border-blue-400">
                <div className="flex items-center justify-between">
                  <div className="flex items-start gap-4 flex-1 min-w-0">
                    <div className="p-3 bg-blue-100 rounded-xl">
                      <FileText className="w-6 h-6 text-blue-600" />
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-semibold text-gray-900 truncate max-w-[80%]">
                          {exportJob.fileName || `Exportación ${exportJob.type}`}
                        </h3>
                        <Badge variant={statusInfo.variant} size="sm">
                          <StatusIcon className="w-3 h-3 mr-1" />
                          {statusInfo.text}
                        </Badge>
                      </div>

                      <div className="flex items-center gap-4 text-sm text-gray-600 flex-wrap">
                        <span className='font-mono text-xs'>Job #{exportJob.id}</span>
                        <span>Formato: <span className='font-medium'>{exportJob.format}</span></span>
                        {exportJob.fileSize && (
                          <span>Tamaño: <span className='font-medium'>{formatFileSize(exportJob.fileSize)}</span></span>
                        )}
                      </div>

                      {exportJob.status === 'PROCESSING' && (
                        <div className="mt-3 w-full max-w-lg">
                          <ProgressBar
                            value={exportJob.progress || 0}
                            max={100}
                            size="sm"
                            showLabel={false}
                          />
                          <p className="text-xs text-blue-500 mt-1 font-medium">
                            {exportJob.progress || 0}% Completado
                          </p>
                        </div>
                      )}

                      {exportJob.status === 'COMPLETED' && (
                        <p className="text-xs text-gray-500 mt-2">
                          Descargado {exportJob.downloadCount} veces • Creado {formatRelativeTime(exportJob.createdAt)}
                          {exportJob.isExpired ? (
                            <span className='text-red-500 font-bold ml-1'>• EXPIRADO</span>
                          ) : (
                            <span className='ml-1'>• Expira en {exportJob.daysUntilExpiration} días</span>
                          )}
                        </p>
                      )}

                      {exportJob.status === 'FAILED' && (
                        <p className="text-xs text-red-600 mt-2 italic">
                          ❌ Error: {exportJob.errorMessage || 'Hubo un error desconocido durante la generación.'}
                        </p>
                      )}
                    </div>
                  </div>

                  {/* Acciones */}
                  {exportJob.status === 'COMPLETED' && !exportJob.isExpired && (
                    <Button
                      icon={Download}
                      onClick={() => handleDownload(exportJob)}
                      variant="success"
                      className="flex-shrink-0"
                    >
                      Descargar
                    </Button>
                  )}
                  {(exportJob.status === 'EXPIRED' || exportJob.isExpired) && (
                    <div className='text-sm text-red-500 font-medium p-2 border border-red-300 rounded-lg'>
                      ⏰ Archivo Expirado
                    </div>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      ) : (
        <EmptyState
          icon={Download}
          title="Sin Historial de Exportaciones"
          description="Una vez que solicites una exportación de proyecto, aparecerá aquí."
        />
      )}
    </div>
  );
};

export default ExportsPage;