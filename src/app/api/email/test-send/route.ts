import { NextRequest, NextResponse } from "next/server";
import * as nodemailer from "nodemailer";

export const dynamic = "force-dynamic";

const DEFAULT_CONFIG = {
  host: "server.hostnextdns.com",
  port: 465,
  secure: true,
  user: "noreply@engracedsmile.com",
  pass: "ha;LS.fiewLkDw~x",
  fromEmail: "noreply@engracedsmile.com",
  fromName: "ESDispatch Logistics",
};

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { to, subject, html, text, credentials } = body;

    if (!to || !to.includes("@")) {
      return NextResponse.json(
        { success: false, error: "A valid recipient email address is required." },
        { status: 400 }
      );
    }

    if (!subject || !html) {
      return NextResponse.json(
        { success: false, error: "Subject and HTML body are required." },
        { status: 400 }
      );
    }

    const creds = credentials || {};
    const host = (creds.host || process.env.SMTP_HOST || DEFAULT_CONFIG.host).trim();
    const port = Number(creds.port || process.env.SMTP_PORT || DEFAULT_CONFIG.port);
    const secure = creds.secure !== undefined ? Boolean(creds.secure) : port === 465;
    const user = (creds.user || process.env.SMTP_USER || DEFAULT_CONFIG.user).trim();
    const pass = (creds.pass || process.env.SMTP_PASS || DEFAULT_CONFIG.pass).trim();
    const fromEmail = (creds.fromEmail || creds.user || DEFAULT_CONFIG.fromEmail).trim();
    const fromName = (creds.fromName || DEFAULT_CONFIG.fromName).trim();

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: {
        rejectUnauthorized: false,
      },
    });

    const mailOptions = {
      from: `"${fromName}" <${fromEmail}>`,
      to: to.trim(),
      subject: subject.trim(),
      text: text || subject,
      html: html,
    };

    const info = await transporter.sendMail(mailOptions);
    console.log(`[SMTP Test Send] Success to ${to}: MessageId ${info.messageId}`);

    return NextResponse.json({
      success: true,
      message: `Email dispatched successfully to ${to}.`,
      messageId: info.messageId,
      accepted: info.accepted,
      response: info.response,
    });
  } catch (error: any) {
    console.error("[SMTP Test Send Route Error]", error);
    return NextResponse.json(
      {
        success: false,
        error: error.message || "Failed to dispatch email via SMTP.",
      },
      { status: 500 }
    );
  }
}
