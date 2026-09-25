import type { VercelRequest, VercelResponse } from "@vercel/node";
import nodemailer from "nodemailer";

export default async function handler(req: any, res: any) {
  if (req.method !== "POST") {
    return res.status(405).json({ success: false, error: "Method not allowed. Use POST." });
  }

  try {
    const { credentials } = req.body || {};
    const host = (credentials?.host || process.env.SMTP_HOST || "server.hostnextdns.com").trim();
    const port = Number(credentials?.port || process.env.SMTP_PORT || 465);
    const secure = credentials?.secure !== undefined ? Boolean(credentials.secure) : port === 465;
    const user = (credentials?.user || process.env.SMTP_USER || "noreply@engracedsmile.com").trim();
    const pass = (credentials?.pass || process.env.SMTP_PASS || "ha;LS.fiewLkDw~x").trim();

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: {
        rejectUnauthorized: false,
      },
    });

    await transporter.verify();

    return res.status(200).json({
      success: true,
      message: `SMTP handshake successful. Connected to ${host}:${port} as ${user}.`,
      verifiedAt: new Date().toISOString(),
    });
  } catch (error: any) {
    console.error("[Vercel SMTP Verify Error]", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Failed to establish SMTP connection.",
    });
  }
}
