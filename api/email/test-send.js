const nodemailer = require("nodemailer");

function extractPlainText(html) {
  if (!html) return "";
  return html
    .replace(/<style[^>]*>[\s\S]*?<\/style>/gi, "")
    .replace(/<script[^>]*>[\s\S]*?<\/script>/gi, "")
    .replace(/<br\s*[\/]?>/gi, "\n")
    .replace(/<\/p>/gi, "\n\n")
    .replace(/<\/h[1-6]>/gi, "\n\n")
    .replace(/<\/div>/gi, "\n")
    .replace(/<\/li>/gi, "\n")
    .replace(/<li>/gi, "• ")
    .replace(/<\/tr>/gi, "\n")
    .replace(/<td[^>]*>/gi, " ")
    .replace(/<a[^>]*href=["']([^"']+)["'][^>]*>([\s\S]*?)<\/a>/gi, "$2 ($1)")
    .replace(/<[^>]+>/g, "")
    .replace(/&bull;/g, "•")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&nbsp;/g, " ")
    .replace(/[ \t]+/g, " ")
    .replace(/\n\s+\n/g, "\n\n")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

const rateLimitMap = new Map();
function isRateLimited(key, maxLimit = 3, windowMs = 5 * 60 * 1000) {
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

function resolveSmtpConfig(credentials) {
  const envHost = (process.env.SMTP_HOST || "server.hostnextdns.com").trim();
  const envPort = Number(process.env.SMTP_PORT || 465);
  const envSecure = process.env.SMTP_SECURE === "false" ? false : envPort === 465;
  const envUser = (process.env.SMTP_USER || process.env.SMTP_EMAIL || "noreply@engracedsmile.com").trim();
  const envPass = (process.env.SMTP_PASS || process.env.SMTP_PASSWORD || "").trim();
  const envFromEmail = (process.env.SMTP_FROM_EMAIL || "noreply@engracedsmile.com").trim();
  const envFromName = (process.env.SMTP_FROM_NAME || "ESDispatch Logistics").trim();

  const clientPass = String((credentials && credentials.pass) || "").trim();

  if (clientPass) {
    const host = String((credentials && credentials.host) || "").trim();
    const port = Number((credentials && credentials.port) || envPort);
    const secure = credentials && credentials.secure !== undefined ? Boolean(credentials.secure) : port === 465;
    const user = String((credentials && credentials.user) || "").trim();
    if (isBlockedHost(host)) {
      return { error: { status: 400, message: "That server address cannot be used." } };
    }
    if (!user) {
      return { error: { status: 400, message: "SMTP username is required to use custom server settings." } };
    }
    const fromEmail = String((credentials && credentials.fromEmail) || "").trim() || user;
    const fromName = String((credentials && credentials.fromName) || "").trim() || "ESDispatch Logistics";
    return { config: { host, port, secure, user, pass: clientPass, fromEmail, fromName, fromEnv: false } };
  }

  const clientHost = credentials && credentials.host ? String(credentials.host).trim() : "";
  if (clientHost && normalizeHost(clientHost) !== normalizeHost(envHost)) {
    return { error: { status: 400, message: "Enter the SMTP password to use custom server settings." } };
  }
  if (!envPass) {
    console.error("[Test Send] SMTP_PASS or SMTP_PASSWORD is not configured in environment variables.");
    return { error: { status: 500, message: "SMTP server credentials are not configured on the server." } };
  }
  return { config: { host: envHost, port: envPort, secure: envSecure, user: envUser, pass: envPass, fromEmail: envFromEmail, fromName: envFromName, fromEnv: true } };
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
    const body = typeof req.body === "string" ? JSON.parse(req.body || "{}") : (req.body || {});
    const { to, subject, html, text, credentials } = body;

    if (!to || !to.includes("@")) {
      return res.status(400).json({ success: false, error: "Valid recipient email required." });
    }
    if (!subject || !String(subject).trim()) {
      return res.status(400).json({ success: false, error: "Email subject is required." });
    }
    if (!html || String(html).length > 300000) {
      return res.status(400).json({ success: false, error: "Email content is missing or too large." });
    }

    const clientIp = req.headers["x-forwarded-for"] || req.socket?.remoteAddress || "unknown";
    const recipient = String(to).trim().toLowerCase();
    if (isRateLimited(`ip:${clientIp}`, 3, 5 * 60 * 1000) || isRateLimited(`to:${recipient}`, 3, 5 * 60 * 1000)) {
      return res.status(429).json({ success: false, error: "Too many test emails. Please wait a few minutes." });
    }

    const resolved = resolveSmtpConfig(credentials);
    if (resolved.error) {
      return res.status(resolved.error.status).json({ success: false, error: resolved.error.message });
    }
    const { host, port, secure, user, pass, fromEmail, fromName } = resolved.config;

    const plainText = (text && text.trim().length > 50 && text !== subject)
      ? text.trim()
      : extractPlainText(html);

    const domain = fromEmail.includes("@") ? fromEmail.split("@")[1] : "engracedsmile.com";
    const randomHex = Math.random().toString(36).substring(2, 10);
    const messageId = `<${Date.now()}.${randomHex}@${domain}>`;

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: { rejectUnauthorized: false },
      connectionTimeout: 15000,
      greetingTimeout: 15000,
      socketTimeout: 20000,
    });

    const info = await transporter.sendMail({
      from: `"${fromName}" <${fromEmail}>`,
      sender: fromEmail,
      replyTo: `"ESDispatch Support" <support@${domain}>`,
      to: recipient,
      subject: String(subject).trim().slice(0, 300),
      text: plainText,
      html: html,
      messageId,
      envelope: {
        from: fromEmail,
        to: [recipient],
      },
      headers: {
        "X-Mailer": "ESDispatch Logistics Mailer/2026",
        "X-Priority": "3",
        "List-Unsubscribe": `<mailto:noreply@${domain}?subject=unsubscribe>`,
        "List-Unsubscribe-Post": "List-Unsubscribe=One-Click",
        "Feedback-ID": `esdispatch:notification:${Date.now()}`,
        "X-Entity-Ref-ID": `${Date.now()}-${randomHex}`,
      },
    });

    return res.status(200).json({
      success: true,
      message: `Email dispatched successfully to ${recipient}.`,
      messageId: info.messageId,
      accepted: info.accepted,
      response: info.response,
    });
  } catch (err) {
    console.error("[Test Send Error]", err);
    return res.status(500).json({
      success: false,
      error: err.message || "Failed to send email",
    });
  }
};
