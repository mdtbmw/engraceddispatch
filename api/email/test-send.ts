import nodemailer from "nodemailer";

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
      to: to.trim(),
      subject: subject.trim(),
      text: text || subject,
      html: html,
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
