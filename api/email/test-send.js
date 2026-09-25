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

    const host = (credentials?.host || process.env.SMTP_HOST || "server.hostnextdns.com").trim();
    const port = Number(credentials?.port || process.env.SMTP_PORT || 465);
    const secure = port === 465;
    const user = (credentials?.user || process.env.SMTP_USER || "noreply@engracedsmile.com").trim();
    const pass = (credentials?.pass || process.env.SMTP_PASS || "ha;LS.fiewLkDw~x").trim();
    const fromEmail = (credentials?.fromEmail || user).trim();
    const fromName = (credentials?.fromName || "ESDispatch Logistics").trim();

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
      to: to.trim(),
      subject: (subject || "ESDispatch Notification").trim(),
      text: plainText,
      html: html,
      messageId,
      envelope: {
        from: fromEmail,
        to: [to.trim()],
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
      message: `Email dispatched successfully to ${to}.`,
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
