import * as crypto from 'crypto';
import * as admin from 'firebase-admin';

export type OtpPurpose =
  | 'SIGN_UP'
  | 'PASSWORD_RESET'
  | 'TWO_FACTOR'
  | 'PIN_RESET'
  | 'DELIVERY_HANDOVER';

const OTP_PEPPER = 'ESDISPATCH_OTP_SECURE_PEPPER_2026';
const MAX_ATTEMPTS = 5;

/**
 * Computes a SHA-256 hash of the 6-digit code with pepper.
 */
function hashOtp(code: string): string {
  return crypto.createHmac('sha256', OTP_PEPPER).update(code.trim()).digest('hex');
}

export interface GeneratedOtpResult {
  code: string;
  docId: string;
  expiresAt: Date;
}

/**
 * Generates a cryptographically strong 6-digit OTP, records it in Firestore, and returns the raw code.
 */
export async function createAndStoreOtp(
  email: string,
  purpose: OtpPurpose,
  expiryMinutes: number = 10
): Promise<GeneratedOtpResult> {
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
    if (existingData?.createdAt) {
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

export interface VerificationResult {
  valid: boolean;
  message: string;
}

/**
 * Validates the submitted OTP against the hashed record in Firestore.
 */
export async function verifyOtpCode(
  email: string,
  purpose: OtpPurpose,
  rawCode: string
): Promise<VerificationResult> {
  const db = admin.firestore();
  const normalizedEmail = email.toLowerCase().trim();
  const docId = `${normalizedEmail.replace(/[^a-z0-9]/g, '_')}_${purpose.toLowerCase()}`;
  const otpRef = db.collection('verification_otps').doc(docId);

  const snap = await otpRef.get();
  if (!snap.exists) {
    return { valid: false, message: 'No active verification code found for this account. Please request a new code.' };
  }

  const data = snap.data()!;

  if (data.used) {
    return { valid: false, message: 'This code has already been used. Please request a new code.' };
  }

  const now = Date.now();
  const expiresAtMs = data.expiresAt?.toMillis ? data.expiresAt.toMillis() : new Date(data.expiresAt).getTime();

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
