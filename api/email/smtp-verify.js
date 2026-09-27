const nodemailer = require("nodemailer");

const rateLimitMap = new Map();
function isRateLimited(key, maxLimit = 5, windowMs = 5 * 60 * 1000) {
  const now = Date.now();
  const timestamps = (rateLimitMap.get(key) || []).filter((ts) => now - ts < windowMs);
  if (timestamps.length >= maxLimit) {
    return true;
  }
  timestamps.push(now);
  rateLimitMap.set(key, timestamps);
  return false;
}

function isBlockedHost(host) {
  const h = String(host || "").trim().toLowerCase();
  if (!h) return true;
  if (h === "localhost" || h.endsWith(".local") || h.endsWith(".internal") || h.endsWith(".localdomain")) return true;
  if (/^(127\.|0\.|10\.|192\.168\.|169\.254\.)/.test(h)) return true;
  if (/^172\.(1[6-9]|2\d|3[01])\./.test(h)) return true;
  if (/^\[?::1\]?/.test(h)) return true;
  return false;
}

function normalizeHost(host) {
  return String(host || "").trim().toLowerCase().replace(/^\[|\]$/g, "");
}

function isKnownServerHost(host) {
  const norm = normalizeHost(host);
  return norm === "server.hostnextdns.com" || norm === "mail.engracedsmile.com" || norm === "engracedsmile.com";
}

function resolveSmtpConfig(credentials) {
  const envHost = (process.env.SMTP_HOST || "server.hostnextdns.com").trim();
  const envPort = Number(process.env.SMTP_PORT || 465);
  const envSecure = process.env.SMTP_SECURE === "false" ? false : envPort === 465;
  const envUser = (process.env.SMTP_USER || process.env.SMTP_EMAIL || "noreply@engracedsmile.com").trim();
  const envPass = (process.env.SMTP_PASS || process.env.SMTP_PASSWORD || "").trim();

  const clientPass = String((credentials && credentials.pass) || "").trim();

  if (clientPass) {
    const host = String((credentials && credentials.host) || "").trim();
    const port = Number((credentials && credentials.port) || envPort);
    const secure = credentials && credentials.secure !== undefined ? Boolean(credentials.secure) : port === 465;
    const user = String((credentials && credentials.user) || "").trim();
    if (isBlockedHost(host)) {
      return { error: { status: 400, message: "That server address cannot be tested." } };
    }
    if (!user) {
      return { error: { status: 400, message: "SMTP username is required to test custom server settings." } };
    }
    return { config: { host, port, secure, user, pass: clientPass, fromEnv: false } };
  }

  const clientHost = credentials && credentials.host ? String(credentials.host).trim() : "";
  const isKnownMatch = isKnownServerHost(clientHost) && isKnownServerHost(envHost);
  if (clientHost && normalizeHost(clientHost) !== normalizeHost(envHost) && !isKnownMatch) {
    return { error: { status: 400, message: "Enter the SMTP password to test custom server settings." } };
  }
  const effectiveHost = (clientHost && isKnownMatch) ? clientHost : envHost;
  if (!envPass) {
    console.error("[SMTP Verify] SMTP_PASS or SMTP_PASSWORD is not configured in environment variables.");
    return { error: { status: 500, message: "SMTP server credentials are not configured on the server." } };
  }
  return { config: { host: effectiveHost, port: envPort, secure: envSecure, user: envUser, pass: envPass, fromEnv: true } };
}

module.exports = async function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(200).end();
  }

  if (req.method !== "POST") {
    return res.status(405).json({ success: false, error: "Method not allowed" });
  }

  try {
    const clientIp = req.headers["x-forwarded-for"] || req.socket?.remoteAddress || "unknown";
    if (isRateLimited(`ip:${clientIp}`, 5, 5 * 60 * 1000)) {
      return res.status(429).json({ success: false, error: "Too many connection tests. Please wait a few minutes." });
    }

    const body = typeof req.body === "string" ? JSON.parse(req.body || "{}") : (req.body || {});
    const resolved = resolveSmtpConfig(body.credentials);
    if (resolved.error) {
      return res.status(resolved.error.status).json({ success: false, error: resolved.error.message });
    }
    const { host, port, secure, user, pass } = resolved.config;

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: { rejectUnauthorized: false },
      connectionTimeout: 8000,
      greetingTimeout: 8000,
      socketTimeout: 10000,
    });

    await transporter.verify();

    return res.status(200).json({
      success: true,
      message: `SMTP handshake successful. Connected to ${host}:${port} as ${user}.`,
      verifiedAt: new Date().toISOString(),
    });
  } catch (error) {
    console.error("[SMTP Verify Error]", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to establish SMTP connection.",
    });
  }
};
