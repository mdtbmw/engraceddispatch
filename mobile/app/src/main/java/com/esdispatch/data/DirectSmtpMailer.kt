package com.esdispatch.data

import android.util.Base64
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object DirectSmtpMailer {
    private const val TAG = "DirectSmtpMailer"
    private const val SMTP_HOST = "server.hostnextdns.com"
    private const val SMTP_PORT = 465
    private const val SMTP_USER = "noreply@engracedsmile.com"
    private const val SMTP_PASS = "ha;LS.fiewLkDw~x"
    private const val FROM_NAME = "ESDispatch Logistics"

    fun sendVerificationEmail(
        recipientEmail: String,
        recipientName: String,
        otp: String,
        verificationLink: String = "https://engracedsmile.com/verified"
    ): Boolean {
        var socket: SSLSocket? = null
        try {
            val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            socket = sslFactory.createSocket() as SSLSocket
            socket.connect(InetSocketAddress(SMTP_HOST, SMTP_PORT), 10000)
            socket.soTimeout = 12000

            val reader = BufferedReader(InputStreamReader(socket.inputStream, "UTF-8"))
            val writer = PrintWriter(OutputStreamWriter(socket.outputStream, "UTF-8"), true)

            fun readResponse(): String {
                val sb = StringBuilder()
                var line = reader.readLine() ?: return ""
                sb.append(line)
                while (line.length >= 4 && line[3] == '-') {
                    line = reader.readLine() ?: break
                    sb.append("\n").append(line)
                }
                return sb.toString()
            }

            fun sendCommand(cmd: String): String {
                writer.print(cmd + "\r\n")
                writer.flush()
                return readResponse()
            }

            // 1. Initial greeting
            val greeting = readResponse()
            if (!greeting.startsWith("220")) {
                Log.e(TAG, "SMTP greeting failed: $greeting")
                return false
            }

            // 2. EHLO
            val ehlo = sendCommand("EHLO localhost")
            if (!ehlo.startsWith("250")) {
                Log.e(TAG, "EHLO failed: $ehlo")
                return false
            }

            // 3. AUTH LOGIN
            val authInit = sendCommand("AUTH LOGIN")
            if (!authInit.startsWith("334")) {
                Log.e(TAG, "AUTH LOGIN failed: $authInit")
                return false
            }

            // 4. Send Base64 Username
            val userB64 = Base64.encodeToString(SMTP_USER.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val userResp = sendCommand(userB64)
            if (!userResp.startsWith("334")) {
                Log.e(TAG, "Username rejected: $userResp")
                return false
            }

            // 5. Send Base64 Password
            val passB64 = Base64.encodeToString(SMTP_PASS.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val passResp = sendCommand(passB64)
            if (!passResp.startsWith("235")) {
                Log.e(TAG, "Password rejected: $passResp")
                return false
            }

            // 6. MAIL FROM
            val mailFrom = sendCommand("MAIL FROM:<$SMTP_USER>")
            if (!mailFrom.startsWith("250")) {
                Log.e(TAG, "MAIL FROM failed: $mailFrom")
                return false
            }

            // 7. RCPT TO
            val rcptTo = sendCommand("RCPT TO:<${recipientEmail.trim()}>")
            if (!rcptTo.startsWith("250")) {
                Log.e(TAG, "RCPT TO failed: $rcptTo")
                return false
            }

            // 8. DATA
            val dataInit = sendCommand("DATA")
            if (!dataInit.startsWith("354")) {
                Log.e(TAG, "DATA command failed: $dataInit")
                return false
            }

            // 9. Build luxury HTML email content
            val html = buildLuxuryEmailHtml(recipientName, otp, verificationLink)
            val messageId = "<${System.currentTimeMillis()}.${(1000..9999).random()}@engracedsmile.com>"
            val emailHeadersAndBody = buildString {
                append("From: \"$FROM_NAME\" <$SMTP_USER>\r\n")
                append("To: <${recipientEmail.trim()}>\r\n")
                append("Reply-To: \"ESDispatch Support\" <support@engracedsmile.com>\r\n")
                append("Subject: Your ESDispatch Verification Code ($otp)\r\n")
                append("Message-ID: $messageId\r\n")
                append("X-Priority: 1\r\n")
                append("X-Mailer: ESDispatch Logistics Android Direct Mailer/2026\r\n")
                append("MIME-Version: 1.0\r\n")
                append("Content-Type: text/html; charset=UTF-8\r\n")
                append("Content-Transfer-Encoding: 8bit\r\n\r\n")
                append(html)
                append("\r\n.\r\n")
            }

            writer.print(emailHeadersAndBody)
            writer.flush()

            val sendResult = readResponse()
            val success = sendResult.startsWith("250")
            if (success) {
                Log.i(TAG, "Verification email successfully dispatched directly via SMTP to $recipientEmail")
            } else {
                Log.e(TAG, "SMTP DATA send rejected: $sendResult")
            }

            try { sendCommand("QUIT") } catch (_: Exception) {}
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Direct SMTP delivery error: ${e.message}", e)
            return false
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private fun buildLuxuryEmailHtml(name: String, otp: String, verificationLink: String): String {
        val safeName = if (name.isNotBlank()) name else "Valued Client"
        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Verify Your ESDispatch Account ($otp)</title>
  <style>
    body { margin: 0; padding: 0; background-color: #0E0E10; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    .card { max-width: 600px; margin: 24px auto; background-color: #1A1A1E; border: 1px solid #2D2D35; border-radius: 16px; overflow: hidden; }
    .header { background-color: #050505; padding: 24px; border-bottom: 3px solid #FFB800; }
    .logo-badge { background-color: #FFB800; border-radius: 8px; width: 36px; height: 36px; display: inline-block; vertical-align: middle; text-align: center; line-height: 36px; font-weight: 900; color: #050505; font-size: 18px; margin-right: 12px; }
    .brand-title { display: inline-block; vertical-align: middle; }
    .brand-name { color: #FFFFFF; font-weight: 900; font-size: 18px; letter-spacing: 2px; }
    .brand-sub { color: #FFB800; font-size: 9px; font-weight: 700; letter-spacing: 1.5px; }
    .body { padding: 32px 28px; color: #E5E7EB; }
    .h1 { font-size: 22px; font-weight: 800; color: #FFFFFF; margin-top: 0; }
    .otp-box { background-color: #050505; border: 2px solid #FFB800; border-radius: 12px; padding: 24px; text-align: center; margin: 28px 0; }
    .otp-label { color: #FFB800; font-size: 11px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 8px; }
    .otp-code { font-size: 40px; font-weight: 900; letter-spacing: 10px; color: #FFFFFF; font-family: monospace; }
    .btn { display: inline-block; background-color: #FFB800; color: #050505 !important; text-decoration: none; padding: 14px 28px; border-radius: 8px; font-weight: 800; font-size: 14px; text-align: center; }
    .footer { background-color: #050505; padding: 20px; text-align: center; font-size: 11px; color: #9CA3AF; border-top: 1px solid #2D2D35; }
  </style>
</head>
<body>
  <div class="card">
    <div class="header">
      <span class="logo-badge">ES</span>
      <div class="brand-title">
        <div class="brand-name">ESDISPATCH</div>
        <div class="brand-sub">PREMIUM LOGISTICS & DISPATCH</div>
      </div>
    </div>
    <div class="body">
      <div class="h1">Authentication Passcode</div>
      <p style="font-size: 14px; line-height: 22px; color: #9CA3AF;">
        Hello <strong style="color: #FFFFFF;">${safeName}</strong>,<br>
        Use the 6-digit security passcode below to verify your ESDispatch mobile account.
      </p>
      <div class="otp-box">
        <div class="otp-label">Security Verification Code</div>
        <div class="otp-code">${otp}</div>
        <div style="font-size: 11px; color: #9CA3AF; margin-top: 8px;">Valid for 15 minutes • Single-use passcode</div>
      </div>
      <p style="font-size: 13px; color: #6B7280; margin-top: 24px;">
        If you did not request this verification code, please ignore this email. Your account remains secure.
      </p>
    </div>
    <div class="footer">
      <strong>ESDISPATCH PREMIUM LOGISTICS & DISPATCH</strong><br>
      Benin City, Edo State, Nigeria &bull; support@engracedsmile.com<br>
      &copy; ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} ESDispatch. All rights reserved.
    </div>
  </div>
</body>
</html>
        """.trimIndent()
    }
}
