import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { getUser } from './lib/auth'
import { ToastProvider } from './components/ui/Toast'
import Layout from './components/Layout'
import LoginPage from './pages/auth/LoginPage'
import DashboardPage from './pages/dashboard/DashboardPage'
import PatientsPage from './pages/patients/PatientsPage'
import VisitsPage from './pages/visits/VisitsPage'
import NewVisitPage from './pages/visits/NewVisitPage'
import VisitDetailPage from './pages/visits/VisitDetailPage'
import ResultsPage from './pages/results/ResultsPage'
import ResultsListPage from './pages/results/ResultsListPage'
import ReportsListPage from './pages/reports/ReportsListPage'
import ReportPage from './pages/reports/ReportPage'
import CataloguePage from './pages/catalogue/CataloguePage'
import SetupPage from './pages/setup/SetupPage'
import AdminPage from './pages/admin/AdminPage'

function RequireAuth({ children }: { children: React.ReactNode }) {
  return getUser() ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<RequireAuth><Layout /></RequireAuth>}>
            <Route index element={<DashboardPage />} />
            <Route path="patients" element={<PatientsPage />} />
            <Route path="visits" element={<VisitsPage />} />
            <Route path="visits/new" element={<NewVisitPage />} />
            <Route path="visits/:id" element={<VisitDetailPage />} />
            <Route path="results" element={<ResultsListPage />} />
            <Route path="results/:visitId" element={<ResultsPage />} />
            <Route path="reports" element={<ReportsListPage />} />
            <Route path="reports/:visitId" element={<ReportPage />} />
            <Route path="catalogue" element={<CataloguePage />} />
            <Route path="setup" element={<SetupPage />} />
            <Route path="admin" element={<AdminPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ToastProvider>
  )
}
