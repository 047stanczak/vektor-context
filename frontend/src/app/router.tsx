import { createBrowserRouter, RouterProvider, Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import AppShell from './layout/AppShell'
import LoginPage from '@/features/auth/LoginPage'
import DivergenceNewPage from '@/features/divergences/DivergenceNewPage'
import DivergenceHistoryPage from '@/features/divergences/DivergenceHistoryPage'
import DivergenceReportPage from '@/features/divergences/DivergenceReportPage'
import UploadsPage from '@/features/uploads/UploadsPage'

function ProtectedLayout() {
  const { isAuthenticated, isChecking } = useAuth()

  if (isChecking) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  if (!isAuthenticated) return <Navigate to="/vektor/login" replace />

  return <AppShell><Outlet /></AppShell>
}

const router = createBrowserRouter(
  [
    { path: '/vektor/login', element: <LoginPage /> },
    {
      path: '/vektor',
      element: <ProtectedLayout />,
      children: [
        { index: true, element: <Navigate to="divergences/new" replace /> },
        { path: 'divergences/new',     element: <DivergenceNewPage /> },
        { path: 'divergences/history', element: <DivergenceHistoryPage /> },
        { path: 'divergences/report',  element: <DivergenceReportPage /> },
        { path: 'uploads',             element: <UploadsPage /> },
      ],
    },
    { path: '*', element: <Navigate to="/vektor" replace /> },
  ],
  {
    future: {
      v7_startTransition: true,
      v7_relativeSplatPath: true,
    } as any,
  }
)

export default function Router() {
  return <RouterProvider router={router} />
}
