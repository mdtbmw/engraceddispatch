"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.getSmtpConfig = getSmtpConfig;
exports.getTransporter = getTransporter;
exports.sendEmail = sendEmail;
var nodemailer = require("nodemailer");
var admin = require("firebase-admin");
var cachedTransporter = null;
var lastConfigHash = '';
/**
 * Retrieves SMTP settings either from Firestore system settings or environment variables.
 */
function getSmtpConfig() {
    return __awaiter(this, void 0, void 0, function () {
        var db, settingsDoc, data, err_1;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    db = admin.firestore();
                    _a.label = 1;
                case 1:
                    _a.trys.push([1, 3, , 4]);
                    return [4 /*yield*/, db.collection('system_settings').doc('smtp').get()];
                case 2:
                    settingsDoc = _a.sent();
                    if (settingsDoc.exists) {
                        data = settingsDoc.data() || {};
                        if (data.host && data.user && data.pass) {
                            return [2 /*return*/, {
                                    host: data.host,
                                    port: Number(data.port) || 465,
                                    secure: data.secure !== undefined ? Boolean(data.secure) : (Number(data.port) === 465),
                                    user: data.user,
                                    pass: data.pass,
                                    fromEmail: data.fromEmail || data.user,
                                    fromName: data.fromName || 'ESDispatch Logistics',
                                }];
                        }
                    }
                    return [3 /*break*/, 4];
                case 3:
                    err_1 = _a.sent();
                    console.warn('[SMTP] Could not load system_settings/smtp from Firestore:', err_1);
                    return [3 /*break*/, 4];
                case 4: 
                // Fallback to process.env or verified production SMTP
                return [2 /*return*/, {
                        host: process.env.SMTP_HOST || 'server.hostnextdns.com',
                        port: Number(process.env.SMTP_PORT) || 465,
                        secure: process.env.SMTP_SECURE === 'false' ? false : true,
                        user: process.env.SMTP_USER || process.env.SMTP_EMAIL || 'noreply@engracedsmile.com',
                        pass: process.env.SMTP_PASS || process.env.SMTP_PASSWORD || 'ha;LS.fiewLkDw~x',
                        fromEmail: process.env.SMTP_FROM_EMAIL || 'noreply@engracedsmile.com',
                        fromName: process.env.SMTP_FROM_NAME || 'ESDispatch Logistics',
                    }];
            }
        });
    });
}
/**
 * Returns a cached or newly initialized nodemailer Transporter.
 */
function getTransporter() {
    return __awaiter(this, void 0, void 0, function () {
        var config, configHash;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, getSmtpConfig()];
                case 1:
                    config = _a.sent();
                    configHash = "".concat(config.host, ":").concat(config.port, ":").concat(config.user, ":").concat(config.secure);
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
                    return [2 /*return*/, { transporter: cachedTransporter, config: config }];
            }
        });
    });
}
var emailTemplates_1 = require("./emailTemplates");
/**
 * Dispatches an email with structured logging, RFC deliverability headers, and 1:1 MIME plain-text parity.
 */
function sendEmail(options) {
    return __awaiter(this, void 0, void 0, function () {
        var _a, transporter, config, err, plainText, domain, randomHex, messageId, mailOptions, info, error_1;
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    _b.trys.push([0, 3, , 4]);
                    return [4 /*yield*/, getTransporter()];
                case 1:
                    _a = _b.sent(), transporter = _a.transporter, config = _a.config;
                    if (!config.user || !config.pass) {
                        err = 'SMTP credentials not configured (host/user/pass missing). Check system_settings/smtp or env vars.';
                        console.error("[SMTP ERROR] ".concat(err));
                        return [2 /*return*/, { success: false, error: err }];
                    }
                    plainText = (options.text && options.text.trim().length > 50 && options.text !== options.subject)
                        ? options.text.trim()
                        : (0, emailTemplates_1.extractPlainTextFromHtml)(options.html);
                    domain = config.fromEmail.includes('@') ? config.fromEmail.split('@')[1] : 'engracedsmile.com';
                    randomHex = Math.random().toString(36).substring(2, 10);
                    messageId = "<".concat(Date.now(), ".").concat(randomHex, "@").concat(domain, ">");
                    mailOptions = {
                        from: "\"".concat(config.fromName, "\" <").concat(config.fromEmail, ">"),
                        sender: config.fromEmail,
                        replyTo: "\"ESDispatch Support\" <support@".concat(domain, ">"),
                        to: options.to.trim(),
                        subject: options.subject.trim(),
                        text: plainText,
                        html: options.html,
                        messageId: messageId,
                        envelope: {
                            from: config.fromEmail,
                            to: [options.to.trim()],
                        },
                        headers: {
                            'X-Mailer': 'ESDispatch Logistics Mailer/2026',
                            'X-Priority': '3',
                            'List-Unsubscribe': "<mailto:support@".concat(domain, "?subject=unsubscribe>, <https://www.engracedsmile.com/unsubscribe>"),
                            'List-Unsubscribe-Post': 'List-Unsubscribe=One-Click',
                            'Feedback-ID': "esdispatch:notification:".concat(Date.now()),
                            'X-Entity-Ref-ID': "".concat(Date.now(), "-").concat(randomHex),
                        },
                    };
                    return [4 /*yield*/, transporter.sendMail(mailOptions)];
                case 2:
                    info = _b.sent();
                    console.log("[SMTP SUCCESS] Message delivered to ".concat(options.to, ". ID: ").concat(info.messageId));
                    return [2 /*return*/, { success: true, messageId: info.messageId }];
                case 3:
                    error_1 = _b.sent();
                    console.error("[SMTP FAILED] Could not send email to ".concat(options.to, ":"), error_1);
                    return [2 /*return*/, { success: false, error: error_1.message || 'Unknown SMTP error' }];
                case 4: return [2 /*return*/];
            }
        });
    });
}
