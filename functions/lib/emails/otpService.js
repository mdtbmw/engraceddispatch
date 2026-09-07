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
exports.createAndStoreOtp = createAndStoreOtp;
exports.verifyOtpCode = verifyOtpCode;
const crypto = __importStar(require("crypto"));
const admin = __importStar(require("firebase-admin"));
const OTP_PEPPER = 'ESDISPATCH_OTP_SECURE_PEPPER_2026';
const MAX_ATTEMPTS = 5;
/**
 * Computes a SHA-256 hash of the 6-digit code with pepper.
 */
function hashOtp(code) {
    return crypto.createHmac('sha256', OTP_PEPPER).update(code.trim()).digest('hex');
}
/**
 * Generates a cryptographically strong 6-digit OTP, records it in Firestore, and returns the raw code.
 */
async function createAndStoreOtp(email, purpose, expiryMinutes = 10) {
    const db = admin.firestore();
    const normalizedEmail = email.toLowerCase().trim();
    // Cryptographic 6-digit code (100000 - 999999)
    const codeInt = crypto.randomInt(100000, 1000000);
    const code = codeInt.toString();
    const hashedCode = hashOtp(code);
    const now = new Date();
    const expiresAt = new Date(now.getTime() + expiryMinutes * 60 * 1000);
    // Use a predictable key per email + purpose to prevent database bloat and invalidate prior codes
    const docId = `${normalizedEmail.replace(/[^a-z0-9]/g, '_')}_${purpose.toLowerCase()}`;
    const otpRef = db.collection('verification_otps').doc(docId);
    // Check rate limit: minimum 30 seconds cooldown between generation requests
    const existingDoc = await otpRef.get();
    if (existingDoc.exists) {
        const existingData = existingDoc.data();
        if (existingData === null || existingData === void 0 ? void 0 : existingData.createdAt) {
            const createdAtMs = existingData.createdAt.toMillis ? existingData.createdAt.toMillis() : new Date(existingData.createdAt).getTime();
            const elapsedSeconds = (now.getTime() - createdAtMs) / 1000;
            if (elapsedSeconds < 30) {
                throw new Error(`Please wait ${Math.ceil(30 - elapsedSeconds)}s before requesting a new code.`);
            }
        }
    }
    await otpRef.set({
        email: normalizedEmail,
        purpose,
        hashedCode,
        attempts: 0,
        used: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        expiresAt: admin.firestore.Timestamp.fromDate(expiresAt),
    });
    return { code, docId, expiresAt };
}
/**
 * Validates the submitted OTP against the hashed record in Firestore.
 */
async function verifyOtpCode(email, purpose, rawCode) {
    var _a;
    const db = admin.firestore();
    const normalizedEmail = email.toLowerCase().trim();
    const docId = `${normalizedEmail.replace(/[^a-z0-9]/g, '_')}_${purpose.toLowerCase()}`;
    const otpRef = db.collection('verification_otps').doc(docId);
    const snap = await otpRef.get();
    if (!snap.exists) {
        return { valid: false, message: 'No active verification code found for this account. Please request a new code.' };
    }
    const data = snap.data();
    if (data.used) {
        return { valid: false, message: 'This code has already been used. Please request a new code.' };
    }
    const now = Date.now();
    const expiresAtMs = ((_a = data.expiresAt) === null || _a === void 0 ? void 0 : _a.toMillis) ? data.expiresAt.toMillis() : new Date(data.expiresAt).getTime();
    if (now > expiresAtMs) {
        return { valid: false, message: 'Verification code has expired. Please request a new code.' };
    }
    if ((data.attempts || 0) >= MAX_ATTEMPTS) {
        return { valid: false, message: 'Maximum attempts exceeded. This code is invalidated. Please request a new one.' };
    }
    const inputHash = hashOtp(rawCode);
    if (inputHash !== data.hashedCode) {
        await otpRef.update({
            attempts: admin.firestore.FieldValue.increment(1),
        });
        const remaining = MAX_ATTEMPTS - (data.attempts || 0) - 1;
        return { valid: false, message: `Incorrect code. ${remaining} attempt${remaining === 1 ? '' : 's'} remaining.` };
    }
    // Code matches! Mark as used
    await otpRef.update({
        used: true,
        verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return { valid: true, message: 'Code verified successfully.' };
}
//# sourceMappingURL=otpService.js.map