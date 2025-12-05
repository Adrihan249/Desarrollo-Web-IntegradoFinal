// ========== src/pages/TaskDetailPage.jsx ==========
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, useEffect } from 'react';
import taskService from '../services/taskService';
import commentService from '../services/commentService';
import { useAuth } from '../contexts/AuthContext';
import {
    CheckCircle2,
    Circle,
    Calendar,
    User,
    MessageSquare,
    Send,
    Trash2,
    ChevronDown,
    ChevronUp,
    AlertCircle,
} from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import Avatar from '../components/common/Avatar';
import { formatRelativeTime, getTaskStatusBadge, getPriorityBadge } from '../utils/formatters';
import toast from 'react-hot-toast';

const TaskDetailPage = () => {
    const { projectId, taskId } = useParams();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { user } = useAuth();

    const [commentText, setCommentText] = useState('');
    const [showSubtasks, setShowSubtasks] = useState(true);

    // Obtener tarea
    const { data: task, isLoading } = useQuery({
        queryKey: ['task', projectId, taskId],
        queryFn: () => taskService.getTaskById(projectId, taskId),
    });

    // Obtener subtareas
    const { data: subtasks = [] } = useQuery({
        queryKey: ['subtasks', projectId, taskId],
        queryFn: () => taskService.getSubtasks(projectId, taskId),
        enabled: !!projectId && !!taskId,
    });

    // Obtener comentarios
    const { data: comments = [] } = useQuery({
        queryKey: ['comments', taskId],
        queryFn: () => commentService.getTaskComments(taskId),
        enabled: !!taskId,
    });

    // Crear comentario
    const createCommentMutation = useMutation({
        mutationFn: (content) => commentService.createComment(taskId, content),
        onSuccess: () => {
            queryClient.invalidateQueries(['comments', taskId]);
            setCommentText('');
            toast.success('Comentario añadido');
        },
        onError: () => toast.error('Error al crear comentario'),
    });

    // Eliminar comentario
    const deleteCommentMutation = useMutation({
        mutationFn: (commentId) => commentService.deleteComment(taskId, commentId),
        onSuccess: () => {
            queryClient.invalidateQueries(['comments', taskId]);
            toast.success('Comentario eliminado');
        },
    });

    // Toggle subtask completion
    const toggleSubtaskMutation = useMutation({
        mutationFn: ({ subtaskId, newStatus }) =>
            taskService.updateTask(projectId, subtaskId, { status: newStatus }),
        onSuccess: () => {
            queryClient.invalidateQueries(['task', projectId, taskId]);
            queryClient.invalidateQueries(['subtasks', projectId, taskId]);
        },
    });

    // Completar tarea principal
    const completeTaskMutation = useMutation({
        mutationFn: () => taskService.updateTask(projectId, taskId, { status: 'DONE' }),
        onSuccess: () => {
            queryClient.invalidateQueries(['task', projectId, taskId]);
            toast.success('Tarea completada');
        },
    });

    const handleToggleSubtask = (subtask) => {
        const newStatus = subtask.status === 'DONE' ? 'TODO' : 'DONE';
        toggleSubtaskMutation.mutate({ subtaskId: subtask.id, newStatus });
    };

    const handleCreateComment = () => {
        if (commentText.trim()) {
            createCommentMutation.mutate(commentText);
        }
    };

    // Verificar si todas las subtareas están completas
    const allSubtasksCompleted =
        subtasks.length > 0 && subtasks.every((st) => st.status === 'DONE');
    const canCompleteTask = subtasks.length === 0 || allSubtasksCompleted;

    if (isLoading || !task) {
        return (
            <div className="flex items-center justify-center h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500" />
            </div>
        );
    }

    const isOverdue =
        task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE';

    return (
        <div className="max-w-7xl mx-auto space-y-6">
            {/* Header */}
            <div className="flex items-start justify-between">
                <div className="space-y-2">
                    <h1 className="text-3xl font-bold text-gray-900">{task.title}</h1>
                    <div className="flex items-center gap-2">
                        <Badge variant={getTaskStatusBadge(task.status).color}>
                            {getTaskStatusBadge(task.status).text}
                        </Badge>
                        <Badge variant={getPriorityBadge(task.priority).color}>
                            {getPriorityBadge(task.priority).text}
                        </Badge>
                        {isOverdue && (
                            <Badge variant="danger">
                                <AlertCircle className="w-3 h-3 mr-1" />
                                VENCIDA
                            </Badge>
                        )}
                    </div>
                </div>

                <Button
                    onClick={() => completeTaskMutation.mutate()}
                    variant={task.status === 'DONE' ? 'success' : 'primary'}
                    disabled={task.status === 'DONE' || !canCompleteTask}
                    className="flex items-center gap-2"
                >
                    <CheckCircle2 className="w-5 h-5" />
                    {task.status === 'DONE' ? 'Completada' : 'Completar Tarea'}
                </Button>
            </div>

            {!canCompleteTask && (
                <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 flex items-start gap-3">
                    <AlertCircle className="w-5 h-5 text-amber-600 mt-0.5" />
                    <div>
                        <p className="font-medium text-amber-900">
                            Completa todas las subtareas primero
                        </p>
                        <p className="text-sm text-amber-700">
                            {subtasks.filter((st) => st.status === 'DONE').length} de{' '}
                            {subtasks.length} subtareas completadas
                        </p>
                    </div>
                </div>
            )}

            <div className="grid grid-cols-3 gap-6">
                {/* Columna Principal */}
                <div className="col-span-2 space-y-6">
                    {/* Información de la Tarea */}
                    <Card className="p-6 space-y-4">
                        <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                                <p className="text-gray-500 mb-1">Creado por</p>
                                <div className="flex items-center gap-2">
                                    <Avatar name={task.createdBy.fullName} size="sm" />
                                    <span className="font-medium">{task.createdBy.fullName}</span>
                                </div>
                            </div>
                            <div>
                                <p className="text-gray-500 mb-1">Proyecto</p>
                                <p className="font-medium">{task.projectName}</p>
                            </div>
                            <div>
                                <p className="text-gray-500 mb-1">Fecha límite</p>
                                <div className="flex items-center gap-1">
                                    <Calendar className="w-4 h-4" />
                                    <span className={isOverdue ? 'text-red-600 font-medium' : ''}>
                                        {task.dueDate ? formatRelativeTime(task.dueDate) : 'No definida'}
                                    </span>
                                </div>
                            </div>
                            <div>
                                <p className="text-gray-500 mb-1">Progreso</p>
                                <div className="flex items-center gap-2">
                                    <div className="flex-1 bg-gray-200 rounded-full h-2">
                                        <div
                                            className="bg-primary-500 h-2 rounded-full transition-all"
                                            style={{ width: `${task.completionPercentage || 0}%` }}
                                        />
                                    </div>
                                    <span className="text-sm font-medium">
                                        {task.completionPercentage || 0}%
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="pt-4 border-t">
                            <h3 className="font-semibold text-gray-800 mb-2">Descripción</h3>
                            {task.description ? (
                                <p className="text-gray-600 whitespace-pre-wrap">{task.description}</p>
                            ) : (
                                <p className="text-gray-500 italic">No hay descripción</p>
                            )}
                        </div>
                    </Card>

                    {/* Subtareas */}
                    <Card className="p-6 space-y-4">
                        <button
                            onClick={() => setShowSubtasks(!showSubtasks)}
                            className="w-full flex items-center justify-between"
                        >
                            <h2 className="text-xl font-semibold flex items-center gap-2">
                                Subtareas ({subtasks.length})
                                {allSubtasksCompleted && subtasks.length > 0 && (
                                    <CheckCircle2 className="w-5 h-5 text-green-600" />
                                )}
                            </h2>
                            {showSubtasks ? (
                                <ChevronUp className="w-5 h-5" />
                            ) : (
                                <ChevronDown className="w-5 h-5" />
                            )}
                        </button>

                        {showSubtasks && (
                            <div className="space-y-2">
                                {subtasks.length > 0 ? (
                                    subtasks.map((st) => (
                                        <div
                                            key={st.id}
                                            className="flex items-center justify-between p-3 border rounded-lg hover:bg-gray-50 transition-colors"
                                        >
                                            <div className="flex items-center gap-3 flex-1">
                                                <button
                                                    onClick={() => handleToggleSubtask(st)}
                                                    className="flex-shrink-0"
                                                >
                                                    {st.status === 'DONE' ? (
                                                        <CheckCircle2 className="w-5 h-5 text-green-600" />
                                                    ) : (
                                                        <Circle className="w-5 h-5 text-gray-400" />
                                                    )}
                                                </button>
                                                <span
                                                    className={`${
                                                        st.status === 'DONE'
                                                            ? 'line-through text-gray-500'
                                                            : 'text-gray-900'
                                                    }`}
                                                >
                                                    {st.title}
                                                </span>
                                            </div>
                                            {st.assignees && st.assignees.length > 0 && (
                                                <div className="flex -space-x-2">
                                                    {st.assignees.map((assignee) => (
                                                        <Avatar
                                                            key={assignee.id}
                                                            name={assignee.fullName}
                                                            size="sm"
                                                            className="border-2 border-white"
                                                        />
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    ))
                                ) : (
                                    <p className="text-center text-gray-500 py-4">No hay subtareas</p>
                                )}
                            </div>
                        )}
                    </Card>
                </div>

                {/* Columna Lateral */}
                <div className="space-y-6">
                    {/* Asignados */}
                    <Card className="p-4 space-y-3">
                        <h3 className="font-semibold text-gray-800 flex items-center gap-2">
                            <User className="w-4 h-4" />
                            Asignados
                        </h3>
                        <div className="space-y-2">
                            {task.assignees?.length > 0 ? (
                                task.assignees.map((assignee) => (
                                    <div key={assignee.id} className="flex items-center gap-2">
                                        <Avatar name={assignee.fullName} size="sm" />
                                        <div className="flex-1 min-w-0">
                                            <p className="text-sm font-medium truncate">
                                                {assignee.fullName}
                                            </p>
                                            <p className="text-xs text-gray-500 truncate">
                                                {assignee.email}
                                            </p>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p className="text-sm text-gray-500 italic">No hay asignados</p>
                            )}
                        </div>
                    </Card>

                    {/* Comentarios */}
                    <Card className="p-4 space-y-4">
                        <h3 className="font-semibold text-gray-800 flex items-center gap-2">
                            <MessageSquare className="w-4 h-4" />
                            Comentarios ({comments.length})
                        </h3>

                        {/* Lista de comentarios */}
                        <div className="space-y-3 max-h-96 overflow-y-auto">
                            {comments.map((comment) => (
                                <div key={comment.id} className="bg-gray-50 rounded-lg p-3 space-y-2">
                                    <div className="flex items-start justify-between">
                                        <div className="flex items-center gap-2">
                                            <Avatar name={comment.user.fullName} size="xs" />
                                            <div>
                                                <p className="text-sm font-medium">
                                                    {comment.user.fullName}
                                                </p>
                                                <p className="text-xs text-gray-500">
                                                    {formatRelativeTime(comment.createdAt)}
                                                </p>
                                            </div>
                                        </div>
                                        {comment.user.id === user?.id && (
                                            <button
                                                onClick={() => deleteCommentMutation.mutate(comment.id)}
                                                className="text-red-600 hover:text-red-700"
                                            >
                                                <Trash2 className="w-4 h-4" />
                                            </button>
                                        )}
                                    </div>
                                    <p className="text-sm text-gray-700">{comment.content}</p>
                                </div>
                            ))}
                        </div>

                        {/* Formulario de comentario */}
                        <div className="flex gap-2 pt-3 border-t">
                            <input
                                type="text"
                                value={commentText}
                                onChange={(e) => setCommentText(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleCreateComment()}
                                placeholder="Escribe un comentario..."
                                className="flex-1 px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 text-sm"
                            />
                            <Button
                                onClick={handleCreateComment}
                                disabled={!commentText.trim()}
                                loading={createCommentMutation.isPending}
                                size="sm"
                            >
                                <Send className="w-4 h-4" />
                            </Button>
                        </div>
                    </Card>
                </div>
            </div>
        </div>
    );
};

export default TaskDetailPage;