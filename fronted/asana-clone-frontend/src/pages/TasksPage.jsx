// ========== src/pages/TasksPage.jsx (CON FILTROS) ==========
/**
 * Página de todas mis tareas asignadas con funciones de búsqueda.
 */
import { useNavigate } from 'react-router-dom';
import { useQuery } from "@tanstack/react-query";
import { useState, useMemo } from 'react'; // Importar useState y useMemo
import taskService from "../services/taskService";
import { useAuth } from "../contexts/AuthContext";
import { Filter, Search } from "lucide-react"; // Importar Search icon
import Card from "../components/common/Card";
import Badge from "../components/common/Badge";
import EmptyState from "../components/common/EmptyState";
import Input from "../components/common/Input"; // Necesitamos Input para la búsqueda
import Select from "../components/common/Select"; // Necesitamos Select para filtrar por criterios
import {
  formatRelativeTime,
  getTaskStatusBadge,
  getPriorityBadge,
} from "../utils/formatters";

// Opciones de filtro para el Select
const filterOptions = [
    { value: 'ALL', label: 'Todos los estados' },
    { value: 'TODO', label: 'Pendientes' },
    { value: 'IN_PROGRESS', label: 'En Progreso' },
    { value: 'DONE', label: 'Completadas' },
    { value: 'OVERDUE', label: 'Vencidas' },
];

const TasksPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    // ESTADOS PARA BÚSQUEDA Y FILTRADO
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('ALL');
    const [sortBy, setSortBy] = useState('DUE_DATE'); // Para ordenar por fecha (por defecto)

    // Obtener mis tareas
    const { data: fetchedTasks, isLoading } = useQuery({
        queryKey: ["my-tasks", user?.id],
        queryFn: () => taskService.getMyTasks(),
    });

    // Filtro inicial: Excluir subtareas y manejar nulos
    const baseTasks = fetchedTasks ? fetchedTasks.filter(task => !task.parentTaskId) : [];

    // =========================================================================
    // LÓGICA DE FILTRADO Y ORDENAMIENTO (usando useMemo para optimización)
    // =========================================================================
    const filteredAndSortedTasks = useMemo(() => {
        let currentTasks = baseTasks;

        // 1. FILTRADO POR ESTADO (Status)
        if (filterStatus !== 'ALL') {
            currentTasks = currentTasks.filter(task => {
                if (filterStatus === 'OVERDUE') {
                    // Una tarea está vencida si tiene dueDate y es anterior a hoy
                    return task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE';
                }
                return task.status === filterStatus;
            });
        }

        // 2. FILTRADO POR TÉRMINO DE BÚSQUEDA (Nombre/Descripción/Proyecto)
        if (searchTerm) {
            const lowerCaseSearch = searchTerm.toLowerCase();
            currentTasks = currentTasks.filter(task => 
                task.title.toLowerCase().includes(lowerCaseSearch) ||
                task.description?.toLowerCase().includes(lowerCaseSearch) ||
                task.projectName?.toLowerCase().includes(lowerCaseSearch) 
            );
        }

        // 3. ORDENAMIENTO (Sort)
        currentTasks.sort((a, b) => {
            if (sortBy === 'DUE_DATE') {
                const dateA = a.dueDate ? new Date(a.dueDate).getTime() : Infinity;
                const dateB = b.dueDate ? new Date(b.dueDate).getTime() : Infinity;
                // Ordenar tareas con fecha más próxima (o vencidas) al principio
                return dateA - dateB; 
            }
            if (sortBy === 'PRIORITY') {
                const priorityOrder = { URGENT: 4, HIGH: 3, MEDIUM: 2, LOW: 1, default: 0 };
                return priorityOrder[b.priority] - priorityOrder[a.priority]; // Mayor prioridad primero
            }
            return 0; // Sin ordenamiento si es otra opción
        });

        return currentTasks;
    }, [baseTasks, searchTerm, filterStatus, sortBy]);
    
    // Usamos el resultado del useMemo para renderizar
    const tasks = filteredAndSortedTasks; 

    return (
        <div className="max-w-6xl mx-auto space-y-6">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-gray-900">Mis Tareas</h1>
                <p className="text-gray-600 mt-1">Todas las tareas asignadas a ti</p>
            </div>

            {/* BARRA DE BÚSQUEDA Y FILTRO */}
            <div className="flex gap-4 p-4 bg-white border rounded-lg shadow-sm">
                
                {/* BÚSQUEDA POR NOMBRE/DESCRIPCIÓN */}
                <div className="flex-1">
                    <Input
                        icon={Search}
                        placeholder="Buscar por nombre, descripción o proyecto..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {/* FILTRO POR ESTADO */}
                <div className="w-48">
                    {/* Asumo que tienes un componente Select */}
                    <Select
                        options={filterOptions}
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className="w-full"
                    />
                </div>

                {/* ORDENAR POR */}
                <div className="w-48">
                     <Select
                        options={[
                            { value: 'DUE_DATE', label: 'Ordenar por Fecha Vencimiento' },
                            { value: 'PRIORITY', label: 'Ordenar por Prioridad' },
                        ]}
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="w-full"
                    />
                </div>
            </div>

            {/* Lista de Tareas */}
            {isLoading ? (
                <div className="text-center py-12">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto" />
                </div>
            ) : tasks.length > 0 ? (
                <div className="space-y-4">
                    {tasks.map((task) => {
                        // Determinar si está vencida para un estilo visual
                        const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE';

                        return (
                            <Card
                                key={task.id}
                                hover
                                className={`cursor-pointer ${isOverdue ? 'border-red-400 ring-1 ring-red-200' : 'border-gray-200'}`}
                                onClick={() =>
                                    navigate(`/projects/${task.projectId}/tasks/${task.id}`)
                                }
                            >
                                <div className="flex items-start justify-between">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-2 mb-2">
                                            <h3 className="font-semibold text-gray-900">
                                                {task.title}
                                            </h3>
                                            
                                            {/* Badge de Estado */}
                                            <Badge variant={getTaskStatusBadge(task.status).color}>
                                                {getTaskStatusBadge(task.status).text}
                                            </Badge>

                                            {/* Badge de Prioridad */}
                                            {task.priority && (
                                                <Badge
                                                    variant={getPriorityBadge(task.priority).color}
                                                    size="sm"
                                                >
                                                    {getPriorityBadge(task.priority).text}
                                                </Badge>
                                            )}

                                            {/* Indicador de Vencimiento */}
                                            {isOverdue && (
                                                <Badge variant="danger" size="sm">
                                                    VENCIDA
                                                </Badge>
                                            )}
                                        </div>
                                        
                                        {task.description && (
                                            <p className="text-sm text-gray-600 mb-2 line-clamp-2">
                                                {task.description}
                                            </p>
                                        )}
                                        
                                        <div className="flex items-center gap-4 text-sm text-gray-500">
                                            <span>Proyecto: {task.projectName}</span>
                                            {task.dueDate && (
                                                <span className={isOverdue ? 'text-red-600 font-medium' : ''}>
                                                    Vence: {formatRelativeTime(task.dueDate)}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </Card>
                        );
                    })}
                </div>
            ) : (
                <EmptyState
                    title={searchTerm || filterStatus !== 'ALL' ? "No se encontraron tareas" : "No tienes tareas asignadas"}
                    description={searchTerm || filterStatus !== 'ALL' ? "Intenta ajustar tus filtros o tu término de búsqueda." : "Las tareas que te asignen aparecerán aquí"}
                />
            )}
        </div>
    );
};

export default TasksPage;