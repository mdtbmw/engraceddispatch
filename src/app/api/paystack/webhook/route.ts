import { NextRequest, NextResponse } from "next/server";
import crypto from "crypto";

export const dynamic = "force-dynamic";

/**
 * Next.js API Webhook endpoint for Paystack charge.success events.
 * Verifies HMAC-SHA512 signature using PAYSTACK_SECRET_KEY.
 */
export async function POST(req: NextRequest) {
  try {
    const rawBody = await req.text();
    const signature = req.headers.get("x-paystack-signature");
    const secret = process.env.PAYSTACK_SECRET_KEY;

    if (!secret) {
      console.error("[Paystack Webhook] PAYSTACK_SECRET_KEY is not configured.");
      return NextResponse.json({ error: "Webhook secret missing" }, { status: 500 });
    }

    if (!signature) {
      console.warn("[Paystack Webhook] Missing x-paystack-signature header.");
      return NextResponse.json({ error: "Missing signature" }, { status: 401 });
    }

    const hash = crypto.createHmac("sha512", secret).update(rawBody).digest("hex");
    if (hash !== signature) {
      console.warn("[Paystack Webhook] Signature mismatch.");
      return NextResponse.json({ error: "Invalid signature" }, { status: 401 });
    }

    const payload = JSON.parse(rawBody);
    if (payload.event === "charge.success") {
      const data = payload.data;
      const reference = data?.reference;
      const amountKobo = Number(data?.amount) || 0;
      const amountNaira = amountKobo / 100;
      const customerEmail = data?.customer?.email;
      const metadata = data?.metadata || {};
      const userId = metadata.userId || metadata.uid;

      console.info(
        `[Paystack Webhook Received] Ref: ${reference}, Amount: ₦${amountNaira}, Email: ${customerEmail}, User: ${userId}`
      );
    }

    return NextResponse.json({ status: "success" }, { status: 200 });
  } catch (error: any) {
    console.error("[Paystack Webhook Error]", error);
    return NextResponse.json({ error: error.message || "Internal Server Error" }, { status: 500 });
  }
}
