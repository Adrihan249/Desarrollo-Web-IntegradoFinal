// ===================================
// PÁGINA DE SUSCRIPCIONES (SPRINT 4)
// ===================================

// ========== src/pages/SubscriptionsPage.jsx ==========
/**
 * Página de gestión de suscripción
 * - Estado actual de la suscripción
 * - Uso de recursos
 * - Cambiar plan
 * - Historial de pagos
 */

import { useSubscription } from '../hooks/useSubscription';
import { Link } from 'react-router-dom';
import { CreditCard, Calendar, TrendingUp, AlertCircle } from 'lucide-react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import ProgressBar from '../components/common/ProgressBar';
import Alert from '../components/common/Alert';
import { formatDate, formatCurrency, daysUntil } from '../utils/formatters';

const SubscriptionsPage = () => {
  const { subscription, usage, isLoading } = useSubscription();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500" />
      </div>
    );
  }

  if (!subscription) {
    return (
      <div className="max-w-4xl mx-auto">
        <Card>
          <div className="text-center py-8">
            <CreditCard className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              No tienes una suscripción activa
            </h2>
            <p className="text-gray-600 mb-6">
              Elige un plan para desbloquear todas las funcionalidades
            </p>
            <Link to="/plans">
              <Button>Ver Planes Disponibles</Button>
            </Link>
          </div>
        </Card>
      </div>
    );
  }

  const daysRemaining = daysUntil(subscription.endDate);
  const isExpiringSoon = subscription.isExpiringSoon;

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Mi Suscripción</h1>
          <p className="text-gray-600 mt-1">
            Gestiona tu plan y métodos de pago
          </p>
        </div>
        <Link to="/plans">
          <Button variant="secondary">Cambiar Plan</Button>
        </Link>
      </div>

      {/* Alerta de expiración */}
      {isExpiringSoon && (
        <Alert
          type="warning"
          title="Suscripción próxima a vencer"
          message={`Tu suscripción vence en ${daysRemaining} días. Renueva ahora para continuar disfrutando de todas las funcionalidades.`}
        />
      )}

      {/* Estado de Suscripción */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Plan Actual */}
        <Card className="lg:col-span-2">
          <div className="flex items-start justify-between mb-6">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                Plan {subscription.plan.name}
              </h2>
              <p className="text-gray-600 mt-1">
                {subscription.plan.description}
              </p>
            </div>
            <Badge
              variant={
                subscription.status === 'ACTIVE'
                  ? 'success'
                  : subscription.status === 'TRIAL'
                  ? 'warning'
                  : 'danger'
              }
            >
              {subscription.status}
            </Badge>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-blue-50 rounded-lg">
                <CreditCard className="w-5 h-5 text-blue-500" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Precio</p>
                <p className="font-semibold">
                  {formatCurrency(subscription.amount)} / {subscription.billingPeriod === 'MONTHLY' ? 'mes' : 'año'}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="p-2 bg-green-50 rounded-lg">
                <Calendar className="w-5 h-5 text-green-500" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Próximo pago</p>
                <p className="font-semibold">
                  {formatDate(subscription.nextBillingDate)}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="p-2 bg-purple-50 rounded-lg">
                <TrendingUp className="w-5 h-5 text-purple-500" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Renovaciones</p>
                <p className="font-semibold">{subscription.renewalCount}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="p-2 bg-yellow-50 rounded-lg">
                <AlertCircle className="w-5 h-5 text-yellow-500" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Días restantes</p>
                <p className="font-semibold">{daysRemaining}</p>
              </div>
            </div>
          </div>
        </Card>

        {/* Acciones Rápidas */}
        <Card>
          <h3 className="font-semibold text-gray-900 mb-4">
            Acciones Rápidas
          </h3>
          <div className="space-y-2">
            <Link to="/plans">
              <Button variant="secondary" fullWidth>
                Cambiar Plan
              </Button>
            </Link>
            <Button variant="secondary" fullWidth>
              Métodos de Pago
            </Button>
            <Button variant="secondary" fullWidth>
              Historial de Pagos
            </Button>
            <Button variant="danger" fullWidth>
              Cancelar Suscripción
            </Button>
          </div>
        </Card>
      </div>

      {/* Uso de Recursos */}
      {usage && (
        <Card title="Uso de Recursos">
          <div className="space-y-6">
            {/* Proyectos */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">
                  Proyectos
                </span>
                <span className="text-sm text-gray-600">
                  {usage.projects.used} / {usage.projects.limit === null ? '∞' : usage.projects.limit}
                </span>
              </div>
              <ProgressBar
                value={usage.projects.used}
                max={usage.projects.limit || 100}
                color={
                  usage.projects.isOverLimit
                    ? 'danger'
                    : usage.projects.isNearLimit
                    ? 'warning'
                    : 'primary'
                }
              />
            </div>

            {/* Miembros */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">
                  Miembros
                </span>
                <span className="text-sm text-gray-600">
                  {usage.members.used} / {usage.members.limit === null ? '∞' : usage.members.limit}
                </span>
              </div>
              <ProgressBar
                value={usage.members.used}
                max={usage.members.limit || 100}
                color={
                  usage.members.isOverLimit
                    ? 'danger'
                    : usage.members.isNearLimit
                    ? 'warning'
                    : 'primary'
                }
              />
            </div>

            {/* Almacenamiento */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">
                  Almacenamiento
                </span>
                <span className="text-sm text-gray-600">
                  {usage.storage.used} MB / {usage.storage.limit === null ? '∞' : `${usage.storage.limit} MB`}
                </span>
              </div>
              <ProgressBar
                value={usage.storage.used}
                max={usage.storage.limit || 1000}
                color={
                  usage.storage.isOverLimit
                    ? 'danger'
                    : usage.storage.isNearLimit
                    ? 'warning'
                    : 'primary'
                }
              />
            </div>
          </div>
        </Card>
      )}

      {/* Características del Plan */}
      <Card title="Características de tu Plan">
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div className="flex items-center gap-2">
            {subscription.plan.customFields ? '✅' : '❌'}
            <span className="text-sm text-gray-700">Campos personalizados</span>
          </div>
          <div className="flex items-center gap-2">
            {subscription.plan.timeline ? '✅' : '❌'}
            <span className="text-sm text-gray-700">Timeline</span>
          </div>
          <div className="flex items-center gap-2">
            {subscription.plan.ganttChart ? '✅' : '❌'}
            <span className="text-sm text-gray-700">Diagrama de Gantt</span>
          </div>
          <div className="flex items-center gap-2">
            {subscription.plan.advancedReports ? '✅' : '❌'}
            <span className="text-sm text-gray-700">Reportes avanzados</span>
          </div>
          <div className="flex items-center gap-2">
            {subscription.plan.prioritySupport ? '✅' : '❌'}
            <span className="text-sm text-gray-700">Soporte prioritario</span>
          </div>
          <div className="flex items-center gap-2">
            {subscription.plan.apiAccess ? '✅' : '❌'}
            <span className="text-sm text-gray-700">Acceso API</span>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default SubscriptionsPage;