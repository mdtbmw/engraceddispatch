/**
 * ESDispatch Phone Normalization & Indexing Utility
 * Ensures identical E.164 canonical normalization across web, mobile, and Firestore indexes.
 */

export function normalizePhoneNumber(raw: string): string {
  const trimmed = (raw || "").trim();
  if (!trimmed) return "";

  // Keep leading '+' and all digits
  let filtered = "";
  for (let i = 0; i < trimmed.length; i++) {
    const c = trimmed[i];
    if (c === "+" && i === 0) {
      filtered += c;
    } else if (/\d/.test(c)) {
      filtered += c;
    }
  }

  const digitsOnly = filtered.startsWith("+") ? filtered.slice(1) : filtered;
  if (!digitsOnly) return "";

  // Local Nigerian format starting with 0 (e.g. 08031234567 -> +2348031234567)
  if (digitsOnly.startsWith("0") && digitsOnly.length === 11) {
    return "+234" + digitsOnly.slice(1);
  }

  // Nigerian international without plus (e.g. 2348031234567 -> +2348031234567)
  if (digitsOnly.startsWith("234") && digitsOnly.length === 13) {
    return "+" + digitsOnly;
  }

  // 10 digits without leading 0 (e.g. 8031234567 -> +2348031234567)
  if (/^[789]/.test(digitsOnly) && digitsOnly.length === 10) {
    return "+234" + digitsOnly;
  }

  if (filtered.startsWith("+")) {
    return filtered;
  }

  if (digitsOnly.length >= 10 && digitsOnly.length <= 15) {
    return "+" + digitsOnly;
  }

  return filtered;
}

/**
 * Returns digits-only key for the phone_indices collection (e.g. "2348031234567").
 */
export function phoneIndexKey(raw: string): string {
  return normalizePhoneNumber(raw).replace("+", "").trim();
}

/**
 * Checks if a phone number matches standard Nigerian mobile patterns.
 */
export function isValidNigerianPhone(raw: string): boolean {
  const normalized = normalizePhoneNumber(raw);
  return /^\+234[789][01]\d{8}$/.test(normalized);
}
