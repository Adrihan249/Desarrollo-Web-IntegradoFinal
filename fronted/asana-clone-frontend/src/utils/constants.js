// ========== src/utils/constants.js ==========
/**
 * Constantes de la aplicación
 */

// Estados de tareas
export const TASK_STATUS = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  IN_REVIEW: 'IN_REVIEW',
  BLOCKED: 'BLOCKED',
  DONE: 'DONE',
  CANCELLED: 'CANCELLED',
};

// Prioridades
export const TASK_PRIORITY = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  URGENT: 'URGENT',
};

// Estados de proyectos
export const PROJECT_STATUS = {
  ACTIVE: 'ACTIVE',
  ON_HOLD: 'ON_HOLD',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
};

// Roles de usuario
export const USER_ROLES = {
  ADMIN: 'ROLE_ADMIN',
  PROJECT_MANAGER: 'ROLE_PROJECT_MANAGER',
  MEMBER: 'ROLE_MEMBER',
  VIEWER: 'ROLE_VIEWER',
};

// Tipos de notificación
export const NOTIFICATION_TYPES = {
  TASK_ASSIGNED: 'TASK_ASSIGNED',
  TASK_COMMENTED: 'TASK_COMMENTED',
  MENTIONED: 'MENTIONED_IN_COMMENT',
  PROJECT_ADDED: 'PROJECT_ADDED_AS_MEMBER',
  // ... más tipos
};

// Formatos de exportación
export const EXPORT_FORMATS = {
  CSV: 'CSV',
  EXCEL: 'EXCEL',
  PDF: 'PDF',
  JSON: 'JSON',
};

// Frecuencias de recordatorios
export const REMINDER_FREQUENCY = {
  ONCE: 'ONCE',
  DAILY: 'DAILY',
  WEEKLY: 'WEEKLY',
  MONTHLY: 'MONTHLY',
};