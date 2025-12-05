import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';

// Layouts
import MainLayout from './components/layout/MainLayout';

// Auth Pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ProtectedRoute from './components/auth/ProtectedRoute';

// Dashboard
import DashboardPage from './pages/DashboardPage';

// Projects (Sprint 1)
import ProjectsPage from './pages/ProjectsPage';
import ProjectDetailPage from './pages/ProjectDetailPage';

// Tasks (Sprint 2)
import TasksPage from './pages/TasksPage';
import TaskDetailPage from './pages/TaskDetailPage';

// Notifications (Sprint 3)
import NotificationsPage from './pages/NotificationsPage';
import SettingsPage from './pages/SettingsPage';
import InvitationsPage from './pages/InvitationsPage';

// Chat - NUEVO
import ChatPage from './pages/ChatPage';

// Subscriptions (Sprint 4)
import SubscriptionsPage from './pages/SubscriptionsPage';
import PlansPage from './pages/PlansPage';
import RemindersPage from './pages/RemindersPage';
import ExportsPage from './pages/ExportsPage';

// Admin (Sprint 4)
import ReportsPage from './pages/ReportsPage';

// 404
import NotFoundPage from './pages/NotFoundPage';

function App() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500" />
      </div>
    );
  }

  return (
    <Routes>
      {/* Rutas Públicas */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Rutas Protegidas */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />

        {/* Projects */}
        <Route path="projects" element={<ProjectsPage />} />
        <Route path="projects/:projectId" element={<ProjectDetailPage />} />

        {/* Tasks */}
        <Route path="tasks" element={<TasksPage />} />
        <Route path="projects/:projectId/tasks" element={<TasksPage />} />
        <Route path="projects/:projectId/tasks/:taskId" element={<TaskDetailPage />} />

        {/* Notifications */}
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="settings" element={<SettingsPage />} />
        <Route path="invitations" element={<InvitationsPage />} />

        {/* Chat - NUEVO */}
        <Route path="chat" element={<ChatPage />} />
<Route path="/settings" element={<SettingsPage />} />
        {/* Subscriptions */}
        <Route path="subscription" element={<SubscriptionsPage />} />
        <Route path="plans" element={<PlansPage />} />
        <Route path="reminders" element={<RemindersPage />} />
        <Route path="exports" element={<ExportsPage />} />

        {/* Admin Routes */}
        <Route
          path="admin/reports"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMIN">
              <ReportsPage />
            </ProtectedRoute>
          }
        />

        {/* 404 */}
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

export default App;