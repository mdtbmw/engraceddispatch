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
                to: to.trim(),
                subject: subject.trim(),
                text: text || subject,
                html: html,
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
