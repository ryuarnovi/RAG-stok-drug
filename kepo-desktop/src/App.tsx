import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from './layouts/AppLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Inventory from './pages/Inventory'
import MedicineRequests from './pages/MedicineRequests'
import Distribution from './pages/Distribution'
import SupplierDonor from './pages/SupplierDonor'
import Events from './pages/Events'
import Shelters from './pages/Shelters'
import Refugees from './pages/Refugees'
import AIChat from './pages/AIChat'
import Predictions from './pages/Predictions'
import Reports from './pages/Reports'
import Settings from './pages/Settings'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/app" element={<AppLayout />}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="medicine" element={<Inventory />} />
          <Route path="medrequest" element={<MedicineRequests />} />
          <Route path="distribution" element={<Distribution />} />
          <Route path="supp_donor" element={<SupplierDonor />} />
          <Route path="event" element={<Events />} />
          <Route path="shelter" element={<Shelters />} />
          <Route path="refugee" element={<Refugees />} />
          <Route path="ai" element={<AIChat />} />
          <Route path="prediction" element={<Predictions />} />
          <Route path="report" element={<Reports />} />
          <Route path="settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
