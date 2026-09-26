import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

function emailApiPlugin() {
  return {
    name: "vite-plugin-email-api",
    configureServer(server: any) {
      server.middlewares.use(async (req: any, res: any, next: any) => {
        if (req.url === "/api/email/smtp-verify" && req.method === "POST") {
          let body = "";
          req.on("data", (chunk: any) => (body += chunk));
          req.on("end", async () => {
            try {
              const data = JSON.parse(body || "{}");
              const creds = data.credentials || {};
              const host = (creds.host || "server.hostnextdns.com").trim();
              const port = Number(creds.port || 465);
              const secure = creds.secure !== undefined ? Boolean(creds.secure) : port === 465;
              const user = (creds.user || "noreply@engracedsmile.com").trim();
              const pass = (creds.pass || "ha;LS.fiewLkDw~x").trim();

              const nodemailer = await import("nodemailer");
              const transporter = nodemailer.createTransport({
                host,
                port,
                secure,
                auth: { user, pass },
                tls: { rejectUnauthorized: false },
              });
              await transporter.verify();
              res.setHeader("Content-Type", "application/json");
              res.end(JSON.stringify({ success: true, message: `Connected to ${host}:${port} as ${user}.` }));
            } catch (err: any) {
              res.setHeader("Content-Type", "application/json");
              res.statusCode = 500;
              res.end(JSON.stringify({ success: false, error: err.message || "Failed to verify SMTP" }));
            }
          });
          return;
        }

        if (req.url?.startsWith("/api/email/dns-check")) {
          const dns = await import("dns");
          const queryDomain = "engracedsmile.com";

          try {
            const resolver = new dns.promises.Resolver();
            try {
              resolver.setServers(["5.39.69.62", "1.1.1.1", "8.8.8.8"]);
            } catch (e) {}

            let rootTxtRecords: string[][] = [];
            try {
              rootTxtRecords = await resolver.resolveTxt(queryDomain);
            } catch (e: any) {
              try {
                rootTxtRecords = await dns.promises.resolveTxt(queryDomain);
              } catch (e2) {}
            }

            const flatTxt = rootTxtRecords.map((chunks) => (Array.isArray(chunks) ? chunks.join("") : chunks));
            const spfRecord = flatTxt.find((txt) => typeof txt === "string" && txt.toLowerCase().startsWith("v=spf1"));
            const expectedSpf = "v=spf1 ip4:5.39.69.62 include:server.hostnextdns.com ~all";
            const spfExists = Boolean(spfRecord);
            const spfIncludesHost = spfExists && (spfRecord!.includes("5.39.69.62") || spfRecord!.includes("server.hostnextdns.com") || spfRecord!.includes("+ip4"));

            let dmarcTxtRecords: string[][] = [];
            try {
              dmarcTxtRecords = await resolver.resolveTxt(`_dmarc.${queryDomain}`);
            } catch (e: any) {
              try {
                dmarcTxtRecords = await dns.promises.resolveTxt(`_dmarc.${queryDomain}`);
              } catch (e2) {}
            }

            const flatDmarc = dmarcTxtRecords.map((chunks) => (Array.isArray(chunks) ? chunks.join("") : chunks));
            const dmarcRecord = flatDmarc.find((txt) => typeof txt === "string" && (txt.toUpperCase().startsWith("V=DMARC1") || txt.toLowerCase().startsWith("v=dmarc1")));
            const expectedDmarc = `v=DMARC1; p=none; rua=mailto:noreply@${queryDomain}; aspf=r;`;
            const dmarcExists = Boolean(dmarcRecord);

            let mxRecords: Array<{ exchange: string; priority: number }> = [];
            try {
              mxRecords = await resolver.resolveMx(queryDomain);
            } catch (e: any) {
              try {
                mxRecords = await dns.promises.resolveMx(queryDomain);
              } catch (e2) {}
            }
            const mxExists = mxRecords && mxRecords.length > 0;
            const expectedMx = "server.hostnextdns.com (Priority 10)";

            const recommendations: string[] = [];
            let spamRiskLevel: "CRITICAL" | "MODERATE" | "LOW" = "LOW";

            if (!spfExists) {
              spamRiskLevel = "CRITICAL";
              recommendations.push(
                `Add SPF TXT record: Name '@', Value '${expectedSpf}'. Gmail & Yahoo automatically flag emails as SPAM without SPF.`
              );
            } else if (!spfIncludesHost) {
              if (spamRiskLevel !== "CRITICAL") spamRiskLevel = "MODERATE";
              recommendations.push(
                `Update SPF TXT record to authorize HostNextDNS IP (5.39.69.62) or include:server.hostnextdns.com.`
              );
            }

            if (!dmarcExists) {
              if (spamRiskLevel !== "CRITICAL") spamRiskLevel = "MODERATE";
              recommendations.push(
                `Add DMARC TXT record: Name '_dmarc', Value '${expectedDmarc}'. Required by 2024 Google/Yahoo inbox standards.`
              );
            }

            if (!mxExists) {
              recommendations.push(
                `Add MX record: Name '@', Server 'server.hostnextdns.com', Priority 10 to receive incoming bounces and support replies.`
              );
            }

            const isInboxReady = spfExists && spfIncludesHost && dmarcExists;

            res.setHeader("Content-Type", "application/json");
            res.end(
              JSON.stringify({
                success: true,
                data: {
                  domain: queryDomain,
                  spf: {
                    exists: spfExists,
                    valid: spfExists && spfIncludesHost,
                    record: spfRecord || "v=spf1 +a +mx +ip4:5.39.69.62 ~all",
                    records: [spfRecord || "v=spf1 +a +mx +ip4:5.39.69.62 ~all"],
                    isAuthorizedForHost: spfIncludesHost,
                    expectedRecord: expectedSpf,
                    status: spfExists && spfIncludesHost ? "valid" : spfExists ? "warning" : "missing",
                    message: spfExists
                      ? spfIncludesHost
                        ? "SPF is valid and authorizes mail server 5.39.69.62"
                        : "SPF exists but does not authorize server.hostnextdns.com (5.39.69.62)"
                      : "Missing SPF record. Gmail/Yahoo will route outgoing mail to SPAM.",
                  },
                  dmarc: {
                    exists: dmarcExists,
                    valid: dmarcExists,
                    record: dmarcRecord || "v=DMARC1; p=none; rua=mailto:noreply@engracedsmile.com; aspf=r;",
                    records: [dmarcRecord || "v=DMARC1; p=none; rua=mailto:noreply@engracedsmile.com; aspf=r;"],
                    expectedRecord: expectedDmarc,
                    status: dmarcExists ? "valid" : "missing",
                    message: dmarcExists
                      ? "DMARC authentication policy is active"
                      : "Missing DMARC policy on _dmarc domain. Mandatory for Google & Yahoo 2024 compliance.",
                  },
                  mx: {
                    exists: mxExists,
                    valid: mxExists,
                    records: mxRecords && mxRecords.length > 0 ? mxRecords : [{ exchange: "engracedsmile.com", priority: 0 }],
                    expectedRecord: expectedMx,
                    status: mxExists ? "valid" : "missing",
                    message: mxExists
                      ? `Found ${mxRecords.length} MX record(s)`
                      : "No MX record detected. Reverse mail checks will fail.",
                  },
                  summary: {
                    isInboxReady,
                    spamRiskLevel,
                    recommendations,
                  },
                },
              })
            );
            return;
          } catch (err: any) {
            res.setHeader("Content-Type", "application/json");
            res.statusCode = 500;
            res.end(JSON.stringify({ success: false, error: err.message || "Failed to inspect DNS" }));
            return;
          }
        }

        if (req.url === "/api/email/test-send" && req.method === "POST") {
          let body = "";
          req.on("data", (chunk: any) => (body += chunk));
          req.on("end", async () => {
            try {
              const data = JSON.parse(body || "{}");
              const { to, subject, html, text, credentials } = data;
              if (!to || !to.includes("@")) {
                res.setHeader("Content-Type", "application/json");
                res.statusCode = 400;
                res.end(JSON.stringify({ success: false, error: "A valid recipient email is required." }));
                return;
              }
              const creds = credentials || {};
              const host = (creds.host || "server.hostnextdns.com").trim();
              const port = Number(creds.port || 465);
              const secure = creds.secure !== undefined ? Boolean(creds.secure) : port === 465;
              const user = (creds.user || "noreply@engracedsmile.com").trim();
              const pass = (creds.pass || "ha;LS.fiewLkDw~x").trim();
              const fromEmail = (creds.fromEmail || creds.user || "noreply@engracedsmile.com").trim();
              const fromName = (creds.fromName || "ESDispatch Logistics").trim();

              const plainText = (text && text.trim().length > 50 && text !== subject)
                ? text.trim()
                : (html || "")
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

              const domain = fromEmail.includes("@") ? fromEmail.split("@")[1] : "engracedsmile.com";
              const randomHex = Math.random().toString(36).substring(2, 10);
              const messageId = `<${Date.now()}.${randomHex}@${domain}>`;

              const nodemailer = await import("nodemailer");
              const transporter = nodemailer.createTransport({
                host,
                port,
                secure,
                auth: { user, pass },
                tls: { rejectUnauthorized: false },
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

              res.setHeader("Content-Type", "application/json");
              res.end(
                JSON.stringify({
                  success: true,
                  message: `Email dispatched successfully to ${to}.`,
                  messageId: info.messageId,
                  accepted: info.accepted,
                  response: info.response,
                })
              );
            } catch (err: any) {
              res.setHeader("Content-Type", "application/json");
              res.statusCode = 500;
              res.end(JSON.stringify({ success: false, error: err.message || "Failed to dispatch email" }));
            }
          });
          return;
        }

        if (req.url === "/api/email/verification" && req.method === "POST") {
          let body = "";
          req.on("data", (chunk: any) => (body += chunk));
          req.on("end", async () => {
            try {
              const data = JSON.parse(body || "{}");
              const { email, name, otp: providedOtp, verificationLink } = data;
              const recipientEmail = (email || "").trim().toLowerCase();
              if (!recipientEmail || !recipientEmail.includes("@")) {
                res.setHeader("Content-Type", "application/json");
                res.statusCode = 400;
                res.end(JSON.stringify({ success: false, error: "A valid recipient email is required." }));
                return;
              }

              const recipientName = (name || "Valued Client").trim();
              const otp = (providedOtp || "").toString().trim() || Math.floor(100000 + Math.random() * 900000).toString();

              const host = "server.hostnextdns.com";
              const port = 465;
              const secure = true;
              const user = "noreply@engracedsmile.com";
              const pass = "ha;LS.fiewLkDw~x";
              const fromEmail = "noreply@engracedsmile.com";
              const fromName = "ESDispatch Logistics";

              const { renderAccountVerificationEmail, extractPlainTextFromHtml } = await import("./src/lib/emailTemplates");
              const html = renderAccountVerificationEmail({
                name: recipientName,
                otp,
                verificationLink,
                expiryMinutes: 15,
              });
              const plainText = extractPlainTextFromHtml(html);

              const domain = "engracedsmile.com";
              const randomHex = Math.random().toString(36).substring(2, 10);
              const messageId = `<verify.${Date.now()}.${randomHex}@${domain}>`;
              const subject = `Verify Your ESDispatch Account (${otp})`;

              const nodemailer = await import("nodemailer");
              const transporter = nodemailer.createTransport({
                host,
                port,
                secure,
                auth: { user, pass },
                tls: { rejectUnauthorized: false },
              });

              const info = await transporter.sendMail({
                from: `"${fromName}" <${fromEmail}>`,
                sender: fromEmail,
                replyTo: `"ESDispatch Support" <support@${domain}>`,
                to: recipientEmail,
                subject,
                text: plainText,
                html,
                messageId,
                envelope: {
                  from: fromEmail,
                  to: [recipientEmail],
                },
                headers: {
                  "X-Mailer": "ESDispatch Logistics Mailer/2026",
                  "X-Priority": "1",
                  "List-Unsubscribe": `<mailto:support@${domain}?subject=unsubscribe>, <https://www.engracedsmile.com/unsubscribe>`,
                  "List-Unsubscribe-Post": "List-Unsubscribe=One-Click",
                  "Feedback-ID": `esdispatch:security-verification:${Date.now()}`,
                  "X-Entity-Ref-ID": `${Date.now()}-${randomHex}`,
                },
              });

              res.setHeader("Content-Type", "application/json");
              res.end(
                JSON.stringify({
                  success: true,
                  message: `Verification passcode dispatched to ${recipientEmail}.`,
                  otp,
                  messageId: info.messageId,
                })
              );
            } catch (err: any) {
              res.setHeader("Content-Type", "application/json");
              res.statusCode = 500;
              res.end(JSON.stringify({ success: false, error: err.message || "Failed to dispatch verification email" }));
            }
          });
          return;
        }

        next();
      });
    },
  };
}

export default defineConfig({
  plugins: [
    react(),
    emailApiPlugin(),
  ],
  define: {
    "process.env": {},
  },
  resolve: {
    alias: [
      { find: "react-icons/fa", replacement: path.resolve(__dirname, "node_modules/react-icons/fa/index.js") },
      { find: "@", replacement: path.resolve(__dirname, "./src") },
      { find: "~", replacement: path.resolve(__dirname, "./src") },
    ],
  },
  server: {
    port: 3000,
  },
  build: {
    outDir: "dist",
  },
});
