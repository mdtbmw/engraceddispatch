import nodemailer from "nodemailer";

function extractPlainTextFromHtml(html: string): string {
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

export default async function handler(req: any, res: any) {
  if (req.method !== "POST") {
    return res.status(405).json({ success: false, error: "Method not allowed. Use POST." });
  }

  try {
    const { to, subject, html, text, credentials } = req.body || {};

    if (!to || !to.includes("@")) {
      return res.status(400).json({ success: false, error: "A valid recipient email address is required." });
    }

    if (!subject || !html) {
      return res.status(400).json({ success: false, error: "Subject and HTML body are required." });
    }

    const host = (credentials?.host || process.env.SMTP_HOST || "server.hostnextdns.com").trim();
    const port = Number(credentials?.port || process.env.SMTP_PORT || 465);
    const secure = credentials?.secure !== undefined ? Boolean(credentials.secure) : port === 465;
    const user = (credentials?.user || process.env.SMTP_USER || "noreply@engracedsmile.com").trim();
    const pass = (credentials?.pass || process.env.SMTP_PASS || "ha;LS.fiewLkDw~x").trim();
    const fromEmail = (credentials?.fromEmail || credentials?.user || "noreply@engracedsmile.com").trim();
    const fromName = (credentials?.fromName || "ESDispatch Logistics").trim();

    const plainText = (text && text.trim().length > 50 && text !== subject)
      ? text.trim()
      : extractPlainTextFromHtml(html);

    const domain = fromEmail.includes("@") ? fromEmail.split("@")[1] : "engracedsmile.com";
    const randomHex = Math.random().toString(36).substring(2, 10);
    const messageId = `<${Date.now()}.${randomHex}@${domain}>`;

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: {
        rejectUnauthorized: false,
      },
    });

    const info = await transporter.sendMail({
      from: `"${fromName}" <${fromEmail}>`,
      sender: fromEmail,
      replyTo: `"ESDispatch Support" <support@${domain}>`,
      to: to.trim(),
      subject: subject.trim(),
      text: plainText,
      html: html,
      messageId: messageId,
      envelope: {
        from: fromEmail,
        to: [to.trim()],
      },
      headers: {
        "X-Mailer": "ESDispatch Logistics Mailer/2026",
        "X-Priority": "3",
        "List-Unsubscribe": `<mailto:support@${domain}?subject=unsubscribe>, <https://www.engracedsmile.com/unsubscribe>`,
        "List-Unsubscribe-Post": "List-Unsubscribe=One-Click",
        "Feedback-ID": `esdispatch:notification:${Date.now()}`,
        "X-Entity-Ref-ID": `${Date.now()}-${randomHex}`,
      },
    });

    console.log(`[Vercel SMTP Test Send] Sent to ${to}: MessageId ${info.messageId}`);

    return res.status(200).json({
      success: true,
      message: `Email dispatched successfully to ${to}.`,
      messageId: info.messageId,
      accepted: info.accepted,
      response: info.response,
    });
  } catch (error: any) {
    console.error("[Vercel SMTP Test Send Error]", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to dispatch email via SMTP.",
    });
  }
}
