import { NextRequest, NextResponse } from "next/server";
import * as nodemailer from "nodemailer";
import { renderAccountVerificationEmail, extractPlainTextFromHtml } from "@/lib/emailTemplates";

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
    const { email, name, userId, otp: providedOtp, verificationLink } = body;

    const recipientEmail = (email || "").trim().toLowerCase();
    if (!recipientEmail || !recipientEmail.includes("@")) {
      return NextResponse.json(
        { success: false, error: "A valid recipient email address is required." },
        { status: 400 }
      );
    }

    const recipientName = (name || "Valued Client").trim();
    const otp = (providedOtp || "").toString().trim() || Math.floor(100000 + Math.random() * 900000).toString();

    const host = (process.env.SMTP_HOST || DEFAULT_CONFIG.host).trim();
    const port = Number(process.env.SMTP_PORT || DEFAULT_CONFIG.port);
    const secure = process.env.SMTP_SECURE === "false" ? false : port === 465;
    const user = (process.env.SMTP_USER || process.env.SMTP_EMAIL || DEFAULT_CONFIG.user).trim();
    const pass = (process.env.SMTP_PASS || process.env.SMTP_PASSWORD || DEFAULT_CONFIG.pass).trim();
    const fromEmail = (process.env.SMTP_FROM_EMAIL || DEFAULT_CONFIG.fromEmail).trim();
    const fromName = (process.env.SMTP_FROM_NAME || DEFAULT_CONFIG.fromName).trim();

    const htmlContent = renderAccountVerificationEmail({
      name: recipientName,
      otp,
      verificationLink,
      expiryMinutes: 15,
    });

    const plainText = extractPlainTextFromHtml(htmlContent);

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure,
      auth: { user, pass },
      tls: {
        rejectUnauthorized: false,
      },
    });

    const domain = fromEmail.includes("@") ? fromEmail.split("@")[1] : "engracedsmile.com";
    const randomHex = Math.random().toString(36).substring(2, 10);
    const messageId = `<verify.${Date.now()}.${randomHex}@${domain}>`;
    const subject = `Verify Your ESDispatch Account (${otp})`;

    const mailOptions = {
      from: `"${fromName}" <${fromEmail}>`,
      sender: fromEmail,
      replyTo: `"ESDispatch Support" <support@${domain}>`,
      to: recipientEmail,
      subject,
      text: plainText,
      html: htmlContent,
      messageId,
      envelope: {
        from: fromEmail,
        to: [recipientEmail],
      },
      headers: {
        "X-Mailer": "ESDispatch Logistics Mailer/2026",
        "X-Priority": "1", // High priority for security passcodes
        "List-Unsubscribe": `<mailto:support@${domain}?subject=unsubscribe>, <https://www.engracedsmile.com/unsubscribe>`,
        "List-Unsubscribe-Post": "List-Unsubscribe=One-Click",
        "Feedback-ID": `esdispatch:security-verification:${Date.now()}`,
        "X-Entity-Ref-ID": `${Date.now()}-${randomHex}`,
      },
    };

    const info = await transporter.sendMail(mailOptions);
    console.log(`[Verification Email Success] Dispatched to ${recipientEmail} with ID: ${info.messageId}`);

    return NextResponse.json({
      success: true,
      message: `Verification passcode dispatched to ${recipientEmail}.`,
      otp,
      messageId: info.messageId,
    });
  } catch (error: any) {
    console.error("[Verification Email Route Error]", error);
    return NextResponse.json(
      {
        success: false,
        error: error.message || "Failed to dispatch verification email.",
      },
      { status: 500 }
    );
  }
}
