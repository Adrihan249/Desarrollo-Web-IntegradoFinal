// ========== src/hooks/useSubscription.js ==========
/**
 * Hook para gestionar suscripción del usuario
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import subscriptionService from '../services/subscriptionService';
import toast from 'react-hot-toast';

export const useSubscription = () => {
  const queryClient = useQueryClient();

  // Obtener suscripción actual
  const {
    data: subscription,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['subscription'],
    queryFn: subscriptionService.getCurrentSubscription,
  });

  // Obtener uso actual
  const { data: usage } = useQuery({
    queryKey: ['subscription-usage'],
    queryFn: subscriptionService.getUsage,
  });

  // Crear suscripción
  const createSubscriptionMutation = useMutation({
    mutationFn: subscriptionService.createSubscription,
    onSuccess: () => {
      queryClient.invalidateQueries(['subscription']);
      toast.success('Suscripción activada');
    },
  });

  // Cambiar plan
  const changePlanMutation = useMutation({
    mutationFn: subscriptionService.changePlan,
    onSuccess: () => {
      queryClient.invalidateQueries(['subscription']);
      toast.success('Plan cambiado exitosamente');
    },
  });

  // Cancelar suscripción
  const cancelSubscriptionMutation = useMutation({
    mutationFn: subscriptionService.cancelSubscription,
    onSuccess: () => {
      queryClient.invalidateQueries(['subscription']);
      toast.success('Suscripción cancelada');
    },
  });

  return {
    subscription,
    usage,
    isLoading,
    error,
    createSubscription: createSubscriptionMutation.mutate,
    changePlan: changePlanMutation.mutate,
    cancelSubscription: cancelSubscriptionMutation.mutate,
  };
};