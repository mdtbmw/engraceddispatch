import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Callable function to verify payment and top up a user's wallet.
 * Takes { paymentToken, amount, reference }
 */
export const verifyPaymentAndTopUp = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "User must be logged in.");
    }
    
    const { amount, reference } = data; // paymentToken omitted for simplicity here
    
    if (typeof amount !== "number" || amount <= 0) {
        throw new functions.https.HttpsError("invalid-argument", "Amount must be a positive number.");
    }
    
    // <TODO> REAL PAYMENT GATEWAY VERIFICATION
    // e.g., const response = await paystack.transaction.verify(reference);
    // if (!response.data.status) throw Error("Payment failed");
    
    const uid = context.auth.uid;
    const userRef = db.collection("users").doc(uid);
    
    try {
        await db.runTransaction(async (transaction) => {
            const userDoc = await transaction.get(userRef);
            
            if (!userDoc.exists) {
                throw new functions.https.HttpsError("not-found", "User document not found.");
            }
            
            const currentBalance = userDoc.data()?.walletBalance || 0;
            const newBalance = currentBalance + amount;
            
            transaction.update(userRef, { walletBalance: newBalance });
        });
        
        return { success: true, message: "Wallet topped up successfully." };
    } catch (error) {
        console.error("Transaction failed:", error);
        throw new functions.https.HttpsError("internal", "Failed to update wallet balance.");
    }
});

/**
 * Firestore trigger to send push notification when a parcel status changes.
 */
export const onParcelStatusChanged = functions.firestore
    .document("parcels/{parcelId}")
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();
        
        if (before.status === after.status) {
            return null; // Status didn't change
        }
        
        // Find the user to notify (assuming recipientId or senderId is on the parcel)
        const recipientId = after.recipientId || after.senderId;
        if (!recipientId) return null;
        
        const userDoc = await db.collection("users").doc(recipientId).get();
        const fcmToken = userDoc.data()?.fcmToken;
        
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
        } catch (error) {
            console.error("Error sending notification:", error);
        }
        return null;
    });
