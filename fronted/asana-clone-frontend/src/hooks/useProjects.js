// ========== src/hooks/useProjects.js (CORREGIDO Y FUNCIONAL) ==========

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import projectService from '../services/projectService';
import toast from 'react-hot-toast';
import { getProjectViewStatus } from '../utils/projectStatus'; // 👈 1. Importar la lógica

export const useProjects = () => {
    const queryClient = useQueryClient();
    // 🚨 REEMPLAZAR con el ID real del usuario loggeado (Ej: const { user } = useAuth();)
    const currentUserId = 1; 

    // Obtener todos los proyectos
    const {
        data: projects,
        isLoading,
        error,
    } = useQuery({
        queryKey: ['projects'],
        queryFn: projectService.getAllProjects,
    });
    
    // Función de ayuda para la mutación de estado
    const updateProjectStatusInDb = ({ projectId, status }) => {
        // Usa el servicio de actualización existente, enviando solo el campo status
        return projectService.updateProject(projectId, { status });
    };

    // MUTACIÓN para ACTUALIZAR el campo 'status' del proyecto
    const updateProjectStatusMutation = useMutation({
        mutationFn: updateProjectStatusInDb,
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries(['projects']);
            queryClient.invalidateQueries(['project', variables.projectId]);
        },
        onError: (err) => {
             console.error("[DB UPDATE FAILED]:", err);
             toast.error('Error al guardar el nuevo estado del proyecto en la DB.');
        },
    });

    // FUNCIÓN PRINCIPAL DE RECALCULO Y ACCIÓN (Llamada desde KanbanBoard)
    const updateProjectStatusBasedOnTasks = async (projectId) => {
        try {
            // 1. Obtener la data más reciente (CRUCIAL para reflejar tareas movidas)
            // Asumiendo que 'projectService' tiene esta función.
            const projectToUpdate = await projectService.fetchProjectById(projectId); 

            if (!projectToUpdate) return;
            
            // 2. Calcular el nuevo estado derivado
            const { viewStatus: newDerivedStatus } = getProjectViewStatus(
                projectToUpdate, 
                currentUserId
            );

            // 3. Mapear el estado derivado al estado de la DB (ACTIVE, ON_HOLD, COMPLETED)
            let dbStatus = projectToUpdate.status; 
            
            if (newDerivedStatus === 'DONE') {
                dbStatus = 'COMPLETED'; // Estado final en la DB
            } else if (newDerivedStatus === 'ON_HOLD') {
                dbStatus = 'ON_HOLD'; 
            } else if (newDerivedStatus === 'ACTIVE') {
                dbStatus = 'ACTIVE'; 
            }
            
            // 4. Actualizar la base de datos si el estado ha cambiado
            if (projectToUpdate.status !== dbStatus) {
                console.log(`[DB UPDATE] Cambiando estado de ${projectToUpdate.name}: ${projectToUpdate.status} -> ${dbStatus}`);
                
                await updateProjectStatusMutation.mutateAsync({ 
                    projectId: projectId, 
                    status: dbStatus 
                });
                
                toast.success(`Estado del proyecto actualizado a: ${dbStatus}`);

            } else {
                console.log(`[Status Check] El estado de ${projectToUpdate.name} sigue siendo: ${dbStatus}.`);
            }

        } catch (error) {
            console.error("[Status Recalc ERROR] Falló la lógica de actualización:", error);
        }
    };

 // ========== Bloque de Creación de Proyecto (CORREGIDO) ==========

    // Crear proyecto
    const createProjectMutation = useMutation({
        mutationFn: projectService.createProject,
        onSuccess: () => {
           // 1. Refresca la lista de proyectos
            queryClient.invalidateQueries({ queryKey: ['projects'] });
            
            // 2. 🔥 AGREGAR: Refresca la lista de notificaciones (para que el elemento aparezca)
            queryClient.invalidateQueries({ queryKey: ['notifications'] });
            
            // 3. 🔥 AGREGAR: Refresca el contador de notificaciones (para que el badge aumente)
            queryClient.invalidateQueries({ queryKey: ['unreadNotificationsCount'] }); 
        },
        // Aquí no se pone toast.success para que el componente lo maneje
        onError: (error) => {
            // Si quieres que el error se propague al componente, puedes relanzarlo
            throw new Error(error.response?.data?.message || 'Error desconocido al crear proyecto');
        }
    });
      
    // Actualizar proyecto
    const updateProjectMutation = useMutation({
        mutationFn: ({ projectId, data }) =>
            projectService.updateProject(projectId, data),
        onSuccess: () => {
            queryClient.invalidateQueries(['projects']);
            toast.success('Proyecto actualizado');
        },
        onError: (err) => {
            toast.error(err.message || 'Error al actualizar proyecto');
        },
    });

    // Archivar proyecto
    const archiveProjectMutation = useMutation({
        mutationFn: projectService.archiveProject,
        onSuccess: () => {
            queryClient.invalidateQueries(['projects']);
            toast.success('Proyecto archivado');
        },
        onError: (err) => {
            toast.error(err.message || 'Error al archivar proyecto');
        },
    });
    
    // ELIMINAR PROYECTO
    const deleteProjectMutation = useMutation({
        mutationFn: projectService.deleteProject,
        onSuccess: () => {
            queryClient.invalidateQueries(['projects']);
            toast.success('Proyecto eliminado permanentemente');
        },
        onError: (err) => {
            toast.error(err.message || 'Error al eliminar proyecto');
        },
    });
    
    // DESARCHIVAR PROYECTO
    const unarchiveProjectMutation = useMutation({
        mutationFn: projectService.unarchiveProject,
        onSuccess: () => {
            queryClient.invalidateQueries(['projects']);
            toast.success('Proyecto desarchivado y activo');
        },
        onError: (err) => {
            toast.error(err.message || 'Error al desarchivar proyecto');
        },
    });
const toggleArchiveProject = (projectId, isCurrentlyArchived) => {
    if (isCurrentlyArchived) {
        // 🔥 USAR MUTATEASYNC para devolver una Promesa a toast.promise
        return unarchiveProjectMutation.mutateAsync(projectId); 
    } else {
        // 🔥 USAR MUTATEASYNC para devolver una Promesa a toast.promise
        return archiveProjectMutation.mutateAsync(projectId);
    }
};
   return {
    projects: projects || [],
    isLoading,
    error,
    toggleArchiveProject: toggleArchiveProject,
    
    // Cambiar a mutateAsync para que devuelva una Promesa a toast.promise
    createProject: createProjectMutation.mutateAsync, // 💡 Recomendable cambiar también para que ProjectsPage pueda usar async/await
    updateProject: updateProjectMutation.mutateAsync, // 💡 Recomendable cambiar también
    
    // 🔥 CAMBIO CRUCIAL: Usar mutateAsync para que toast.promise funcione
    deleteProject: deleteProjectMutation.mutateAsync, 
    
    updateProjectStatusBasedOnTasks,
};
};