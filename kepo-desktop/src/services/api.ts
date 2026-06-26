import type {
  User, Event, Shelter, Refugee, Medicine, Distribution, Supplier, Donor,
  DashboardStats, RefugeeMovement, MedicineRequest
} from '../types';

const API_BASE = '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || 'Request failed');
  }
  return res.json();
}

export const api = {
  // Auth
  login: (username: string, password: string) =>
    request<User>('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),

  logout: () => request<void>('/auth/logout'),

  // Dashboard
  getDashboardStats: () => request<DashboardStats>('/dashboard/stats'),
  getDashboardAlerts: () => request<string[]>('/dashboard/alerts'),
  getDashboardDistributions: () => request<Distribution[]>('/dashboard/distributions'),
  getDashboardAiSuggestions: () => request<string[]>('/dashboard/ai-suggestions'),

  // Events
  getEvents: () => request<Event[]>('/events'),
  getEvent: (id: number) => request<Event>(`/events/${id}`),
  saveEvent: (event: Partial<Event>) =>
    request<Event>('/events', { method: 'POST', body: JSON.stringify(event) }),
  deleteEvent: (id: number) => request<void>(`/events/${id}`, { method: 'DELETE' }),

  // Shelters
  getShelters: () => request<Shelter[]>('/shelters'),
  getShelter: (id: number) => request<Shelter>(`/shelters/${id}`),
  saveShelter: (shelter: Partial<Shelter>) =>
    request<Shelter>('/shelters', { method: 'POST', body: JSON.stringify(shelter) }),
  deleteShelter: (id: number) => request<void>(`/shelters/${id}`, { method: 'DELETE' }),

  // Refugees
  getRefugees: () => request<Refugee[]>('/refugees'),
  getRefugee: (id: number) => request<Refugee>(`/refugees/${id}`),
  saveRefugee: (refugee: Partial<Refugee>) =>
    request<Refugee>('/refugees', { method: 'POST', body: JSON.stringify(refugee) }),
  deleteRefugee: (id: number) => request<void>(`/refugees/${id}`, { method: 'DELETE' }),
  checkInRefugee: (id: number, shelterId: number) =>
    request<void>(`/refugees/${id}/checkin`, { method: 'POST', body: JSON.stringify({ shelterId }) }),
  checkOutRefugee: (id: number) =>
    request<void>(`/refugees/${id}/checkout`, { method: 'POST' }),
  transferRefugee: (id: number, targetShelterId: number, notes?: string) =>
    request<void>(`/refugees/${id}/transfer`, { method: 'POST', body: JSON.stringify({ targetShelterId, notes }) }),
  getRefugeeMovementHistory: (id: number) =>
    request<RefugeeMovement[]>(`/refugees/${id}/movements`),

  // Medicines
  getMedicines: () => request<Medicine[]>('/medicines'),
  getMedicine: (id: number) => request<Medicine>(`/medicines/${id}`),
  saveMedicine: (medicine: Partial<Medicine>) =>
    request<Medicine>('/medicines', { method: 'POST', body: JSON.stringify(medicine) }),
  deleteMedicine: (id: number) => request<void>(`/medicines/${id}`, { method: 'DELETE' }),
  adjustStock: (id: number, qty: number, type: string, notes?: string) =>
    request<void>(`/medicines/${id}/stock`, { method: 'POST', body: JSON.stringify({ qty, type, notes }) }),

  // Distributions
  getDistributions: () => request<Distribution[]>('/distributions'),
  getDistribution: (id: number) => request<Distribution>(`/distributions/${id}`),
  saveDistribution: (dist: Partial<Distribution>) =>
    request<Distribution>('/distributions', { method: 'POST', body: JSON.stringify(dist) }),
  deleteDistribution: (id: number) => request<void>(`/distributions/${id}`, { method: 'DELETE' }),
  updateDistributionStatus: (id: number, status: string) =>
    request<void>(`/distributions/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),

  // Suppliers
  getSuppliers: () => request<Supplier[]>('/suppliers'),
  saveSupplier: (supplier: Partial<Supplier>) =>
    request<Supplier>('/suppliers', { method: 'POST', body: JSON.stringify(supplier) }),
  deleteSupplier: (id: number) => request<void>(`/suppliers/${id}`, { method: 'DELETE' }),

  // Donors
  getDonors: () => request<Donor[]>('/donors'),
  saveDonor: (donor: Partial<Donor>) =>
    request<Donor>('/donors', { method: 'POST', body: JSON.stringify(donor) }),
  deleteDonor: (id: number) => request<void>(`/donors/${id}`, { method: 'DELETE' }),

  // Users (Settings)
  getUsers: () => request<User[]>('/users'),
  saveUser: (user: Partial<User> & { password?: string }) =>
    request<User>('/users', { method: 'POST', body: JSON.stringify(user) }),
  deleteUser: (id: number) => request<void>(`/users/${id}`, { method: 'DELETE' }),

  // Reports
  generateReport: (type: string, format: string) =>
    request<{ filePath: string }>(`/reports/${type}?format=${format}`),

  // AI Chat
  aiChat: (message: string) => request<{ response: string }>('/ai/chat', {
    method: 'POST', body: JSON.stringify({ message })
  }),

  // Auto-distribution suggestions (approve/reject)
  getAutoDistSuggestions: () => request<{
    id: number; description: string; status: string;
  }[]>('/auto-distributions/suggestions'),

  approveAutoDist: (data: { shelterId: number; shelterName: string; medicineName: string; quantity: number; itemType?: string }) =>
    request<{ success: boolean; distributionId: number; docNum: string; analysis: string }>('/auto-distributions/approve', {
      method: 'POST', body: JSON.stringify(data)
    }),

  aiAnalyze: (query: string) =>
    request<{ response: string }>('/ai/analyze', { method: 'POST', body: JSON.stringify({ query }) }),

  // Barcode
  barcodeLookup: (code: string) => request<Medicine>(`/barcode/lookup?code=${encodeURIComponent(code)}`),

  // Medicine Requests
  getMedicineRequests: () => request<MedicineRequest[]>('/medicine-requests'),

  createMedicineRequest: (data: { refugeeId: number; shelterId: number; medicineCode: string; medicineName: string; quantity: number; notes?: string }) =>
    request<{ success: boolean; requestId: number }>('/medicine-requests', { method: 'POST', body: JSON.stringify(data) }),

  updateMedicineRequestStatus: (requestId: number, status: string, notes?: string) =>
    request<{ success: boolean }>('/medicine-requests/status', { method: 'POST', body: JSON.stringify({ requestId, status, notes }) }),

  getMedicineRequestCount: () => request<{ pending: number; approved: number; fulfilled: number; total: number }>('/medicine-requests/count'),

  // Predictions
  getPredictions: () => request<{
    summary: string;
    shelterForecasts: string[];
    priorities: string[];
    medPredictions: string[];
    lackingLogistics: string[];
  }>('/predictions'),
};
