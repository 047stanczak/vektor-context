import { createBrowserRouter, RouterProvider, Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import AppShell from './layout/AppShell'
import LoginPage from '@/features/auth/LoginPage'
import HomePage from '@/features/home/HomePage'
import DivergenceNewPage from '@/features/divergences/DivergenceNewPage'
import DivergenceHistoryPage from '@/features/divergences/DivergenceHistoryPage'
import DivergenceReportPage from '@/features/divergences/DivergenceReportPage'
import UploadsPage from '@/features/uploads/UploadsPage'
import JobsPage from '@/features/jobs/JobsPage'
import OldPendingPage from '@/features/old-pending/OldPendingPage'
import RankingsPage from '@/features/rankings/RankingsPage'
import PendingByBarcodePage from '@/features/pending-by-barcode/PendingByBarcodePage'
import CountingPage from '@/features/counting/CountingPage'
import RequestPendingPage from '@/features/request-pending/RequestPendingPage'
import { PageSpinner } from '@/components/ui/spinner'

function ProtectedLayout() {
  const { isAuthenticated, isChecking } = useAuth()

  if (isChecking) return <PageSpinner />

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
        { index: true, element: <HomePage /> },
        { path: 'divergences/new',     element: <DivergenceNewPage /> },
        { path: 'divergences/history', element: <DivergenceHistoryPage /> },
        { path: 'divergences/report',  element: <DivergenceReportPage /> },
        { path: 'uploads',             element: <UploadsPage /> },
        { path: 'jobs',                element: <JobsPage /> },
        { path: 'old-pending',         element: <OldPendingPage /> },
        { path: 'rankings',            element: <RankingsPage /> },
        { path: 'pending-by-barcode',  element: <PendingByBarcodePage /> },
        { path: 'counting',            element: <CountingPage /> },
        { path: 'request-pending',     element: <RequestPendingPage /> },
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
