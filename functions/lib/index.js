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
exports.onRiderSubcollectionChanged = exports.onNotificationCreated = exports.onRiderDocumentChanged = exports.onDeliveryStatusUpdated = exports.onUserCreatedSendWelcome = void 0;
const functions = __importStar(require("firebase-functions"));
const admin = __importStar(require("firebase-admin"));
admin.initializeApp();
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
        // Retrieve the user's FCM token from Firestore
        const userDocRef = admin.firestore().collection('users').doc(uid);
        const userDoc = await userDocRef.get();
        let fcmToken = '';
        if (userDoc.exists) {
            fcmToken = ((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.fcmToken) || '';
        }
        const payload = {
            notification: {
                title: 'Welcome to ENGRACED DISPATCH! 👑🚚',
                body: `Hello ${displayName}! Thank you for choosing Premium Logistics & Dispatch. Your logistics partner is active and ready to deliver excellence! 🌟✨`,
                sound: 'default'
            },
            data: {
                click_action: 'FLUTTER_NOTIFICATION_CLICK',
                type: 'welcome_alert',
                userId: uid
            }
        };
        if (fcmToken) {
            await admin.messaging().sendToDevice(fcmToken, payload);
            console.log(`[Welcome Trigger] Personalized welcome push notification successfully sent to device token: ${fcmToken}`);
        }
        else {
            // Fallback: Broadcast to general topic
            await admin.messaging().sendToTopic('all_users', payload);
            console.log('[Welcome Trigger] Welcome broadcast successfully sent to "all_users" topic.');
        }
    }
    catch (error) {
        console.error('[Welcome Trigger Error] Failed to send welcome notification:', error);
    }
});
/**
 * Cloud Function triggered when a shipment status updates in the 'shipments' collection.
 * Automatically sends a targeted FCM status push alert to the associated user's device.
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
    const itemName = afterData.itemName || 'Parcel';
    // Trigger only if status has updated and is 'Out for Delivery' or 'Delivered'
    if (oldStatus === newStatus) {
        return null;
    }
    const targetStatuses = ['Out for Delivery', 'Delivered'];
    const isTargetStatus = targetStatuses.some(status => status.toLowerCase() === newStatus.toLowerCase());
    if (!isTargetStatus) {
        return null;
    }
    console.log(`[Delivery Trigger] Status updated for delivery ${deliveryId}: ${oldStatus} -> ${newStatus}`);
    try {
        // Fetch user's profile and notification settings
        const userDoc = await admin.firestore().collection('users').doc(userId).get();
        if (!userDoc.exists) {
            console.log(`[Shipment Trigger] User document ${userId} not found. Skipping.`);
            return null;
        }
        const userData = userDoc.data();
        const fcmToken = (userData === null || userData === void 0 ? void 0 : userData.fcmToken) || '';
        const notificationPreferences = (userData === null || userData === void 0 ? void 0 : userData.notificationPreferences) || {};
        // Check user preferences if they have toggled off alerts for specific stages
        const isBooked = newStatus.toLowerCase() === 'pending_assignment' || newStatus.toLowerCase() === 'booked';
        const isDispatched = newStatus.toLowerCase() === 'out for delivery' || newStatus.toLowerCase() === 'transit';
        const isDelivered = newStatus.toLowerCase() === 'delivered';
        if (isDispatched && notificationPreferences.dispatched === false) {
            console.log(`[Shipment Trigger] User has disabled push alerts for Dispatched/Out for Delivery stage.`);
            return null;
        }
        if (isDelivered && notificationPreferences.delivered === false) {
            console.log(`[Shipment Trigger] User has disabled push alerts for Delivered stage.`);
            return null;
        }
        if (!fcmToken) {
            console.log(`[Shipment Trigger] No FCM token found for user ${userId}. Unable to send push alert.`);
            return null;
        }
        const emoji = newStatus.toLowerCase() === 'delivered' ? '✅📦' : '🚚⚡';
        const title = `Shipment Status Updated! ${emoji}`;
        const message = `Your shipment '${itemName}' (#${deliveryId}) is now ${newStatus}.`;
        const payload = {
            notification: {
                title: title,
                body: message,
                sound: 'default'
            },
            data: {
                click_action: 'FLUTTER_NOTIFICATION_CLICK',
                type: 'status_update',
                parcelId: deliveryId,
                status: newStatus
            }
        };
        await admin.messaging().sendToDevice(fcmToken, payload);
        console.log(`[Delivery Trigger] Successfully sent status update push alert for delivery ${deliveryId} to user ${userId}`);
    }
    catch (error) {
        console.error('[Shipment Trigger Error] Failed to send status notification:', error);
    }
    return null;
});
/**
 * Cloud Function triggered when a document in the root 'riders' collection is changed.
 * Updates the root 'users' collection document's role and permissions to keep them synchronized.
 */
exports.onRiderDocumentChanged = functions.firestore
    .document('riders/{riderId}')
    .onWrite(async (change, context) => {
    const riderId = context.params.riderId;
    const afterData = change.after.data();
    console.log(`[Rider Sync Trigger] Change detected for rider: ${riderId}`);
    try {
        if (!change.after.exists) {
            console.log(`[Rider Sync Trigger] Rider document ${riderId} deleted. No action taken.`);
            return null;
        }
        const userRef = admin.firestore().collection('users').doc(riderId);
        // Update or enforce the role as 'rider' in the users collection
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
        console.log(`[Rider Sync Trigger] Successfully updated root user record for rider ID: ${riderId} to role: rider.`);
        // Sync auth custom claims
        try {
            await admin.auth().setCustomUserClaims(riderId, { rider: true, customer: false });
            console.log(`[Rider Sync Trigger] Custom user claims successfully synchronized for rider: ${riderId}`);
        }
        catch (authError) {
            console.warn(`[Rider Sync Trigger] Failed to update auth custom claims (user may not exist in Auth yet):`, authError);
        }
    }
    catch (error) {
        console.error(`[Rider Sync Trigger Error] Error synchronizing rider document for ID ${riderId}:`, error);
    }
    return null;
});
/**
 * Cloud Function triggered when a document is created in the root 'notifications' collection.
 * Fans out the notification to every active user's 'users/{uid}/notifications/' subcollection.
 */
exports.onNotificationCreated = functions.firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
    const notificationId = context.params.notificationId;
    const data = snap.data();
    const { title, description } = data;
    console.log(`[Notification Fan-out] New notification: ${notificationId} — ${title}`);
    try {
        const usersSnapshot = await admin.firestore().collection('users')
            .where('isDeleted', '==', false)
            .get();
        const batchSize = 500;
        let batch = admin.firestore().batch();
        let count = 0;
        usersSnapshot.forEach((userDoc) => {
            const notifRef = admin.firestore()
                .collection('users').doc(userDoc.id)
                .collection('notifications').doc();
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
                batch = admin.firestore().batch();
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
 * Cloud Function triggered when a document in the sub-collection 'users/{userId}/riders/{riderId}' is written.
 * Automatically updates the parent/root user's role to 'rider'.
 */
exports.onRiderSubcollectionChanged = functions.firestore
    .document('users/{userId}/riders/{riderId}')
    .onWrite(async (change, context) => {
    const userId = context.params.userId;
    console.log(`[Rider Subcollection Sync] Change detected for user sub-collection: ${userId}`);
    try {
        if (!change.after.exists)
            return null;
        const userRef = admin.firestore().collection('users').doc(userId);
        await userRef.update({
            role: 'rider',
            updatedAt: new Date().toISOString()
        });
        console.log(`[Rider Subcollection Sync] Successfully set user ${userId} role to rider.`);
        try {
            await admin.auth().setCustomUserClaims(userId, { rider: true, customer: false });
            console.log(`[Rider Subcollection Sync] Custom user claims successfully set for: ${userId}`);
        }
        catch (authError) {
            console.warn(`[Rider Subcollection Sync] Failed to update auth custom claims:`, authError);
        }
    }
    catch (err) {
        console.error(`[Rider Subcollection Sync Error]`, err);
    }
    return null;
});
//# sourceMappingURL=index.js.map