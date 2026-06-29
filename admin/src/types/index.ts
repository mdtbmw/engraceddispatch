export interface User {
  id: string
  fullName: string
  email: string
  phone: string
  photoUrl: string
  isVerified: boolean
  rating: number
  totalDeliveries: number
  role: 'admin' | 'customer' | 'rider'
  createdAt: string
}

export interface Delivery {
  id: string
  trackingNumber: string
  deliveryType: 'Express' | 'Economy' | 'Batch' | 'Multi-Pickup'
  status: 'PENDING' | 'ASSIGNED' | 'PICKED_UP' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED'
  totalAmount: number
  scheduledAt: string
  createdAt: string
  pickupAddress: string
  deliveryAddress: string
  itemName: string
  itemWeight: number
  otpCode: string
  otpVerified: boolean
  photoProofUri: string | null
  riderId: string | null
  riderName: string
  riderBikeNumber: string
  riderRating: number
  etaMinutes: number
  userId: string
  customerName: string
  customerPhone: string
}

export interface Rider {
  id: string
  name: string
  email: string
  phone: string
  bikeNumber: string
  rating: number
  status: 'active' | 'offline' | 'delivering'
  totalDeliveries: number
  totalDistance: number
  isOnline: boolean
  currentLat?: number
  currentLng?: number
  assignedZone?: string
  joinedAt: string
}

export interface Transaction {
  id: string
  title: string
  description: string
  amount: number
  type: 'CREDIT' | 'DEBIT'
  status: 'PENDING' | 'COMPLETED' | 'FAILED'
  reference: string
  userId: string
  userName: string
  createdAt: string
}

export interface WalletStats {
  balance: number
  currency: string
  totalRevenue: number
  pendingWithdrawals: number
  customerCount: number
}

export interface Zone {
  id: string
  name: string
  basePrice: number
  surgeMultiplier: number
  isActive: boolean
  riderCount: number
  deliveryCount: number
}

export interface PricingRule {
  id: string
  deliveryType: string
  basePrice: number
  surgeAmount: number
  perKgRate: number
  isActive: boolean
}

export interface SystemSettings {
  autoAssignRider: boolean
  maxDeliveryRadiusKm: number
  surgePricingActive: boolean
  surgeMultiplier: number
  serviceFeePercent: number
  minWalletBalance: number
  riderAssignmentTimeoutMinutes: number
  maxActiveDeliveriesPerRider: number
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
  message: string
}

export interface ApiError {
  message: string
  code: number
}
