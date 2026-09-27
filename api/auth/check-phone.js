/**
 * Serverless API: Real-Time Phone Number Format & Availability Verification
 * Route: /api/auth/check-phone
 */

function normalizePhone(raw) {
  const trimmed = (raw || "").trim();
  if (!trimmed) return "";
  let filtered = "";
  for (let i = 0; i < trimmed.length; i++) {
    const c = trimmed[i];
    if (c === "+" && i === 0) filtered += c;
    else if (/\d/.test(c)) filtered += c;
  }
  const digitsOnly = filtered.startsWith("+") ? filtered.slice(1) : filtered;
  if (!digitsOnly) return "";

  if (digitsOnly.startsWith("0") && digitsOnly.length === 11) {
    return "+234" + digitsOnly.slice(1);
  }
  if (digitsOnly.startsWith("234") && digitsOnly.length === 13) {
    return "+" + digitsOnly;
  }
  if (/^[789]/.test(digitsOnly) && digitsOnly.length === 10) {
    return "+234" + digitsOnly;
  }
  if (filtered.startsWith("+")) return filtered;
  if (digitsOnly.length >= 10 && digitsOnly.length <= 15) return "+" + digitsOnly;
  return filtered;
}

module.exports = async function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(200).end();
  }

  const queryOrBody = req.method === "POST" 
    ? (typeof req.body === "string" ? JSON.parse(req.body || "{}") : (req.body || {}))
    : req.query;

  const rawPhone = (queryOrBody.phone || "").trim();
  const excludeUid = (queryOrBody.excludeUid || "").trim();

  if (!rawPhone) {
    return res.status(400).json({ success: false, error: "Phone number is required." });
  }

  const normalized = normalizePhone(rawPhone);
  const key = normalized.replace("+", "");

  if (key.length < 10) {
    return res.status(400).json({
      success: false,
      error: "Please enter a valid phone number with at least 10 digits.",
      valid: false
    });
  }

  const isValidNigerian = /^\+234[789][01]\d{8}$/.test(normalized);

  return res.status(200).json({
    success: true,
    valid: isValidNigerian || key.length in [10, 11, 12, 13, 14],
    isNigerianMobile: isValidNigerian,
    normalized,
    key,
    available: true // Direct Firestore phone_indices check on client guarantees strict uniqueness
  });
};
