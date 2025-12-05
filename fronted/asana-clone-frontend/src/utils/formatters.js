// ===================================
// UTILIDADES - FORMATTERS
// ===================================

// ========== src/utils/formatters.js ==========
/**
 * Funciones de formateo de datos
 */

import { format, formatDistanceToNow, isToday, isYesterday, differenceInDays } from 'date-fns';
import { es } from 'date-fns/locale';

/**
 * Formatear fecha a formato legible
 */
export const formatDate = (date, formatStr = 'PPP') => {
  if (!date) return '';
  return format(new Date(date), formatStr, { locale: es });
};

/**
 * Formatear fecha relativa (hace X tiempo)
 */
export const formatRelativeTime = (date) => {
  if (!date) return '';
  
  const dateObj = new Date(date);
  
  if (isToday(dateObj)) {
    return `Hoy a las ${format(dateObj, 'HH:mm')}`;
  }
  
  if (isYesterday(dateObj)) {
    return `Ayer a las ${format(dateObj, 'HH:mm')}`;
  }
  
  return formatDistanceToNow(dateObj, { addSuffix: true, locale: es });
};

/**
 * Formatear tamaño de archivo
 */
export const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B';
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const k = 1024;
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${units[i]}`;
};

/**
 * Formatear moneda
 */
export const formatCurrency = (amount, currency = 'USD') => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(amount);
};

/**
 * Calcular días restantes hasta una fecha
 */
export const daysUntil = (date) => {
  if (!date) return null;
  return differenceInDays(new Date(date), new Date());
};

/**
 * Verificar si una fecha está vencida
 */
export const isOverdue = (date) => {
  if (!date) return false;
  return new Date(date) < new Date();
};

/**
 * Obtener badge de estado de tarea
 */
export const getTaskStatusBadge = (status) => {
  const badges = {
    TODO: { text: 'Por Hacer', color: 'gray' },
    IN_PROGRESS: { text: 'En Progreso', color: 'blue' },
    IN_REVIEW: { text: 'En Revisión', color: 'yellow' },
    BLOCKED: { text: 'Bloqueado', color: 'red' },
    DONE: { text: 'Completado', color: 'green' },
    CANCELLED: { text: 'Cancelado', color: 'gray' },
  };
  
  return badges[status] || { text: status, color: 'gray' };
};

/**
 * Obtener badge de prioridad
 */
export const getPriorityBadge = (priority) => {
  const badges = {
    LOW: { text: 'Baja', color: 'gray' },
    MEDIUM: { text: 'Media', color: 'yellow' },
    HIGH: { text: 'Alta', color: 'orange' },
    URGENT: { text: 'Urgente', color: 'red' },
  };
  
  return badges[priority] || { text: priority, color: 'gray' };
};