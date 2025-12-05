// ===================================
// PÁGINA DE PLANES (SPRINT 4)
// ===================================

// ========== src/pages/PlansPage.jsx ==========
/**
 * Página de comparación y selección de planes
 */

import { useQuery } from '@tanstack/react-query';
import planService from '../services/planService';
import subscriptionService from '../services/subscriptionService';
import { Check } from 'lucide-react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import { formatCurrency } from '../utils/formatters';
import toast from 'react-hot-toast';

const PlansPage = () => {
  const { data: plans, isLoading } = useQuery({
    queryKey: ['plans'],
    queryFn: planService.getAllPlans,
  });

  const handleSelectPlan = async (planId) => {
    try {
      await subscriptionService.createSubscription({
        planId,
        billingPeriod: 'MONTHLY',
        paymentMethod: 'CREDIT_CARD',
      });
      toast.success('Suscripción activada');
    } catch (error) {
      toast.error('Error al activar suscripción');
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          Elige el Plan Perfecto para Ti
        </h1>
        <p className="text-xl text-gray-600">
          Planes flexibles que se adaptan a las necesidades de tu equipo
        </p>
      </div>

      {/* Plans Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {plans?.map((plan) => (
          <Card
            key={plan.id}
            className={`relative ${
              plan.name === 'Pro' ? 'border-2 border-primary-500' : ''
            }`}
          >
            {plan.name === 'Pro' && (
              <Badge
                variant="primary"
                className="absolute -top-3 left-1/2 transform -translate-x-1/2"
              >
                MÁS POPULAR
              </Badge>
            )}

            <div className="text-center mb-6">
              <h3 className="text-2xl font-bold text-gray-900 mb-2">
                {plan.name}
              </h3>
              <p className="text-gray-600 text-sm mb-4">{plan.description}</p>
              <div className="mb-4">
                <span className="text-4xl font-bold text-gray-900">
                  {formatCurrency(plan.price)}
                </span>
                <span className="text-gray-600">/mes</span>
              </div>
              {plan.annualPrice && (
                <p className="text-sm text-gray-500">
                  o {formatCurrency(plan.annualPrice)}/año
                </p>
              )}
            </div>

            <Button
              fullWidth
              variant={plan.name === 'Pro' ? 'primary' : 'secondary'}
              onClick={() => handleSelectPlan(plan.id)}
            >
              Elegir {plan.name}
            </Button>

            <div className="mt-6 space-y-3">
              <div className="flex items-center gap-2 text-sm">
                <Check className="w-4 h-4 text-green-500" />
                <span>
                  {plan.maxProjects === -1 ? 'Proyectos ilimitados' : `${plan.maxProjects} proyectos`}
                </span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Check className="w-4 h-4 text-green-500" />
                <span>
                  {plan.maxMembers === -1 ? 'Miembros ilimitados' : `${plan.maxMembers} miembros`}
                </span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Check className="w-4 h-4 text-green-500" />
                <span>
                  {plan.maxStorage === -1 ? 'Almacenamiento ilimitado' : `${plan.maxStorage} MB`}
                </span>
              </div>
              {plan.customFields && (
                <div className="flex items-center gap-2 text-sm">
                  <Check className="w-4 h-4 text-green-500" />
                  <span>Campos personalizados</span>
                </div>
              )}
              {plan.timeline && (
                <div className="flex items-center gap-2 text-sm">
                  <Check className="w-4 h-4 text-green-500" />
                  <span>Timeline</span>
                </div>
              )}
              {plan.advancedReports && (
                <div className="flex items-center gap-2 text-sm">
                  <Check className="w-4 h-4 text-green-500" />
                  <span>Reportes avanzados</span>
                </div>
              )}
              {plan.apiAccess && (
                <div className="flex items-center gap-2 text-sm">
                  <Check className="w-4 h-4 text-green-500" />
                  <span>Acceso API</span>
                </div>
              )}
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default PlansPage;