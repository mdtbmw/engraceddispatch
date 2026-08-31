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
exports.onContactCreated = exports.onRiderSubcollectionChanged = exports.onNotificationCreated = exports.onRiderDocumentChanged = exports.processVendorPayout = exports.verifyDeliveryOtp = exports.verifyPaymentAndTopUp = exports.onDeliveryStatusUpdated = exports.onDeliveryCreatedAutoDispatch = exports.onUserCreatedSendWelcome = void 0;
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
                title: 'Welcome to ESDISPATCH! 👑🚚',
                body: `Hello ${displayName}! Thank you for choosing Premium Logistics & Dispatch. Your logistics partner is active and ready to deliver excellence! 🌟✨`,
            },
            android: { notification: { sound: 'default' } },
            data: {
                click_action: 'FLUTTER_NOTIFICATION_CLICK',
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
 * Autonomous Dispatch Trigger:
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
            console.log(`[Auto Dispatch] No online riders currently available for ${deliveryId}. Delivery queued in unassigned pool.`);
            return null;
        }
        const pickupLat = deliveryData.pickupLat || 6.5244; // Default to Lagos center if coords missing
        const pickupLng = deliveryData.pickupLng || 3.3792;
        let nearestRider = null;
        let minDistance = Infinity;
        for (const doc of ridersSnap.docs) {
            const rData = doc.data();
            const rLat = rData.lat || rData.latitude || 6.5244;
            const rLng = rData.lng || rData.longitude || 3.3792;
            const dist = haversineDistanceKm(pickupLat, pickupLng, rLat, rLng);
            if (dist < minDistance) {
                minDistance = dist;
                nearestRider = Object.assign({ id: doc.id }, rData);
            }
        }
        if (nearestRider) {
            console.log(`[Auto Dispatch] Matched nearest rider ${nearestRider.name || nearestRider.id} (${minDistance.toFixed(2)} km away)`);
            await db.collection('deliveries').doc(deliveryId).update({
                riderId: nearestRider.id,
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
                        title: '⚡ New Delivery Assigned!',
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
    // --- Automated Escrow Release on Delivery ---
    if (newStatus.toUpperCase() === 'DELIVERED') {
        try {
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
            if (riderId) {
                const tipAmount = Number(afterData.tipAmount) || 0;
                const deliveryFee = Number(afterData.deliveryFee) || (afterData.type === 'EXPRESS' ? 2500 : 1500);
                const riderPayout = (deliveryFee * 0.70) + tipAmount; // 70% rider split + 100% customer tip
                if (riderPayout > 0) {
                    const riderRef = db.collection('users').doc(riderId);
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
                        userId: riderId,
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
                    console.log(`[Escrow Release] Rider ${riderId} credited ₦${riderPayout} for delivery ${deliveryId}`);
                }
            }
        }
        catch (escrowErr) {
            console.error(`[Escrow Release Error] Error settling delivery ${deliveryId}:`, escrowErr);
        }
    }
    try {
        if (!userId)
            return null;
        const userDoc = await db.collection('users').doc(userId).get();
        if (!userDoc.exists)
            return null;
        const userData = userDoc.data();
        const fcmToken = (userData === null || userData === void 0 ? void 0 : userData.fcmToken) || '';
        if (fcmToken) {
            const emoji = newStatus.toLowerCase() === 'delivered' ? '✅📦' : '🚚⚡';
            const title = `Shipment Status: ${newStatus} ${emoji}`;
            const body = `Your shipment '${itemName}' (#${deliveryId}) is now ${newStatus}.`;
            const payload = {
                token: fcmToken,
                notification: { title, body },
                android: { notification: { sound: 'default' } },
                data: {
                    click_action: 'FLUTTER_NOTIFICATION_CLICK',
                    type: 'status_update',
                    parcelId: deliveryId,
                    status: newStatus
                }
            };
            await messaging.send(payload);
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
    var _a, _b;
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
 */
exports.verifyDeliveryOtp = functions.https.onCall(async (data, context) => {
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
    const storedOtp = String((deliveryData === null || deliveryData === void 0 ? void 0 : deliveryData.otpCode) || '').trim();
    const inputOtp = String(otpInput).trim();
    if (storedOtp !== inputOtp) {
        throw new functions.https.HttpsError('invalid-argument', 'Invalid OTP code. Please check with the recipient.');
    }
    await deliveryRef.update({
        otpVerified: true,
        status: 'DELIVERED',
        deliveredAt: admin.firestore.FieldValue.serverTimestamp()
    });
    return { success: true, message: 'OTP verified and delivery marked completed.' };
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
//# sourceMappingURL=index.js.map