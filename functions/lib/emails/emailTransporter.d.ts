import * as nodemailer from 'nodemailer';
export interface SmtpConfig {
    host: string;
    port: number;
    secure: boolean;
    user: string;
    pass: string;
    fromEmail: string;
    fromName: string;
}
/**
 * Retrieves SMTP settings either from Firestore system settings or environment variables.
 */
export declare function getSmtpConfig(): Promise<SmtpConfig>;
/**
 * Returns a cached or newly initialized nodemailer Transporter.
 */
export declare function getTransporter(): Promise<{
    transporter: nodemailer.Transporter;
    config: SmtpConfig;
}>;
/**
 * Dispatches an email with structured logging, RFC deliverability headers, and 1:1 MIME plain-text parity.
 */
export declare function sendEmail(options: {
    to: string;
    subject: string;
    html: string;
    text?: string;
}): Promise<{
    success: boolean;
    messageId?: string;
    error?: string;
}>;
