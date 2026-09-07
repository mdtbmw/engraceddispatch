import * as nodemailer from 'nodemailer';
import * as admin from 'firebase-admin';

export interface SmtpConfig {
  host: string;
  port: number;
  secure: boolean;
  user: string;
  pass: string;
  fromEmail: string;
  fromName: string;
}

let cachedTransporter: nodemailer.Transporter | null = null;
let lastConfigHash: string = '';

/**
 * Retrieves SMTP settings either from Firestore system settings or environment variables.
 */
export async function getSmtpConfig(): Promise<SmtpConfig> {
  const db = admin.firestore();
  
  // Try to load dynamic SMTP settings from Firestore
  try {
    const settingsDoc = await db.collection('system_settings').doc('smtp').get();
    if (settingsDoc.exists) {
      const data = settingsDoc.data() || {};
      if (data.host && data.user && data.pass) {
        return {
          host: data.host,
          port: Number(data.port) || 465,
          secure: data.secure !== undefined ? Boolean(data.secure) : (Number(data.port) === 465),
          user: data.user,
          pass: data.pass,
          fromEmail: data.fromEmail || data.user,
          fromName: data.fromName || 'ESDispatch Logistics',
        };
      }
    }
  } catch (err) {
    console.warn('[SMTP] Could not load system_settings/smtp from Firestore:', err);
  }

  // Fallback to process.env
  return {
    host: process.env.SMTP_HOST || 'smtp.gmail.com',
    port: Number(process.env.SMTP_PORT) || 465,
    secure: process.env.SMTP_SECURE === 'false' ? false : true,
    user: process.env.SMTP_USER || process.env.SMTP_EMAIL || '',
    pass: process.env.SMTP_PASS || process.env.SMTP_PASSWORD || '',
    fromEmail: process.env.SMTP_FROM_EMAIL || process.env.SMTP_USER || 'dispatch@esdispatch.com',
    fromName: process.env.SMTP_FROM_NAME || 'ESDispatch Logistics',
  };
}

/**
 * Returns a cached or newly initialized nodemailer Transporter.
 */
export async function getTransporter(): Promise<{ transporter: nodemailer.Transporter; config: SmtpConfig }> {
  const config = await getSmtpConfig();
  const configHash = `${config.host}:${config.port}:${config.user}:${config.secure}`;

  if (!cachedTransporter || configHash !== lastConfigHash) {
    cachedTransporter = nodemailer.createTransport({
      host: config.host,
      port: config.port,
      secure: config.secure,
      auth: {
        user: config.user,
        pass: config.pass,
      },
      tls: {
        rejectUnauthorized: false, // Prevents self-signed cert rejections in dev/corporate networks
      },
    });
    lastConfigHash = configHash;
  }

  return { transporter: cachedTransporter, config };
}

/**
 * Dispatches an email with structured logging and fallback diagnostics.
 */
export async function sendEmail(options: {
  to: string;
  subject: string;
  html: string;
  text?: string;
}): Promise<{ success: boolean; messageId?: string; error?: string }> {
  try {
    const { transporter, config } = await getTransporter();

    if (!config.user || !config.pass) {
      const err = 'SMTP credentials not configured (host/user/pass missing). Check system_settings/smtp or env vars.';
      console.error(`[SMTP ERROR] ${err}`);
      return { success: false, error: err };
    }

    const mailOptions = {
      from: `"${config.fromName}" <${config.fromEmail}>`,
      to: options.to,
      subject: options.subject,
      text: options.text || options.subject,
      html: options.html,
    };

    const info = await transporter.sendMail(mailOptions);
    console.log(`[SMTP SUCCESS] Message delivered to ${options.to}. ID: ${info.messageId}`);
    return { success: true, messageId: info.messageId };
  } catch (error: any) {
    console.error(`[SMTP FAILED] Could not send email to ${options.to}:`, error);
    return { success: false, error: error.message || 'Unknown SMTP error' };
  }
}
