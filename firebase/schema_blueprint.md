# Firestore Database Schema Blueprint

This document defines the Firestore collections structure used for Engraced Smile Dispatch.

## Collections

### 1. `users`
- **Document ID:** User UID (`userId`)
```json
{
  "fullName": "String",
  "email": "String",
  "phone": "String",
  "photoUrl": "String",
  "role": "customer | rider | admin",
  "isVerified": "Boolean",
  "rating": "Number (Float)",
  "totalDeliveries": "Number (Integer)",
  "totalEarned": "Number (Double)",
  "memberSince": "String (MMM yyyy)"
}
```

### 2. `deliveries`
- **Document ID:** Auto-generated UUID or custom tracking ID (`ESD-XXX-XXXX`)
```json
{
  "trackingNumber": "String",
  "deliveryType": "Express | Economy | Batch | Multi-Pickup",
  "status": "PENDING | ASSIGNED | PICKED_UP | OUT_FOR_DELIVERY | DELIVERED | CANCELLED",
  "totalAmount": "Number (Double)",
  "scheduledAt": "String",
  "pickupAddress": "String",
  "deliveryAddress": "String",
  "itemName": "String",
  "itemWeight": "Number (Double)",
  "otpCode": "String (4-digit)",
  "otpVerified": "Boolean",
  "riderName": "String",
  "riderBikeNumber": "String",
  "riderRating": "Number",
  "etaMinutes": "Number",
  "userId": "String",
  "createdAt": "Timestamp"
}
```

### 3. `wallets`
- **Document ID:** User UID (`userId`)
```json
{
  "balance": "Number (Double)",
  "currency": "NGN",
  "userId": "String",
  "updatedAt": "Timestamp"
}
```

### 4. `transactions`
- **Document ID:** Auto-generated ID
```json
{
  "userId": "String",
  "title": "String",
  "description": "String",
  "amount": "Number (Double)",
  "type": "CREDIT | DEBIT",
  "status": "PENDING | COMPLETED | FAILED",
  "reference": "String",
  "createdAt": "Timestamp"
}
```
