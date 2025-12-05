import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import projectService from '../../services/projectService';
import Modal from '../common/Modal';
import Button from '../common/Button';
import Input from '../common/Input';
import toast from 'react-hot-toast';
import { UserPlus } from 'lucide-react';

/**
 * Modal para invitar a un nuevo miembro a un proyecto por email.
 * @param {object} props
 * @param {boolean} props.isOpen - Controla la visibilidad del modal.
 * @param {function} props.onClose - Función para cerrar el modal.
 * @param {string} props.projectId - ID del proyecto.
 * @param {string} props.projectName - Nombre del proyecto.
 */
const InviteMemberModal = ({ isOpen, onClose, projectId, projectName }) => {
    const [email, setEmail] = useState('');
    const [error, setError] = useState('');
    const queryClient = useQueryClient();

    const inviteMemberMutation = useMutation({
        mutationFn: (data) => projectService.inviteMember(projectId, data),
        onSuccess: () => {
            toast.success(`Invitación enviada a ${email} para ${projectName}.`);
            
            // Invalida la lista de miembros del proyecto para forzar la actualización
            queryClient.invalidateQueries(['project', projectId]); 
            
            // Limpia y cierra
            setEmail('');
            setError('');
            onClose();
        },
        onError: (err) => {
            console.error('Error al invitar miembro:', err);
            
            // 🔥 MANEJO ESPECÍFICO DEL LÍMITE DE SUSCRIPCIÓN (403 AccessDeniedException)
            if (err.response?.status === 403) {
                setError(err.response.data?.message || 
                         "Límite de miembros de la suscripción alcanzado. Por favor, actualiza tu plan.");
                toast.error("Límite de miembros alcanzado.");
            } else {
                // Otros errores (email inválido, ya es miembro, etc.)
                setError(err.response?.data?.message || "Error al enviar la invitación.");
                toast.error(err.response?.data?.message || "Error al enviar la invitación.");
            }
        },
    });

    const handleSubmit = (e) => {
        e.preventDefault(); // 🔥 CRÍTICO: Previene recarga de página
        e.stopPropagation(); // 🔥 ADICIONAL: Evita propagación del evento
        
        setError('');
        
        if (!email || !email.trim()) {
            setError('El email es obligatorio.');
            return;
        }

        // 🔥 VALIDACIÓN BÁSICA DE EMAIL
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            setError('Por favor, ingresa un email válido.');
            return;
        }

        // 🔥 LOGGING PARA DEBUGGING
        console.log('Enviando invitación:', { 
            projectId, 
            invitedEmail: email.trim() 
        });

        inviteMemberMutation.mutate({ invitedEmail: email.trim() });
    };

    // 🔥 Manejar cierre del modal solo si no está enviando
    const handleClose = () => {
        if (!inviteMemberMutation.isPending) {
            setEmail('');
            setError('');
            onClose();
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={handleClose}
            title={`Invitar a ${projectName}`}
            icon={UserPlus}
        >
            <p className="text-gray-600 mb-4">
                Introduce el email de la persona que deseas invitar.
            </p>
            <form onSubmit={handleSubmit} className="space-y-4">
                <Input
                    label="Email del Invitado"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="ejemplo@empresa.com"
                    required
                />
                
                {/* Muestra el error de límite de suscripción o cualquier otro error */}
                {error && (
                    <p className="text-sm text-red-600 p-2 bg-red-50 border border-red-200 rounded">
                        {error}
                    </p>
                )}

                <div className="flex justify-end gap-3 pt-2">
                    <Button 
                        variant="secondary" 
                        onClick={handleClose}
                        type="button"
                        disabled={inviteMemberMutation.isPending}
                    >
                        Cancelar
                    </Button>
                    
                    {/* 🔥 CORRECCIÓN CRÍTICA: Cambiar 'loading' por 'isLoading' */}
                    <Button 
                        type="submit" 
                        isLoading={inviteMemberMutation.isPending}
                        disabled={inviteMemberMutation.isPending}
                    >
                        {inviteMemberMutation.isPending ? 'Enviando...' : 'Enviar Invitación'}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};

export default InviteMemberModal;