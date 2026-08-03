"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.onParcelStatusChanged = exports.verifyPaymentAndTopUp = void 0;
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();
/**
 * Callable function to verify payment and top up a user's wallet.
 * Takes { paymentToken, amount, reference }
 */
exports.verifyPaymentAndTopUp = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "User must be logged in.");
    }
    const { amount, reference } = data; // paymentToken omitted for simplicity here
    if (typeof amount !== "number" || amount <= 0) {
        throw new functions.https.HttpsError("invalid-argument", "Amount must be a positive number.");
    }
    const paystackSecret = process.env.PAYSTACK_SECRET_KEY;
    if (!paystackSecret) {
        throw new functions.https.HttpsError("internal", "Payment gateway is not configured.");
    }
    try {
        const response = await fetch(`https://api.paystack.co/transaction/verify/${reference}`, {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${paystackSecret}`,
                'Content-Type': 'application/json'
            }
        });
        const responseData = await response.json();
        if (!response.ok || !responseData.status || responseData.data.status !== 'success') {
            throw new functions.https.HttpsError("invalid-argument", "Payment verification failed.");
        }
        // Ensure the verified amount matches (Paystack amount is in kobo/cents)
        const verifiedAmount = responseData.data.amount;
        const requestedAmount = amount * 100;
        if (verifiedAmount < requestedAmount) {
            throw new functions.https.HttpsError("invalid-argument", "Payment amount mismatch.");
        }
    }
    catch (e) {
        console.error("Paystack verification error:", e);
        if (e instanceof functions.https.HttpsError)
            throw e;
        throw new functions.https.HttpsError("internal", "Payment verification service error.");
    }
    const uid = context.auth.uid;
    const userRef = db.collection("users").doc(uid);
    try {
        await db.runTransaction(async (transaction) => {
            var _a;
            const userDoc = await transaction.get(userRef);
            if (!userDoc.exists) {
                throw new functions.https.HttpsError("not-found", "User document not found.");
            }
            const currentBalance = ((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.walletBalance) || 0;
            const newBalance = currentBalance + amount;
            transaction.update(userRef, { walletBalance: newBalance });
        });
        return { success: true, message: "Wallet topped up successfully." };
    }
    catch (error) {
        console.error("Transaction failed:", error);
        throw new functions.https.HttpsError("internal", "Failed to update wallet balance.");
    }
});
/**
 * Firestore trigger to send push notification when a parcel status changes.
 */
exports.onParcelStatusChanged = functions.firestore
    .document("parcels/{parcelId}")
    .onUpdate(async (change, context) => {
    var _a;
    const before = change.before.data();
    const after = change.after.data();
    if (before.status === after.status) {
        return null; // Status didn't change
    }
    // Find the user to notify (assuming recipientId or senderId is on the parcel)
    const recipientId = after.recipientId || after.senderId;
    if (!recipientId)
        return null;
    const userDoc = await db.collection("users").doc(recipientId).get();
    const fcmToken = (_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.fcmToken;
    if (!fcmToken) {
        console.log("No FCM token found for user:", recipientId);
        return null;
    }
    const payload = {
        notification: {
            title: "Parcel Status Update",
            body: `Your parcel is now ${after.status}.`,
        },
        data: {
            parcelId: context.params.parcelId,
            status: after.status,
        }
    };
    try {
        await messaging.sendToDevice(fcmToken, payload);
        console.log("Notification sent successfully.");
    }
    catch (error) {
        console.error("Error sending notification:", error);
    }
    return null;
});
//# sourceMappingURL=index.js.map