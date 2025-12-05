// ========== src/utils/projectStatus.js (CORREGIDO) ==========

/**
 * Función central para obtener el estado visual de un proyecto.
 * 
 * @param {object} project - El objeto proyecto completo.
 * @param {string|number} currentUserId - El ID del usuario loggeado.
 * @returns {{ progress: number, viewStatus: string }}
 */
export const getProjectViewStatus = (project, currentUserId) => {
  // 1. REGLA ESPECIAL DE ARCHIVADO
  if (project.archived) {
    const isCreator = project.createdBy?.id === currentUserId;
    return {
      progress: 0,
      viewStatus: isCreator ? 'ARCHIVED' : 'CANCELLED',
    };
  }

  // 2. VERIFICAR SI HAY PROCESOS O TAREAS
  if (!project.processes || project.processes.length === 0) {
    return { progress: 0, viewStatus: project.status || 'ACTIVE' };
  }

  // 3. RECOPILAR TODAS LAS TAREAS DE NIVEL SUPERIOR (sin parentTaskId)
  let allTopLevelTasks = [];
  project.processes.forEach((process) => {
    if (process.tasks && Array.isArray(process.tasks)) {
      const topLevelTasks = process.tasks.filter(task => !task.parentTaskId);
      allTopLevelTasks.push(...topLevelTasks);
    }
  });

  // Si no hay tareas de nivel superior, usar el estado de la BD
  if (allTopLevelTasks.length === 0) {
    return { progress: 0, viewStatus: project.status || 'ACTIVE' };
  }

  // 4. CONTAR TAREAS POR ESTADO
  let todoTasks = 0;
  let inProgressOrReviewTasks = 0;
  let completedTasks = 0;

  allTopLevelTasks.forEach((task) => {
    if (task.status === 'TODO') todoTasks++;
    if (task.status === 'IN_PROGRESS' || task.status === 'IN_REVIEW' || task.status === 'REVIEW') {
      inProgressOrReviewTasks++;
    }
    if (task.status === 'DONE') completedTasks++;
  });

  const totalTasks = allTopLevelTasks.length;

  // 5. CALCULAR PROGRESO
  const progress = Math.floor((completedTasks / totalTasks) * 100);

  // 6. DETERMINAR ESTADO VISUAL
  let derivedStatus;

  if (completedTasks === totalTasks) {
    derivedStatus = 'DONE'; // 100% completado
  } else if (inProgressOrReviewTasks > 0 || completedTasks > 0) {
    derivedStatus = 'IN_PROGRESS'; // 🔥 Parcialmente en progreso
  } else if (todoTasks === totalTasks) {
    derivedStatus = 'ACTIVE'; // Todas las tareas en TODO
  } else {
    derivedStatus = project.status || 'ACTIVE';
  }

  return {
    progress: progress,
    viewStatus: derivedStatus,
  };
};