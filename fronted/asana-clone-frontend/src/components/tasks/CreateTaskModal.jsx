// ========== src/components/tasks/CreateTaskModal.jsx ==========
import { useForm, useFieldArray } from 'react-hook-form';
import React, { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import taskService from '../../services/taskService';
import projectService from '../../services/projectService';
import Modal from '../common/Modal';
import Input from '../common/Input';
import Button from '../common/Button';
import toast from 'react-hot-toast';
import { Users, X } from 'lucide-react';

// Componente de selección de miembros
const MemberSelector = ({ members, selectedIds, onChange, label }) => {
    const toggleMember = (memberId) => {
        const newSelection = selectedIds.includes(memberId)
            ? selectedIds.filter(id => id !== memberId)
            : [...selectedIds, memberId];
        onChange(newSelection);
    };

    return (
        <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">{label}</label>
            <div className="border rounded-lg p-3 max-h-40 overflow-y-auto bg-gray-50">
                {members.length === 0 ? (
                    <p className="text-sm text-gray-500 italic">No hay miembros disponibles</p>
                ) : (
                    <div className="space-y-2">
                        {members.map((member) => (
                            <label
                                key={member.id}
                                className="flex items-center space-x-3 p-2 hover:bg-gray-100 rounded cursor-pointer"
                            >
                                <input
                                    type="checkbox"
                                    checked={selectedIds.includes(member.id)}
                                    onChange={() => toggleMember(member.id)}
                                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                                />
                                <div className="flex items-center space-x-2">
                                    <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center">
                                        <span className="text-xs font-medium text-primary-700">
                                            {member.fullName?.charAt(0)}
                                        </span>
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium text-gray-900">
                                            {member.fullName}
                                        </p>
                                        <p className="text-xs text-gray-500">{member.email}</p>
                                    </div>
                                </div>
                            </label>
                        ))}
                    </div>
                )}
            </div>
            {selectedIds.length > 0 && (
                <p className="text-xs text-gray-600">
                    {selectedIds.length} miembro(s) seleccionado(s)
                </p>
            )}
        </div>
    );
};

// Componente de subtarea
const SubtaskInput = ({ index, register, remove, members, watch, setValue }) => {
    const subtaskAssignees = watch(`subtasks.${index}.assigneeIds`) || [];

    return (
        <div className="p-3 border border-gray-200 rounded-lg bg-white space-y-2">
            <div className="flex items-start space-x-2">
                <Input
                    placeholder={`Subtarea #${index + 1}: Título`}
                    {...register(`subtasks.${index}.title`, { required: 'El título es requerido' })}
                    className="flex-1"
                />
                <Button
                    variant="danger"
                    size="sm"
                    type="button"
                    onClick={() => remove(index)}
                >
                    <X className="w-4 h-4" />
                </Button>
            </div>

            {/* Selector de miembros para subtarea */}
            <div className="pl-4">
                <MemberSelector
                    members={members}
                    selectedIds={subtaskAssignees}
                    onChange={(ids) => setValue(`subtasks.${index}.assigneeIds`, ids)}
                    label={`Asignar Subtarea #${index + 1}`}
                />
            </div>
        </div>
    );
};

const CreateTaskModal = ({ isOpen, onClose, projectId, processId }) => {
    const queryClient = useQueryClient();

    // Obtener miembros del proyecto
    const { data: project } = useQuery({
        queryKey: ['project', projectId],
        queryFn: () => projectService.getProjectById(projectId),
        enabled: !!projectId && isOpen,
    });

    const members = project?.members || [];
    const [mainTaskAssignees, setMainTaskAssignees] = React.useState([]);

    const {
        register,
        handleSubmit,
        control,
        formState: { errors },
        reset,
        watch,
        setValue,
    } = useForm({
        defaultValues: {
            subtasks: [],
        },
    });

    const { fields, append, remove } = useFieldArray({
        control,
        name: 'subtasks',
    });

    const createTaskMutation = useMutation({
        mutationFn: (data) => taskService.createTask(projectId, data),
        onSuccess: () => {
            queryClient.invalidateQueries(['tasks', projectId]);
            queryClient.invalidateQueries(['my-tasks']);
            toast.success('Tarea creada exitosamente');
            reset();
            setMainTaskAssignees([]);
            onClose();
        },
        onError: (error) => {
            toast.error(`Error: ${error.response?.data?.message || error.message}`);
        },
    });

    const onSubmit = (data) => {
        // Filtrar subtareas con miembros asignados de la tarea principal
        const assignedMemberIds = mainTaskAssignees;
        const validSubtasks = (data.subtasks || [])
            .filter((st) => st.title && st.title.trim().length > 0)
            .map((st) => ({
                title: st.title,
                assigneeIds: (st.assigneeIds || []).filter((id) =>
                    assignedMemberIds.includes(id)
                ),
                dueDate: st.dueDate || null,
            }));

        createTaskMutation.mutate({
            title: data.title,
            description: data.description,
            priority: data.priority || 'MEDIUM',
            dueDate: data.dueDate || null,
            processId,
            assigneeIds: mainTaskAssignees.length > 0 ? mainTaskAssignees : null,
            subtasks: validSubtasks.length > 0 ? validSubtasks : null,
        });
    };

    // Filtrar miembros disponibles para subtareas (solo los asignados a la tarea principal)
    const availableSubtaskMembers = members.filter((m) =>
        mainTaskAssignees.includes(m.id)
    );

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Nueva Tarea"
            size="lg"
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>
                        Cancelar
                    </Button>
                    <Button
                        onClick={handleSubmit(onSubmit)}
                        loading={createTaskMutation.isPending}
                    >
                        Crear Tarea
                    </Button>
                </>
            }
        >
            <form className="space-y-4">
                {/* Título */}
                <Input
                    label="Título *"
                    placeholder="¿Qué hay que hacer?"
                    error={errors.title?.message}
                    {...register('title', { required: 'El título es requerido' })}
                />

                {/* Descripción */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Descripción
                    </label>
                    <textarea
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                        rows={3}
                        placeholder="Describe la tarea..."
                        {...register('description')}
                    />
                </div>

                {/* Selector de Miembros para Tarea Principal */}
                <MemberSelector
                    members={members}
                    selectedIds={mainTaskAssignees}
                    onChange={setMainTaskAssignees}
                    label="Asignar Tarea Principal"
                />

                <div className="grid grid-cols-2 gap-4">
                    {/* Prioridad */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Prioridad
                        </label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                            {...register('priority')}
                        >
                            <option value="LOW">Baja</option>
                            <option value="MEDIUM">Media</option>
                            <option value="HIGH">Alta</option>
                            <option value="URGENT">Urgente</option>
                        </select>
                    </div>

                    {/* Fecha Límite */}
                    <Input
                        label="Fecha Límite"
                        type="datetime-local"
                        {...register('dueDate')}
                    />
                </div>

                {/* Subtareas */}
                <hr className="my-4" />
                <div className="space-y-3">
                    <div className="flex justify-between items-center">
                        <h3 className="text-lg font-semibold flex items-center gap-2">
                            <Users className="w-5 h-5" />
                            Subtareas
                        </h3>
                        <Button
                            type="button"
                            size="sm"
                            onClick={() => append({ title: '', assigneeIds: [] })}
                            disabled={mainTaskAssignees.length === 0}
                        >
                            + Agregar Subtarea
                        </Button>
                    </div>

                    {mainTaskAssignees.length === 0 && (
                        <p className="text-sm text-amber-600 bg-amber-50 p-2 rounded">
                            ⚠️ Debes asignar la tarea principal primero para poder crear
                            subtareas
                        </p>
                    )}

                    {fields.map((item, index) => (
                        <SubtaskInput
                            key={item.id}
                            index={index}
                            register={register}
                            remove={remove}
                            members={availableSubtaskMembers}
                            watch={watch}
                            setValue={setValue}
                        />
                    ))}
                </div>
            </form>
        </Modal>
    );
};

export default CreateTaskModal;