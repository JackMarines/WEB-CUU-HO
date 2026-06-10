export interface User {
  id: number;
  name: string;
  email: string;
  phone: string;
  avatarUrl?: string;
  provider: string;
  role: 'user' | 'admin' | 'superadmin';
  isActive: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface DisasterType {
  id: number;
  name: string;
  slug: string;
  icon: string;
  baseUrgencyScore: number;
}

export interface DistressCall {
  id: number;
  userId: number | null;
  userName: string | null;
  callerName: string | null;
  callerPhone: string | null;
  disasterType: DisasterType;
  lat: number;
  lng: number;
  locationName: string;
  description: string;
  status: 'active' | 'in_progress' | 'resolved' | 'dismissed';
  urgencyScore: number;
  personCount: number;
  imageUrl: string | null;
  suggestedSupplies: string[];
  createdAt: string;
  resolvedAt: string | null;
}

export interface RescueCenter {
  id: number;
  name: string;
  type: 'shelter' | 'supply_distribution' | 'rescue_team';
  lat: number;
  lng: number;
  address: string;
  phone: string;
  supplies: Record<string, number>;
  capacity: number;
  createdAt: string;
}

export interface Response {
  id: number;
  distressCallId: number;
  rescueCenterId: number;
  rescueCenterName: string;
  assignedBy: number;
  assignedByName: string;
  status: 'assigned' | 'in_progress' | 'delivered';
  note: string;
  createdAt: string;
  updatedAt: string;
  rating: number | null;
  feedback: string | null;
  feedbackAt: string | null;
}

export interface DashboardStats {
  totalCalls: number;
  activeCalls: number;
  inProgressCalls: number;
  resolvedCalls: number;
  totalCenters: number;
  callsByType: { type: string; count: number }[];
  recentCalls: DistressCall[];
}
