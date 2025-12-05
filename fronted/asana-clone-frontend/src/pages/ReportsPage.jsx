// ===================================
// PÁGINA DE REPORTES ADMIN (SPRINT 4)
// ===================================

// ========== src/pages/ReportsPage.jsx ==========
/**
 * Página de reportes y métricas (Solo Admin)
 */

import { useQuery } from '@tanstack/react-query';
import reportService from '../services/reportService';
import { TrendingUp, DollarSign, Users, Percent } from 'lucide-react';
import Card from '../components/common/Card';
import { formatCurrency } from '../utils/formatters';

const ReportsPage = () => {
  // Obtener dashboard de métricas
  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['reports-dashboard'],
    queryFn: reportService.getDashboard,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500" />
      </div>
    );
  }

  if (!dashboard) {
    return <div>Error al cargar reportes</div>;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Reportes</h1>
        <p className="text-gray-600 mt-1">
          Dashboard de métricas y análisis del sistema
        </p>
      </div>

      {/* Resumen de Métricas */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-50 rounded-lg">
              <Users className="w-6 h-6 text-blue-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Suscripciones Totales</p>
              <p className="text-2xl font-bold">
                {dashboard.summary.totalSubscriptions}
              </p>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-green-50 rounded-lg">
              <TrendingUp className="w-6 h-6 text-green-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Activas</p>
              <p className="text-2xl font-bold">
                {dashboard.summary.activeSubscriptions}
              </p>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-yellow-50 rounded-lg">
              <DollarSign className="w-6 h-6 text-yellow-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">MRR</p>
              <p className="text-2xl font-bold">
                {formatCurrency(dashboard.revenue.monthlyRecurringRevenue)}
              </p>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-purple-50 rounded-lg">
              <Percent className="w-6 h-6 text-purple-500" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Tasa de Conversión</p>
              <p className="text-2xl font-bold">
                {dashboard.conversions.conversionRate.toFixed(1)}%
              </p>
            </div>
          </div>
        </Card>
      </div>

      {/* Ingresos */}
      <Card title="Ingresos">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <p className="text-sm text-gray-600 mb-1">Ingresos Mensuales</p>
            <p className="text-2xl font-bold text-gray-900">
              {formatCurrency(dashboard.revenue.monthlyRevenue)}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-600 mb-1">Ingresos Anuales</p>
            <p className="text-2xl font-bold text-gray-900">
              {formatCurrency(dashboard.revenue.annualRevenue)}
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-600 mb-1">ARPU</p>
            <p className="text-2xl font-bold text-gray-900">
              {formatCurrency(dashboard.revenue.averageRevenuePerUser)}
            </p>
          </div>
        </div>
      </Card>

      {/* Suscripciones por Plan */}
      <Card title="Suscripciones por Plan">
        <div className="space-y-4">
          {Object.entries(dashboard.subscriptionsByPlan).map(([plan, count]) => (
            <div key={plan} className="flex items-center justify-between">
              <span className="text-gray-700">{plan}</span>
              <div className="flex items-center gap-4">
                <div className="w-48 bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-primary-500 h-2 rounded-full"
                    style={{
                      width: `${
                        (count / dashboard.summary.totalSubscriptions) * 100
                      }%`,
                    }}
                  />
                </div>
                <span className="font-semibold text-gray-900 w-12 text-right">
                  {count}
                </span>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Métricas de Conversión */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card title="Conversión de Trial">
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-gray-600">Trials iniciados</span>
              <span className="font-semibold">{dashboard.conversions.trialStarted}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Convertidos a pago</span>
              <span className="font-semibold">{dashboard.conversions.trialConverted}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Tasa de conversión</span>
              <span className="font-semibold text-green-600">
                {dashboard.conversions.conversionRate.toFixed(1)}%
              </span>
            </div>
          </div>
        </Card>

        <Card title="Retención">
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-gray-600">Tasa de cancelación</span>
              <span className="font-semibold text-red-600">
                {dashboard.conversions.cancellationRate.toFixed(1)}%
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Tasa de retención</span>
              <span className="font-semibold text-green-600">
                {dashboard.conversions.retentionRate.toFixed(1)}%
              </span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default ReportsPage;
