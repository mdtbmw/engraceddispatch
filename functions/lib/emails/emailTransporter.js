"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.getSmtpConfig = getSmtpConfig;
exports.getTransporter = getTransporter;
exports.sendEmail = sendEmail;
const nodemailer = __importStar(require("nodemailer"));
const admin = __importStar(require("firebase-admin"));
let cachedTransporter = null;
let lastConfigHash = '';
/**
 * Retrieves SMTP settings either from Firestore system settings or environment variables.
 */
async function getSmtpConfig() {
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
    }
    catch (err) {
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
async function getTransporter() {
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
async function sendEmail(options) {
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
    }
    catch (error) {
        console.error(`[SMTP FAILED] Could not send email to ${options.to}:`, error);
        return { success: false, error: error.message || 'Unknown SMTP error' };
    }
}
//# sourceMappingURL=emailTransporter.js.map