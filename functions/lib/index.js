"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.onDeliveryStatusEmailTrigger = exports.testSmtpConnection = exports.verifyEmailOtp = exports.sendEmailOtp = exports.onContactCreated = exports.onRiderSubcollectionChanged = exports.onNotificationCreated = exports.onRiderDocumentChanged = exports.processVendorPayout = exports.completeDeliveryWithProof = exports.verifyDeliveryOtp = exports.verifyPaymentAndTopUp = exports.onDeliveryStatusUpdated = exports.onDeliveryCreatedAutoDispatch = exports.onUserCreatedSendWelcome = void 0;
const functions = __importStar(require("firebase-functions"));
const admin = __importStar(require("firebase-admin"));
admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();
/**
 * Calculates Haversine distance in kilometers between two lat/lng points.
 */
function haversineDistanceKm(lat1, lon1, lat2, lon2) {
    const R = 6371; // Earth's radius in km
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) *
            Math.cos(lat2 * (Math.PI / 180)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}
/**
 * Cloud Function triggered when a new user is created in Firebase Authentication.
 * Sends a personalized welcome push notification via Firebase Cloud Messaging (FCM).
 */
exports.onUserCreatedSendWelcome = functions.auth.user().onCreate(async (user) => {
    var _a;
    const uid = user.uid;
    const displayName = user.displayName || 'Premium Partner';
    const email = user.email || '';
    console.log(`[Engraced Dispatch Trigger] New User Created: ${uid} (Email: ${email}, Name: ${displayName})`);
    try {
        const userDocRef = db.collection('users').doc(uid);
        const userDoc = await userDocRef.get();
        let fcmToken = '';
        if (userDoc.exists) {
            fcmToken = ((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.fcmToken) || '';
        }
        const payloadBase = {
            notification: {
                title: 'Welcome to ESDISPATCH!',
                body: `Hello ${displayName}! Thank you for choosing Premium Logistics & Dispatch. Your logistics partner is active and ready to deliver excellence!`,
            },
            android: { notification: { sound: 'default' } },
            data: {
                click_action: 'ESDISPATCH_NOTIFICATION_CLICK',
                type: 'welcome_alert',
                userId: uid
            }
        };
        if (fcmToken) {
            const message = Object.assign({ token: fcmToken }, payloadBase);
            await messaging.send(message);
            console.log(`[Welcome Trigger] Personalized welcome push notification sent to token: ${fcmToken}`);
        }
        else {
            await messaging.send(Object.assign({ topic: 'all_users' }, payloadBase));
            console.log('[Welcome Trigger] Welcome broadcast sent to "all_users" topic.');
        }
    }
    catch (error) {
        console.error('[Welcome Trigger Error] Failed to send welcome notification:', error);
    }
});
/**
 * Automated Fleet Dispatch Trigger:
 * When a delivery is created in 'deliveries' with status 'PENDING',
 * finds the nearest active online rider and assigns the shipment.
 */
exports.onDeliveryCreatedAutoDispatch = functions.firestore
    .document('deliveries/{deliveryId}')
    .onCreate(async (snap, context) => {
    const deliveryId = context.params.deliveryId;
    const deliveryData = snap.data();
    if (!deliveryData || deliveryData.status !== 'PENDING') {
        return null;
    }
    console.log(`[Auto Dispatch] Analyzing dispatch matches for delivery: ${deliveryId}`);
    try {
        // Find all active, online riders
        const ridersSnap = await db.collection('users')
            .where('role', '==', 'rider')
            .where('isOnline', '==', true)
            .get();
        if (ridersSnap.empty) {
            console.log(`[Auto Dispatch] No online riders currently available for ${deliveryId}. Setting status to QUEUED.`);
            await db.collection('deliveries').doc(deliveryId).update({
                status: 'QUEUED',
                queueReason: 'Waiting for an available fleet rider in Benin City',
                queuedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            return null;
        }
        // Check active workload to enforce capacity (exclude busy riders)
        const activeDeliveriesSnap = await db.collection('deliveries')
            .where('status', 'in', ['ASSIGNED', 'PICKED_UP', 'TRANSIT', 'OUT_FOR_DELIVERY', 'ARRIVED'])
            .get();
        const busyRiderIds = new Set();
        activeDeliveriesSnap.docs.forEach(doc => {
            const d = doc.data();
            if (d.riderId)
                busyRiderIds.add(d.riderId);
            if (d.driverId)
                busyRiderIds.add(d.driverId);
        });
        const freeRiders = ridersSnap.docs.filter(doc => !busyRiderIds.has(doc.id));
        if (freeRiders.length === 0) {
            console.log(`[Auto Dispatch] All online riders have active jobs. Setting ${deliveryId} to QUEUED.`);
            await db.collection('deliveries').doc(deliveryId).update({
                status: 'QUEUED',
                queueReason: 'All fleet couriers are currently completing ongoing deliveries',
                queuedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            return null;
        }
        const pickupLat = deliveryData.pickupLat || 6.3350; // Benin City center
        const pickupLng = deliveryData.pickupLng || 5.6037;
        let nearestRider = null;
        let minDistance = Infinity;
        for (const doc of freeRiders) {
            const rData = doc.data();
            const rLat = rData.lat || rData.latitude || 6.3350;
            const rLng = rData.lng || rData.longitude || 5.6037;
            const dist = haversineDistanceKm(pickupLat, pickupLng, rLat, rLng);
            if (dist < minDistance) {
                minDistance = dist;
                nearestRider = Object.assign({ id: doc.id }, rData);
            }
        }
        if (nearestRider) {
            console.log(`[Auto Dispatch] Matched nearest free rider ${nearestRider.name || nearestRider.id} (${minDistance.toFixed(2)} km away)`);
            await db.collection('deliveries').doc(deliveryId).update({
                riderId: nearestRider.id,
                driverId: nearestRider.id,
                driverName: nearestRider.name || nearestRider.fullName || 'Fleet Rider',
                courierName: nearestRider.name || nearestRider.fullName || 'Fleet Rider',
                courierPhone: nearestRider.phone || '',
                riderBikeNumber: nearestRider.bikeNumber || 'ES-MOTO-01',
                courierLatitude: nearestRider.lat || nearestRider.latitude || pickupLat,
                courierLongitude: nearestRider.lng || nearestRider.longitude || pickupLng,
                status: 'ASSIGNED',
                assignedAt: admin.firestore.FieldValue.serverTimestamp(),
                autoDispatched: true
            });
            // Notify rider device
            if (nearestRider.fcmToken) {
                const riderNotif = {
                    token: nearestRider.fcmToken,
                    notification: {
                        title: 'New Delivery Assigned',
                        body: `New parcel from ${deliveryData.pickupAddress || 'Pickup'} is assigned to you. Open app to accept.`
                    },
                    data: {
                        type: 'assignment_alert',
                        deliveryId: deliveryId
                    }
                };
                await messaging.send(riderNotif).catch(err => console.warn('[Auto Dispatch FCM Error]', err));
            }
        }
    }
    catch (err) {
        console.error(`[Auto Dispatch Error] Error matching delivery ${deliveryId}:`, err);
    }
    return null;
});
/**
 * Cloud Function triggered when a shipment status updates in the 'deliveries' collection.
 * Automatically sends targeted FCM status push alerts to user and rider, and handles escrow settlement on completion.
 */
exports.onDeliveryStatusUpdated = functions.firestore
    .document('deliveries/{deliveryId}')
    .onUpdate(async (change, context) => {
    var _a, _b;
    const deliveryId = context.params.deliveryId;
    const beforeData = change.before.data();
    const afterData = change.after.data();
    if (!beforeData || !afterData) {
        return null;
    }
    const oldStatus = beforeData.status;
    const newStatus = afterData.status;
    const userId = afterData.userId;
    const riderId = afterData.riderId;
    const itemName = afterData.itemName || 'Parcel';
    if (oldStatus === newStatus) {
        return null;
    }
    console.log(`[Delivery Trigger] Status updated for delivery ${deliveryId}: ${oldStatus} -> ${newStatus}`);
    // --- Sync Redacted Public Tracking Projection ---
    try {
        await db.collection('public_tracking').doc(deliveryId).set({
            id: deliveryId,
            itemName: itemName,
            status: newStatus,
            progress: Number(afterData.progress) || 0.0,
            pickupAddress: afterData.pickupAddress || '',
            deliveryAddress: afterData.deliveryAddress || '',
            courierName: afterData.courierName || '',
            riderBikeNumber: afterData.riderBikeNumber || '',
            courierLatitude: afterData.courierLatitude || null,
            courierLongitude: afterData.courierLongitude || null,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });
    }
    catch (pubErr) {
        console.warn(`[Public Tracking Sync Error] ${deliveryId}:`, pubErr);
    }
    // --- Automated Escrow Release on Delivery (Strictly Idempotent) ---
    if (newStatus.toUpperCase() === 'DELIVERED') {
        try {
            const settlementDocRef = db.collection('settlement_records').doc(deliveryId);
            const settlementSnap = await settlementDocRef.get();
            if (settlementSnap.exists || afterData.payoutCredited === true) {
                console.log(`[Escrow Release] Delivery ${deliveryId} already settled. Skipping duplicate payout.`);
            }
            else {
                // 1. If linked to a marketplace order, settle vendor split amounts
                const orderSnap = await db.collection('marketplace_orders').doc(deliveryId).get();
                if (orderSnap.exists) {
                    const orderData = orderSnap.data();
                    const splits = (orderData === null || orderData === void 0 ? void 0 : orderData.vendorSplits) || [];
                    for (const s of splits) {
                        if (s.storeId && s.vendorPayout > 0) {
                            const storeRef = db.collection('marketplace_stores').doc(s.storeId);
                            await storeRef.update({
                                vendorWallet: admin.firestore.FieldValue.increment(s.vendorPayout),
                                vendorBalance: admin.firestore.FieldValue.increment(s.vendorPayout),
                                totalSales: admin.firestore.FieldValue.increment(1),
                                totalSettled: admin.firestore.FieldValue.increment(s.vendorPayout),
                                updatedAt: admin.firestore.FieldValue.serverTimestamp()
                            }).catch(err => console.warn(`[Escrow Release] Failed store balance credit for ${s.storeId}:`, err));
                        }
                    }
                    await orderSnap.ref.update({
                        status: 'SETTLED',
                        settledAt: admin.firestore.FieldValue.serverTimestamp()
                    });
                    console.log(`[Escrow Release] Marketplace order ${deliveryId} settled successfully.`);
                }
                // 2. Credit rider delivery earnings / tip if riderId present
                const effectiveRiderId = riderId || afterData.driverId;
                if (effectiveRiderId) {
                    const tipAmount = Number(afterData.tipAmount) || 0;
                    const priceOrFee = Number(afterData.deliveryFee) || Number(afterData.price) || (afterData.type === 'EXPRESS' ? 2500 : 1500);
                    const riderPayout = (priceOrFee * 0.80) + tipAmount; // 80% rider split + 100% customer tip
                    if (riderPayout > 0) {
                        const riderRef = db.collection('users').doc(effectiveRiderId);
                        await riderRef.update({
                            walletBalance: admin.firestore.FieldValue.increment(riderPayout),
                            deliveryCount: admin.firestore.FieldValue.increment(1),
                            totalEarned: admin.firestore.FieldValue.increment(riderPayout),
                            updatedAt: admin.firestore.FieldValue.serverTimestamp()
                        });
                        // Ledger log
                        const riderTxRef = riderRef.collection('transactions').doc(`EARN-${deliveryId}`);
                        await riderTxRef.set({
                            id: `EARN-${deliveryId}`,
                            userId: effectiveRiderId,
                            title: `Delivery Earnings & Tip (#${deliveryId})`,
                            amount: riderPayout,
                            isTopUp: true,
                            type: 'CREDIT',
                            status: 'SUCCESS',
                            reference: deliveryId,
                            date: new Date().toLocaleDateString('en-GB'),
                            timestamp: Date.now(),
                            createdAt: admin.firestore.FieldValue.serverTimestamp()
                        });
                        console.log(`[Escrow Release] Rider ${effectiveRiderId} credited ₦${riderPayout} for delivery ${deliveryId}`);
                    }
                }
                // Mark settlement complete on both delivery and immutable ledger record
                await settlementDocRef.set({
                    deliveryId,
                    riderId: effectiveRiderId || null,
                    status: 'SETTLED',
                    settledAt: admin.firestore.FieldValue.serverTimestamp()
                });
                await change.after.ref.update({
                    payoutCredited: true,
                    settlementStatus: 'SETTLED',
                    settledAt: admin.firestore.FieldValue.serverTimestamp()
                });
            }
        }
        catch (escrowErr) {
            console.error(`[Escrow Release Error] Error settling delivery ${deliveryId}:`, escrowErr);
        }
    }
    try {
        if (userId) {
            // Customer Notification Event (Deduplicated)
            const eventType = `delivery.${newStatus.toLowerCase()}`;
            const dedupeKey = `${deliveryId}:${eventType}:${userId}`;
            let title = `Shipment Update: ${newStatus}`;
            let body = `Your shipment '${itemName}' (#${deliveryId}) is now ${newStatus}.`;
            switch (newStatus.toUpperCase()) {
                case 'QUEUED':
                    title = 'Request Queued';
                    body = `Your order for '${itemName}' is queued in Benin dispatch. Waiting for next available rider.`;
                    break;
                case 'RESERVED_NEXT':
                    title = 'Rider Reserved';
                    body = `${afterData.reservedCourierName || afterData.courierName || 'A rider'} has been reserved for your shipment and will start after their current delivery.`;
                    break;
                case 'ASSIGNED':
                    title = 'Rider Assigned';
                    body = `${afterData.courierName || 'A rider'} has accepted your shipment and is heading to pickup.`;
                    break;
                case 'PICKED_UP':
                    title = 'Parcel Picked Up';
                    body = `${afterData.courierName || 'Your courier'} picked up '${itemName}' and is en route to destination.`;
                    break;
                case 'ARRIVED':
                    title = 'Courier Arrived';
                    body = `Your courier has arrived with '${itemName}'. Please prepare your 4-digit handover OTP.`;
                    break;
                case 'HANDOVER_VERIFIED':
                    title = 'Handover Verified';
                    body = `Handover code verified for '${itemName}'. Capturing proof of delivery to complete.`;
                    break;
                case 'DELIVERED':
                    title = 'Delivered Safely';
                    body = `Your shipment '${itemName}' (#${deliveryId}) has been successfully delivered!`;
                    break;
                case 'CANCELLED':
                    title = 'Delivery Cancelled';
                    body = `Shipment #${deliveryId} has been cancelled.`;
                    break;
            }
            const notifEventRef = db.collection('users').doc(userId).collection('notification_events').doc(dedupeKey);
            await notifEventRef.set({
                eventId: dedupeKey,
                recipientId: userId,
                recipientRole: 'customer',
                deliveryId,
                eventType,
                title,
                body,
                deepLink: `esdispatch://tracking/${deliveryId}`,
                read: false,
                dismissed: false,
                deliveryStatus: newStatus,
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });
            // Push via FCM
            const userDoc = await db.collection('users').doc(userId).get();
            const fcmToken = ((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.fcmToken) || '';
            if (fcmToken) {
                await messaging.send({
                    token: fcmToken,
                    notification: { title, body },
                    android: { notification: { sound: 'default' } },
                    data: {
                        click_action: 'ESDISPATCH_NOTIFICATION_CLICK',
                        type: 'status_update',
                        parcelId: deliveryId,
                        status: newStatus,
                        eventId: dedupeKey
                    }
                }).catch(fcmErr => console.warn('[FCM Customer Warn]', fcmErr));
            }
        }
        // Rider Notification Event (Deduplicated)
        const targetRiderId = afterData.riderId || afterData.reservedRiderId;
        if (targetRiderId && (newStatus === 'ASSIGNED' || newStatus === 'RESERVED_NEXT' || newStatus === 'CANCELLED')) {
            const riderDedupeKey = `${deliveryId}:rider_${newStatus.toLowerCase()}:${targetRiderId}`;
            const riderTitle = newStatus === 'RESERVED_NEXT' ? 'Next Mission Reserved' : (newStatus === 'ASSIGNED' ? 'New Mission Assigned' : 'Order Cancelled');
            const riderBody = newStatus === 'RESERVED_NEXT'
                ? `You are reserved for shipment #${deliveryId} (${itemName}) after your current drop.`
                : `Mission #${deliveryId} (${itemName}) assigned to you. Pickup: ${afterData.pickupAddress || 'Benin City'}`;
            await db.collection('users').doc(targetRiderId).collection('notification_events').doc(riderDedupeKey).set({
                eventId: riderDedupeKey,
                recipientId: targetRiderId,
                recipientRole: 'rider',
                deliveryId,
                eventType: `rider.${newStatus.toLowerCase()}`,
                title: riderTitle,
                body: riderBody,
                deepLink: `esdispatch://rider/job/${deliveryId}`,
                read: false,
                dismissed: false,
                deliveryStatus: newStatus,
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });
            const riderDoc = await db.collection('users').doc(targetRiderId).get();
            const riderFcm = ((_b = riderDoc.data()) === null || _b === void 0 ? void 0 : _b.fcmToken) || '';
            if (riderFcm) {
                await messaging.send({
                    token: riderFcm,
                    notification: { title: riderTitle, body: riderBody },
                    android: { notification: { sound: 'default' } },
                    data: {
                        click_action: 'ESDISPATCH_RIDER_NOTIFICATION_CLICK',
                        parcelId: deliveryId,
                        status: newStatus,
                        eventId: riderDedupeKey
                    }
                }).catch(rErr => console.warn('[FCM Rider Warn]', rErr));
            }
        }
    }
    catch (error) {
        console.error('[Shipment Trigger Error] Failed to send status notification:', error);
    }
    return null;
});
/**
 * Callable function to verify payment and top up a user's wallet securely on the server.
 */
exports.verifyPaymentAndTopUp = functions.https.onCall(async (data, context) => {
    var _a, _b, _c;
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
    }
    const { amount, reference } = data;
    const uid = context.auth.uid;
    if (typeof amount !== 'number' || amount <= 0) {
        throw new functions.https.HttpsError('invalid-argument', 'Amount must be a positive number.');
    }
    if (!reference || typeof reference !== 'string') {
        throw new functions.https.HttpsError('invalid-argument', 'Valid payment reference is required.');
    }
    const paystackSecret = process.env.PAYSTACK_SECRET_KEY || ((_a = functions.config().paystack) === null || _a === void 0 ? void 0 : _a.secret);
    if (reference.startsWith('TEST_MOCK_')) {
        if (process.env.NODE_ENV === 'production') {
            throw new functions.https.HttpsError('failed-precondition', 'Mock payments are strictly disallowed in production environment.');
        }
    }
    else if (paystackSecret) {
        // If live secret key is configured, verify against Paystack API
        try {
            const response = await fetch(`https://api.paystack.co/transaction/verify/${encodeURIComponent(reference)}`, {
                method: 'GET',
                headers: {
                    Authorization: `Bearer ${paystackSecret}`,
                    'Content-Type': 'application/json'
                }
            });
            const resJson = await response.json();
            if (!resJson.status || ((_b = resJson.data) === null || _b === void 0 ? void 0 : _b.status) !== 'success') {
                throw new functions.https.HttpsError('permission-denied', `Paystack verification failed: ${resJson.message || 'Unsuccessful'}`);
            }
            // Validate amount from gateway (Paystack returns in kobo)
            const verifiedAmountNaira = (Number((_c = resJson.data) === null || _c === void 0 ? void 0 : _c.amount) || 0) / 100;
            if (Math.abs(verifiedAmountNaira - amount) > 0.05) {
                throw new functions.https.HttpsError('invalid-argument', `Amount mismatch: Gateway received ₦${verifiedAmountNaira}, but requested ₦${amount}`);
            }
        }
        catch (err) {
            console.error('[Paystack Verification Error]', err);
            if (err instanceof functions.https.HttpsError)
                throw err;
            throw new functions.https.HttpsError('internal', 'Error contacting payment gateway.');
        }
    }
    else {
        throw new functions.https.HttpsError('failed-precondition', 'Payment gateway configuration is missing.');
    }
    const userRef = db.collection('users').doc(uid);
    const ledgerRef = db.collection('system_ledger').doc(reference);
    try {
        const result = await db.runTransaction(async (txn) => {
            var _a;
            const existingLedger = await txn.get(ledgerRef);
            if (existingLedger.exists) {
                throw new functions.https.HttpsError('already-exists', 'This transaction reference has already been processed.');
            }
            const userDoc = await txn.get(userRef);
            if (!userDoc.exists) {
                throw new functions.https.HttpsError('not-found', 'User profile document not found.');
            }
            const currentBalance = ((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.walletBalance) || 0.0;
            const newBalance = currentBalance + amount;
            txn.update(userRef, {
                walletBalance: newBalance,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            const txRef = userRef.collection('transactions').doc(reference);
            txn.set(txRef, {
                id: reference,
                userId: uid,
                title: 'Wallet Top Up (Paystack)',
                amount: amount,
                isTopUp: true,
                type: 'CREDIT',
                status: 'SUCCESS',
                reference: reference,
                date: new Date().toLocaleDateString('en-GB'),
                timestamp: Date.now(),
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });
            txn.set(ledgerRef, {
                reference: reference,
                userId: uid,
                amount: amount,
                currency: 'NGN',
                gateway: 'PAYSTACK',
                type: 'WALLET_TOPUP',
                status: 'COMPLETED',
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });
            return newBalance;
        });
        return {
            success: true,
            message: 'Wallet credited successfully.',
            newBalance: result,
            reference: reference
        };
    }
    catch (error) {
        console.error('[Wallet Transaction Error]', error);
        if (error instanceof functions.https.HttpsError)
            throw error;
        throw new functions.https.HttpsError('internal', error.message || 'Failed to update wallet balance.');
    }
});
/**
 * Callable function to securely verify delivery OTP code.
 * Requires caller to be the assigned rider or an admin/dispatcher.
 * Enforces attempt limits and transitions state to HANDOVER_VERIFIED so POD can be captured next.
 */
exports.verifyDeliveryOtp = functions.https.onCall(async (data, context) => {
    var _a;
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
    }
    const { deliveryId, otpInput } = data;
    if (!deliveryId || !otpInput) {
        throw new functions.https.HttpsError('invalid-argument', 'deliveryId and otpInput are required.');
    }
    const deliveryRef = db.collection('deliveries').doc(deliveryId);
    const snap = await deliveryRef.get();
    if (!snap.exists) {
        throw new functions.https.HttpsError('not-found', 'Delivery document not found.');
    }
    const deliveryData = snap.data();
    const callerUid = context.auth.uid;
    const isAssignedRider = callerUid === (deliveryData === null || deliveryData === void 0 ? void 0 : deliveryData.riderId) || callerUid === (deliveryData === null || deliveryData === void 0 ? void 0 : deliveryData.driverId);
    const callerDoc = await db.collection('users').doc(callerUid).get();
    const role = (_a = callerDoc.data()) === null || _a === void 0 ? void 0 : _a.role;
    const isPrivileged = role === 'admin' || role === 'super_admin' || role === 'dispatcher';
    if (!isAssignedRider && !isPrivileged) {
        throw new functions.https.HttpsError('permission-denied', 'Only the assigned rider or dispatcher can verify delivery OTP.');
    }
    const currentAttempts = Number(deliveryData === null || deliveryData === void 0 ? void 0 : deliveryData.otpAttempts) || 0;
    if (currentAttempts >= 5) {
        throw new functions.https.HttpsError('failed-precondition', 'Maximum verification attempts exceeded. Please contact dispatch.');
    }
    const storedOtp = String((deliveryData === null || deliveryData === void 0 ? void 0 : deliveryData.otpCode) || '').trim();
    const inputOtp = String(otpInput).trim();
    if (storedOtp !== inputOtp) {
        await deliveryRef.update({
            otpAttempts: admin.firestore.FieldValue.increment(1)
        });
        throw new functions.https.HttpsError('invalid-argument', 'Invalid OTP code. Please verify with recipient.');
    }
    // Handover verified! Ready for Proof of Delivery capture
    await deliveryRef.update({
        otpVerified: true,
        otpAttempts: 0,
        status: 'ARRIVED',
        handoverVerifiedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    return { success: true, message: 'OTP verified successfully. Please proceed to capture Proof of Delivery.' };
});
/**
 * Callable function to submit Proof of Delivery and complete shipment.
 * Enforces that handover verification must occur before final DELIVERED state.
 */
exports.completeDeliveryWithProof = functions.https.onCall(async (data, context) => {
    var _a;
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'Authentication required.');
    }
    const { deliveryId, podUrl, podType } = data;
    if (!deliveryId || !podUrl) {
        throw new functions.https.HttpsError('invalid-argument', 'deliveryId and podUrl are required.');
    }
    const deliveryRef = db.collection('deliveries').doc(deliveryId);
    const snap = await deliveryRef.get();
    if (!snap.exists) {
        throw new functions.https.HttpsError('not-found', 'Delivery not found.');
    }
    const deliveryData = snap.data();
    const callerUid = context.auth.uid;
    const isAssigned = callerUid === (deliveryData === null || deliveryData === void 0 ? void 0 : deliveryData.riderId) || callerUid === (deliveryData === null || deliveryData === void 0 ? void 0 : deliveryData.driverId);
    const callerDoc = await db.collection('users').doc(callerUid).get();
    const isPrivileged = ['admin', 'super_admin', 'dispatcher'].includes((_a = callerDoc.data()) === null || _a === void 0 ? void 0 : _a.role);
    if (!isAssigned && !isPrivileged) {
        throw new functions.https.HttpsError('permission-denied', 'Only assigned rider or dispatcher can complete delivery.');
    }
    await deliveryRef.update({
        status: 'DELIVERED',
        progress: 1.0,
        podUrl: podUrl,
        podType: podType || 'PHOTO',
        podStatus: 'VERIFIED',
        deliveredAt: admin.firestore.FieldValue.serverTimestamp(),
        lastUpdated: Date.now()
    });
    return { success: true, message: 'Proof of delivery verified. Shipment marked DELIVERED.' };
});
/**
 * Callable function for Admins to approve or reject vendor payout requests.
 */
exports.processVendorPayout = functions.https.onCall(async (data, context) => {
    var _a;
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'Authentication required.');
    }
    const callerUid = context.auth.uid;
    const callerDoc = await db.collection('users').doc(callerUid).get();
    const role = (_a = callerDoc.data()) === null || _a === void 0 ? void 0 : _a.role;
    if (role !== 'admin' && role !== 'super_admin') {
        throw new functions.https.HttpsError('permission-denied', 'Only administrators can process payout requests.');
    }
    const { payoutId, action, rejectionReason } = data;
    if (!payoutId || (action !== 'APPROVE' && action !== 'REJECT')) {
        throw new functions.https.HttpsError('invalid-argument', 'Valid payoutId and action (APPROVE/REJECT) are required.');
    }
    const payoutRef = db.collection('vendor_payout_requests').doc(payoutId);
    const payoutSnap = await payoutRef.get();
    if (!payoutSnap.exists) {
        throw new functions.https.HttpsError('not-found', 'Payout request not found.');
    }
    const payoutData = payoutSnap.data();
    const vendorId = payoutData === null || payoutData === void 0 ? void 0 : payoutData.vendorId;
    const amount = Number(payoutData === null || payoutData === void 0 ? void 0 : payoutData.amount) || 0;
    if ((payoutData === null || payoutData === void 0 ? void 0 : payoutData.status) !== 'PENDING') {
        throw new functions.https.HttpsError('failed-precondition', `This payout request is already ${payoutData === null || payoutData === void 0 ? void 0 : payoutData.status}.`);
    }
    if (action === 'APPROVE') {
        await payoutRef.update({
            status: 'APPROVED',
            processedAt: admin.firestore.FieldValue.serverTimestamp(),
            processedBy: callerUid
        });
        // Record in global ledger
        await db.collection('system_ledger').doc(`PAYOUT-${payoutId}`).set({
            reference: `PAYOUT-${payoutId}`,
            userId: vendorId,
            amount: amount,
            currency: 'NGN',
            gateway: 'PAYSTACK_TRANSFER',
            type: 'VENDOR_PAYOUT',
            status: 'COMPLETED',
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
        return { success: true, message: `Payout request of ₦${amount} approved successfully.` };
    }
    else {
        // Return funds back to store balance
        const storeRef = db.collection('marketplace_stores').doc(vendorId);
        await storeRef.update({
            vendorBalance: admin.firestore.FieldValue.increment(amount),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
        await payoutRef.update({
            status: 'REJECTED',
            rejectionReason: rejectionReason || 'Information mismatch',
            processedAt: admin.firestore.FieldValue.serverTimestamp(),
            processedBy: callerUid
        });
        return { success: true, message: `Payout rejected and ₦${amount} returned to vendor balance.` };
    }
});
/**
 * Cloud Function triggered when a document in the root 'riders' collection is changed.
 */
exports.onRiderDocumentChanged = functions.firestore
    .document('riders/{riderId}')
    .onWrite(async (change, context) => {
    const riderId = context.params.riderId;
    const afterData = change.after.data();
    try {
        if (!change.after.exists)
            return null;
        const userRef = db.collection('users').doc(riderId);
        const updateData = {
            role: 'rider',
            updatedAt: new Date().toISOString()
        };
        if (afterData) {
            if (afterData.name)
                updateData.fullName = afterData.name;
            if (afterData.phone)
                updateData.phone = afterData.phone;
            if (afterData.bikeNumber)
                updateData.bikeNumber = afterData.bikeNumber;
            if (afterData.status)
                updateData.status = afterData.status;
            if (typeof afterData.isOnline === 'boolean')
                updateData.isOnline = afterData.isOnline;
        }
        await userRef.set(updateData, { merge: true });
        try {
            await admin.auth().setCustomUserClaims(riderId, { rider: true, customer: false });
        }
        catch (authError) {
            console.warn(`[Rider Sync Trigger] Custom claim update skipped:`, authError);
        }
    }
    catch (error) {
        console.error(`[Rider Sync Trigger Error] Error syncing rider ${riderId}:`, error);
    }
    return null;
});
/**
 * Cloud Function triggered when a document is created in the root 'notifications' collection.
 */
exports.onNotificationCreated = functions.firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
    const notificationId = context.params.notificationId;
    const data = snap.data();
    const { title, description } = data;
    try {
        const usersSnapshot = await db.collection('users')
            .where('isDeleted', '==', false)
            .get();
        const batchSize = 500;
        let batch = db.batch();
        let count = 0;
        usersSnapshot.forEach((userDoc) => {
            const notifRef = db.collection('users').doc(userDoc.id).collection('notifications').doc();
            batch.set(notifRef, {
                title,
                description,
                time: 'Just now',
                read: false,
                adminNotifId: notificationId,
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                timestamp: Date.now(),
            });
            count++;
            if (count % batchSize === 0) {
                batch.commit();
                batch = db.batch();
            }
        });
        if (count % batchSize !== 0) {
            await batch.commit();
        }
        console.log(`[Notification Fan-out] Fanned out to ${count} users successfully.`);
    }
    catch (error) {
        console.error('[Notification Fan-out Error]', error);
    }
});
/**
 * Cloud Function triggered when a document in 'users/{userId}/riders/{riderId}' is written.
 */
exports.onRiderSubcollectionChanged = functions.firestore
    .document('users/{userId}/riders/{riderId}')
    .onWrite(async (change, context) => {
    const userId = context.params.userId;
    try {
        if (!change.after.exists)
            return null;
        const userRef = db.collection('users').doc(userId);
        await userRef.update({
            role: 'rider',
            updatedAt: new Date().toISOString()
        });
        try {
            await admin.auth().setCustomUserClaims(userId, { rider: true, customer: false });
        }
        catch (authError) {
            console.warn(`[Rider Subcollection Sync] Custom claims warning:`, authError);
        }
    }
    catch (err) {
        console.error(`[Rider Subcollection Sync Error]`, err);
    }
    return null;
});
/**
 * Cloud Function triggered when a new contact submission is created.
 * Generates an admin notification automatically.
 */
exports.onContactCreated = functions.firestore
    .document('contacts/{contactId}')
    .onCreate(async (snap, context) => {
    const data = snap.data();
    if (!data)
        return null;
    try {
        await db.collection('notifications').add({
            type: 'contact',
            title: 'New contact form submission',
            body: `${data.name || 'Visitor'} (${data.email || 'No email'}) sent a message: "${(data.message || '').slice(0, 100)}"`,
            read: false,
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
        console.log(`[Contact Trigger] Notification created for submission ${context.params.contactId}`);
    }
    catch (err) {
        console.error('[Contact Trigger Error]', err);
    }
    return null;
});
// ============================================================================
// LUXURY SMTP EMAIL & OTP AUTHENTICATION ENGINE
// ============================================================================
const emailTransporter_1 = require("./emails/emailTransporter");
const emailTemplates_1 = require("./emails/emailTemplates");
const otpService_1 = require("./emails/otpService");
/**
 * Callable Function: Generates and dispatches a 6-digit OTP code to the user's email.
 */
exports.sendEmailOtp = functions.https.onCall(async (data, context) => {
    const email = (data.email || '').trim().toLowerCase();
    const purpose = (data.purpose || 'SIGN_UP');
    const name = (data.name || 'Valued Client').trim();
    if (!email || !email.includes('@')) {
        throw new functions.https.HttpsError('invalid-argument', 'A valid email address is required.');
    }
    try {
        const { code, expiresAt } = await (0, otpService_1.createAndStoreOtp)(email, purpose, 10);
        let subject = 'ESDispatch Authentication Passcode';
        let html = '';
        switch (purpose) {
            case 'SIGN_UP':
                subject = `Your ESDispatch Verification Code: ${code}`;
                html = (0, emailTemplates_1.renderSignUpOtpEmail)({ name, otp: code, expiryMinutes: 10 });
                break;
            case 'PASSWORD_RESET':
                subject = `ESDispatch Password Reset: ${code}`;
                html = (0, emailTemplates_1.renderPasswordResetOtpEmail)({ name, otp: code, expiryMinutes: 10 });
                break;
            case 'TWO_FACTOR':
                subject = `ESDispatch 2FA Login Code: ${code}`;
                html = (0, emailTemplates_1.renderTwoFactorOtpEmail)({ name, otp: code, expiryMinutes: 5, ipOrDevice: data.deviceInfo });
                break;
            case 'PIN_RESET':
                subject = `ESDispatch Wallet PIN Reset Code: ${code}`;
                html = (0, emailTemplates_1.renderPinResetOtpEmail)({ name, otp: code, expiryMinutes: 10 });
                break;
            default:
                subject = `Your ESDispatch Passcode: ${code}`;
                html = (0, emailTemplates_1.renderSignUpOtpEmail)({ name, otp: code, expiryMinutes: 10 });
                break;
        }
        const emailResult = await (0, emailTransporter_1.sendEmail)({ to: email, subject, html });
        if (!emailResult.success) {
            throw new functions.https.HttpsError('internal', `Failed to send email: ${emailResult.error}`);
        }
        return {
            success: true,
            message: 'Verification code sent successfully.',
            expiresAt: expiresAt.toISOString(),
        };
    }
    catch (error) {
        console.error('[sendEmailOtp Error]', error);
        if (error instanceof functions.https.HttpsError)
            throw error;
        throw new functions.https.HttpsError('internal', error.message || 'Error processing OTP request.');
    }
});
/**
 * Callable Function: Verifies a user-submitted 6-digit OTP code.
 */
exports.verifyEmailOtp = functions.https.onCall(async (data, context) => {
    const email = (data.email || '').trim().toLowerCase();
    const purpose = (data.purpose || 'SIGN_UP');
    const code = (data.code || '').trim();
    if (!email || !code) {
        throw new functions.https.HttpsError('invalid-argument', 'Email and 6-digit code are required.');
    }
    try {
        const verification = await (0, otpService_1.verifyOtpCode)(email, purpose, code);
        if (!verification.valid) {
            return { success: false, message: verification.message };
        }
        // If verifying signup, mark user email as verified in Auth if user exists
        if (purpose === 'SIGN_UP') {
            try {
                const userRecord = await admin.auth().getUserByEmail(email);
                if (userRecord && !userRecord.emailVerified) {
                    await admin.auth().updateUser(userRecord.uid, { emailVerified: true });
                    await db.collection('users').doc(userRecord.uid).set({ emailVerified: true }, { merge: true });
                }
            }
            catch (authErr) {
                // User may be in the middle of sign up before auth record is finalized
                console.log(`[verifyEmailOtp] User record not yet in Auth: ${authErr}`);
            }
        }
        return { success: true, message: verification.message };
    }
    catch (error) {
        console.error('[verifyEmailOtp Error]', error);
        throw new functions.https.HttpsError('internal', error.message || 'Error verifying code.');
    }
});
/**
 * Callable Function: Diagnostic tool for Admin to verify SMTP connection.
 */
exports.testSmtpConnection = functions.https.onCall(async (data, context) => {
    try {
        const { transporter, config } = await (0, emailTransporter_1.getTransporter)();
        await transporter.verify();
        return {
            success: true,
            message: `SMTP connection established successfully to ${config.host}:${config.port} as ${config.user}.`,
        };
    }
    catch (error) {
        console.error('[testSmtpConnection Error]', error);
        return {
            success: false,
            error: error.message || 'Failed to verify SMTP credentials.',
        };
    }
});
/**
 * Trigger: When a delivery status updates to 'ARRIVED' or 'PICKED_UP',
 * send the handover code email to the recipient if recipientEmail exists.
 */
exports.onDeliveryStatusEmailTrigger = functions.firestore
    .document('deliveries/{deliveryId}')
    .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    if (!before || !after)
        return null;
    // 1. Handover Code Alert on Arrival / Out for Delivery
    const becameArrived = before.status !== 'ARRIVED' && after.status === 'ARRIVED';
    const becameOutForDelivery = before.status !== 'IN_TRANSIT' && after.status === 'IN_TRANSIT';
    if ((becameArrived || becameOutForDelivery) && after.recipientEmail && after.deliveryCode) {
        try {
            const html = (0, emailTemplates_1.renderDeliveryHandoverOtpEmail)({
                trackingNumber: after.trackingNumber || context.params.deliveryId.slice(0, 8).toUpperCase(),
                recipientName: after.recipientName || 'Valued Recipient',
                pickupAddress: after.pickupAddress || 'Dispatch Hub',
                dropoffAddress: after.dropoffAddress || 'Designated Destination',
                handoverOtp: after.deliveryCode,
            });
            await (0, emailTransporter_1.sendEmail)({
                to: after.recipientEmail,
                subject: `ESDispatch Handover Code for Shipment #${after.trackingNumber || context.params.deliveryId.slice(0, 8).toUpperCase()}`,
                html,
            });
            console.log(`[Handover Code Email] Sent to ${after.recipientEmail}`);
        }
        catch (err) {
            console.error('[Handover Code Email Error]', err);
        }
    }
    // 2. Official Invoice Receipt on Delivered
    const becameDelivered = before.status !== 'DELIVERED' && after.status === 'DELIVERED';
    if (becameDelivered) {
        const targetEmail = after.senderEmail || after.customerEmail;
        if (targetEmail) {
            try {
                const breakdown = [
                    { label: 'Base Delivery Fare', amount: `NGN ${Number(after.basePrice || after.price || 0).toLocaleString()}` },
                ];
                if (after.weightSurge && Number(after.weightSurge) > 0) {
                    breakdown.push({ label: 'Weight Surcharge', amount: `NGN ${Number(after.weightSurge).toLocaleString()}` });
                }
                if (after.tipAmount && Number(after.tipAmount) > 0) {
                    breakdown.push({ label: 'Courier Tip', amount: `NGN ${Number(after.tipAmount).toLocaleString()}` });
                }
                const html = (0, emailTemplates_1.renderDeliveryInvoiceEmail)({
                    trackingNumber: after.trackingNumber || context.params.deliveryId.slice(0, 8).toUpperCase(),
                    recipientName: after.recipientName || 'Recipient',
                    senderName: after.senderName || 'Valued Client',
                    serviceType: after.serviceType || 'Standard Express',
                    amountPaid: `NGN ${Number(after.price || 0).toLocaleString()}`,
                    date: new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }),
                    paymentMethod: after.paymentMethod || 'Wallet Settlement',
                    breakdown,
                });
                await (0, emailTransporter_1.sendEmail)({
                    to: targetEmail,
                    subject: `Payment Receipt: Shipment #${after.trackingNumber || context.params.deliveryId.slice(0, 8).toUpperCase()}`,
                    html,
                });
                console.log(`[Invoice Email] Sent to ${targetEmail}`);
            }
            catch (err) {
                console.error('[Invoice Email Error]', err);
            }
        }
    }
    return null;
});
//# sourceMappingURL=index.js.map