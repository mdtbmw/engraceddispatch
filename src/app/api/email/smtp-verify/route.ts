import { NextRequest, NextResponse } from "next/server";
import * as nodemailer from "nodemailer";

export const dynamic = "force-dynamic";

interface SmtpCredentials {
  host?: string;
  port?: number;
  secure?: boolean;
  user?: string;
  pass?: string;
}

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
    const body = await req.json().catch(() => ({}));
    const creds: SmtpCredentials = body.credentials || {};

    const host = (creds.host || process.env.SMTP_HOST || DEFAULT_CONFIG.host).trim();
    const port = Number(creds.port || process.env.SMTP_PORT || DEFAULT_CONFIG.port);
    const secure = creds.secure !== undefined ? Boolean(creds.secure) : port === 465;
    const user = (creds.user || process.env.SMTP_USER || DEFAULT_CONFIG.user).trim();
    const pass = (creds.pass || process.env.SMTP_PASS || DEFAULT_CONFIG.pass).trim();

    if (!host || !user || !pass) {
      return NextResponse.json(
        { success: false, error: "Host, user, and password are required for SMTP verification." },
        { status: 400 }
      );
    }

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: {
        rejectUnauthorized: false, // Prevents certificate chain rejections in corporate/dev networks
      },
    });

    await transporter.verify();

    return NextResponse.json({
      success: true,
      message: `SMTP connection established successfully to ${host}:${port} as ${user}.`,
      details: { host, port, secure, user },
    });
  } catch (error: any) {
    console.error("[SMTP Verify Route Error]", error);
    return NextResponse.json(
      {
        success: false,
        error: error.message || "Failed to verify SMTP credentials.",
      },
      { status: 500 }
    );
  }
}
