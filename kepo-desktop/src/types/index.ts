export interface User {
  userId: number;
  username: string;
  fullName: string;
  role: 'ADMIN' | 'SHELTER_OFFICER' | 'HEALTH_OFFICER' | 'FIELD_COORDINATOR';
}

export interface Event {
  eventId: number;
  name: string;
  location: string;
  status: 'ACTIVE' | 'MONITORING' | 'CLOSED';
  description: string;
  shelterCount: number;
}

export interface Shelter {
  shelterId: number;
  name: string;
  location: string;
  capacity: number;
  currentOccupancy: number;
  penanggungJawab: string;
  status: string;
  eventId?: number;
  avgAvailability?: number;
}

export interface Refugee {
  refugeeId: number;
  name: string;
  nik: string;
  age: number;
  gender: string;
  shelterId?: number;
  shelterName?: string;
  status: 'CHECKED_IN' | 'CHECKED_OUT';
  priorityStatus: string;
  familyCode?: string;
  medicalNotes?: string;
}

export interface Medicine {
  medicineId: number;
  medicineCode: string;
  medicineName: string;
  category: string;
  batchNumber: string;
  unit: string;
  stockQuantity: number;
  minimumStock: number;
  purchasePrice: number;
  sellingPrice: number;
  expiryDate?: string;
  supplierId?: number;
}

export interface MedicineAllocation {
  medicineCode: string;
  medicineName: string;
  quantity: number;
  unit: string;
}

export interface Distribution {
  distributionId: number;
  docNum: string;
  shelterId: number;
  shelterName: string;
  itemType: string;
  quantity: number;
  status: 'DRAFT' | 'APPROVED' | 'SHIPPED' | 'RECEIVED';
  notes?: string;
  allocations?: MedicineAllocation[];
}

export interface Supplier {
  supplierId: number;
  supplierName: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
}

export interface Donor {
  donorId: number;
  donorName: string;
  contact?: string;
  phone?: string;
  email?: string;
  address?: string;
}

export interface ShelterStock {
  shelterStockId: number;
  shelterId: number;
  medicineId: number;
  medicineName: string;
  quantity: number;
  unit: string;
  minimumRequired: number;
}

export interface AuditLog {
  logId: number;
  action: string;
  entityType: string;
  entityId: number;
  userId: number;
  timestamp: string;
  details?: string;
}

export interface RefugeeMovement {
  movementId: number;
  refugeeId: number;
  fromShelterName?: string;
  toShelterName?: string;
  movedBy: string;
  movedAt: string;
  notes?: string;
}

export interface DashboardStats {
  totalShelters: number;
  totalRefugees: number;
  criticalShelters: number;
  activeEvents: number;
  fullShelters: number;
  criticalLogistics: number;
}

export interface MedicineRequest {
  requestId: number;
  refugeeId: number;
  refugeeName: string;
  shelterId: number;
  shelterName: string;
  medicineCode: string;
  medicineName: string;
  quantity: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'FULFILLED';
  notes?: string;
  createdAt: string;
}

export type PageId = 'dashboard' | 'event' | 'shelter' | 'refugee' | 'medicine' | 'distribution' | 'supp_donor' | 'ai' | 'prediction' | 'report' | 'medrequest' | 'settings';
