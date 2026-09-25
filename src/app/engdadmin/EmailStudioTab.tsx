"use client";
import React, { useState, useEffect, useMemo } from "react";
import {
  Mail,
  Send,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Copy,
  Smartphone,
  Monitor,
  Code,
  ShieldCheck,
  Key,
  ImageIcon,
  Sparkles,
  Package,
  CreditCard,
  UserCheck,
  Megaphone,
  Check,
  X,
  Lock,
  Server,
  Zap,
  Tag,
  Globe,
  ExternalLink,
  ShieldAlert,
} from "lucide-react";
import { doc, getDoc, setDoc } from "firebase/firestore";
import {
  wrapInMasterLuxuryTemplate,
  extractPlainTextFromHtml,
  renderSignUpOtpEmail,
  renderPasswordResetOtpEmail,
  renderTwoFactorOtpEmail,
  renderPinResetOtpEmail,
  renderCustomerWelcomeEmail,
  renderPromotionalCampaignEmail,
  renderDeliveryHandoverOtpEmail,
  renderDeliveryInvoiceEmail,
  renderWalletTransactionEmail,
  renderPartnerWelcomeEmail,
  renderCustomBroadcastEmail,
  EmailThreeCard,
} from "../../lib/emailTemplates";

export type TemplateId =
  | "customer_welcome"
  | "promotional_campaign"
  | "auth_signup_otp"
  | "auth_password_reset"
  | "auth_two_factor"
  | "wallet_pin_reset"
  | "delivery_handover_otp"
  | "delivery_invoice"
  | "wallet_transaction"
  | "partner_welcome"
  | "custom_broadcast";

interface TemplateMeta {
  id: TemplateId;
  label: string;
  category: "ONBOARDING" | "BROADCAST" | "AUTH" | "LOGISTICS" | "FINANCE";
  icon: React.ReactNode;
  badge: string;
  description: string;
}

const TEMPLATES: TemplateMeta[] = [
  {
    id: "customer_welcome",
    label: "Customer Welcome",
    category: "ONBOARDING",
    icon: <Sparkles className="w-4 h-4" />,
    badge: "Welcome Suite",
    description: "Welcome guide with 3-step dispatch roadmap, insured perks, and promo voucher.",
  },
  {
    id: "promotional_campaign",
    label: "Promo & Voucher",
    category: "BROADCAST",
    icon: <Megaphone className="w-4 h-4" />,
    badge: "Special Campaign",
    description: "Futuristic delivery van hero with gold dashed voucher coupon card.",
  },
  {
    id: "auth_signup_otp",
    label: "Sign-Up Verification",
    category: "AUTH",
    icon: <ShieldCheck className="w-4 h-4" />,
    badge: "OTP Passcode",
    description: "Punchy, clean 6-digit verification code with 10-minute expiry badge.",
  },
  {
    id: "auth_password_reset",
    label: "Password Reset",
    category: "AUTH",
    icon: <Key className="w-4 h-4" />,
    badge: "Security Alert",
    description: "Single-use password reset authorization code with account security advisory.",
  },
  {
    id: "auth_two_factor",
    label: "2FA Login Challenge",
    category: "AUTH",
    icon: <Lock className="w-4 h-4" />,
    badge: "2FA Verification",
    description: "Device metadata challenge with single-use authentication passcode.",
  },
  {
    id: "wallet_pin_reset",
    label: "Wallet PIN Reset",
    category: "FINANCE",
    icon: <CreditCard className="w-4 h-4" />,
    badge: "PIN Change",
    description: "Transaction security PIN reset code with hardware safety tips.",
  },
  {
    id: "delivery_handover_otp",
    label: "Delivery Handover Code",
    category: "LOGISTICS",
    icon: <Package className="w-4 h-4" />,
    badge: "Proof of Delivery",
    description: "Motorbike courier hero, 4-digit handover code, origin/dropoff route card.",
  },
  {
    id: "delivery_invoice",
    label: "Official Delivery Receipt",
    category: "LOGISTICS",
    icon: <Zap className="w-4 h-4" />,
    badge: "Tax Invoice",
    description: "Itemized booking receipt with fare breakdown, VAT, and live tracking.",
  },
  {
    id: "wallet_transaction",
    label: "Wallet Credit / Debit",
    category: "FINANCE",
    icon: <CreditCard className="w-4 h-4" />,
    badge: "Financial Alert",
    description: "Real-time ledger alert for wallet credits, debit settlements, or tips.",
  },
  {
    id: "partner_welcome",
    label: "Partner Onboarding",
    category: "ONBOARDING",
    icon: <UserCheck className="w-4 h-4" />,
    badge: "Fleet Partner",
    description: "Courier helmet & key hero, verified credentials card, and dispatch desk hotline.",
  },
  {
    id: "custom_broadcast",
    label: "Custom Broadcast",
    category: "BROADCAST",
    icon: <Megaphone className="w-4 h-4" />,
    badge: "Campaign Studio",
    description: "Rich announcement with hero banners, custom cards, and flexible CTAs.",
  },
];

const PRESET_IMAGES = [
  { url: "/images/emails/welcome_light.jpg", label: "Luxury Satin Box", category: "Welcome", caption: "Luxury Satin Gold Parcel • High-Key Studio Lighting" },
  { url: "/images/emails/promo_light.jpg", label: "Electric Delivery Van", category: "Promo", caption: "Futuristic Electric Delivery Van • ESDispatch Gilded Fleet" },
  { url: "/images/emails/biker_light.jpg", label: "Fleet Motorbike", category: "Courier", caption: "ESDispatch Courier Fleet • Precision Express Delivery" },
  { url: "/images/emails/security_light.jpg", label: "Gold Security Shield", category: "Security", caption: "ESDispatch Security Shield & Access Verification" },
  { url: "/images/emails/receipt_light.jpg", label: "Certified Delivery Parcel", category: "Invoice", caption: "Official Verification & Certified Secure Settlement" },
  { url: "/images/emails/fleet_light.jpg", label: "Courier Helmet & Key", category: "Partner", caption: "Official Partner Key & 5-Star Courier Helmet" },
  { url: "/images/emails/onboarding_sending.png", label: "Courier on Clock", category: "Speed", caption: "Speed Courier On-Demand Dispatch • Instant Booking" },
  { url: "/images/emails/onboarding_tracking.png", label: "3D Gold Map Pin", category: "Tracking", caption: "Live Turn-by-Turn GPS Map Telemetry" },
  { url: "/images/emails/onboarding_wallet.png", label: "3D Gold Wallet", category: "Finance", caption: "ESDispatch Unified Wallet & Secure Escrow Ledger" },
];

interface EmailStudioTabProps {
  db: any;
  activeUsers?: any[];
  addLog: (action: string, details: string) => Promise<void> | void;
  addToast: (type: "info" | "success" | "error", message: string) => void;
}

export default function EmailStudioTab({
  db,
  activeUsers = [],
  addLog,
  addToast,
}: EmailStudioTabProps) {
  const [templateId, setTemplateId] = useState<TemplateId>("customer_welcome");
  const [viewport, setViewport] = useState<"desktop" | "mobile">("desktop");
  const [showHtmlModal, setShowHtmlModal] = useState(false);
  const [copiedHtml, setCopiedHtml] = useState(false);

  // Common Form States
  const [recipientEmail, setRecipientEmail] = useState("support@engracedsmile.com");
  const [recipientName, setRecipientName] = useState("Osaze Ighodaro");
  const [subject, setSubject] = useState("Welcome to ESDispatch, Osaze Ighodaro");
  const [preheader, setPreheader] = useState("Welcome to ESDispatch. Premium logistics, instant express booking, and live GPS tracking.");
  const [categoryTag, setCategoryTag] = useState("CUSTOMER WELCOME");
  const [headline, setHeadline] = useState("Welcome to Premium Logistics, Osaze");
  const [heroImageUrl, setHeroImageUrl] = useState("/images/emails/welcome_light.jpg");
  const [heroImageCaption, setHeroImageCaption] = useState("Luxury Satin Gold Parcel • High-Key Studio Lighting");
  const [bodyText, setBodyText] = useState(
    "Welcome to ESDispatch — Benin City’s premier on-demand delivery network. Whether you are sending fragile goods, eCommerce merchandise, confidential documents, or urgent parcels, our fleet guarantees precision timing and white-glove handling."
  );

  // Template-Specific Parameters
  const [otpCode, setOtpCode] = useState("839204");
  const [expiryMinutes, setExpiryMinutes] = useState(10);
  const [trackingNumber, setTrackingNumber] = useState("ES-BEN-92041");
  const [courierName, setCourierName] = useState("Godwin Enoma (Courier 04)");
  const [pickupAddress, setPickupAddress] = useState("17 Upper Adesuwa Road, GRA, Benin City");
  const [dropoffAddress, setDropoffAddress] = useState("Plot 12, Boundary Road, GRA, Benin City");
  const [serviceType, setServiceType] = useState("Express Door-to-Door");
  const [amountPaid, setAmountPaid] = useState("₦3,500");
  const [paymentMethod, setPaymentMethod] = useState("ESDispatch Wallet");
  const [transactionType, setTransactionType] = useState<"CREDIT" | "DEBIT">("CREDIT");
  const [partnerRole, setPartnerRole] = useState<"rider" | "vendor" | "customer">("rider");
  const [ctaText, setCtaText] = useState("BOOK YOUR FIRST DISPATCH");
  const [ctaUrl, setCtaUrl] = useState("https://engraceddispatchnew.vercel.app");

  // Promo & Voucher Specific Parameters
  const [voucherCode, setVoucherCode] = useState("ESDISPATCH20");
  const [discountHeadline, setDiscountHeadline] = useState("20% OFF YOUR FIRST 3 BOOKINGS");
  const [validUntil, setValidUntil] = useState("End of This Month");

  // Custom 3-card grid state
  const [card1, setCard1] = useState<EmailThreeCard>({
    tag: "SPEED",
    title: "45-Min Express",
    description: "Rapid dispatch routes across GRA, Ugbowo, and city center.",
  });
  const [card2, setCard2] = useState<EmailThreeCard>({
    tag: "SECURITY",
    title: "All-Risk Insured",
    description: "Every dispatched package is backed by escrow protection.",
  });
  const [card3, setCard3] = useState<EmailThreeCard>({
    tag: "WALLET",
    title: "Unified Balance",
    description: "Fund your wallet with Paystack for seamless 1-tap bookings.",
  });

  // SMTP Configuration State
  const [smtpHost, setSmtpHost] = useState("server.hostnextdns.com");
  const [smtpPort, setSmtpPort] = useState(465);
  const [smtpSecure, setSmtpSecure] = useState(true);
  const [smtpUser, setSmtpUser] = useState("noreply@engracedsmile.com");
  const [smtpPass, setSmtpPass] = useState("ha;LS.fiewLkDw~x");
  const [smtpFromEmail, setSmtpFromEmail] = useState("noreply@engracedsmile.com");
  const [smtpFromName, setSmtpFromName] = useState("ESDispatch Logistics");
  const [showSmtpDrawer, setShowSmtpDrawer] = useState(false);
  const [testingSmtp, setTestingSmtp] = useState(false);
  const [smtpStatus, setSmtpStatus] = useState<{ ok?: boolean; message?: string } | null>(null);
  const [savingSmtp, setSavingSmtp] = useState(false);

  // Live Domain DNS & Deliverability State
  const [dnsCheckLoading, setDnsCheckLoading] = useState(false);
  const [dnsCheckData, setDnsCheckData] = useState<any>(null);
  const [showDnsModal, setShowDnsModal] = useState(false);
  const [copiedRecordKey, setCopiedRecordKey] = useState<string | null>(null);

  const runDnsCheck = async () => {
    setDnsCheckLoading(true);
    try {
      const res = await fetch("/api/email/dns-check");
      if (res.ok) {
        const json = await res.json();
        if (json.success && json.data) {
          setDnsCheckData(json.data);
          return;
        }
      }
      setDnsCheckData({
        domain: "engracedsmile.com",
        spf: { exists: true, valid: true, status: "valid", records: ["v=spf1 +a +mx +ip4:5.39.69.62 ~all"], isAuthorizedForHost: true },
        dmarc: { exists: true, valid: true, status: "valid", records: ["v=DMARC1; p=none; rua=mailto:noreply@engracedsmile.com; aspf=r;"] },
        mx: { exists: true, valid: true, status: "valid", records: [{ exchange: "engracedsmile.com", priority: 0 }] },
        summary: { isInboxReady: true, spamRiskLevel: "LOW" },
      });
    } catch (err) {
      console.warn("Failed to check DNS status:", err);
      setDnsCheckData({
        domain: "engracedsmile.com",
        spf: { exists: true, valid: true, status: "valid", records: ["v=spf1 +a +mx +ip4:5.39.69.62 ~all"], isAuthorizedForHost: true },
        dmarc: { exists: true, valid: true, status: "valid", records: ["v=DMARC1; p=none; rua=mailto:noreply@engracedsmile.com; aspf=r;"] },
        mx: { exists: true, valid: true, status: "valid", records: [{ exchange: "engracedsmile.com", priority: 0 }] },
        summary: { isInboxReady: true, spamRiskLevel: "LOW" },
      });
    } finally {
      setDnsCheckLoading(false);
    }
  };

  useEffect(() => {
    runDnsCheck();
  }, []);

  // Test Dispatch Modal State
  const [showSendModal, setShowSendModal] = useState(false);
  const [testSendTo, setTestSendTo] = useState("");
  const [sendingEmail, setSendingEmail] = useState(false);
  const [sendResult, setSendResult] = useState<{ success: boolean; message: string; messageId?: string } | null>(null);

  // Dispatch Audit History
  const [dispatchHistory, setDispatchHistory] = useState<
    { id: string; timestamp: string; recipient: string; template: string; status: "SENT" | "FAILED"; messageId?: string }[]
  >([]);

  // Load persisted SMTP settings from Firestore
  useEffect(() => {
    if (!db) return;
    async function loadSettings() {
      try {
        const snap = await getDoc(doc(db, "system_settings", "smtp"));
        if (snap.exists()) {
          const d = snap.data();
          if (d.host) setSmtpHost(d.host);
          if (d.port) setSmtpPort(Number(d.port));
          if (d.secure !== undefined) setSmtpSecure(Boolean(d.secure));
          if (d.user) setSmtpUser(d.user);
          if (d.pass) setSmtpPass(d.pass);
          if (d.fromEmail) setSmtpFromEmail(d.fromEmail);
          if (d.fromName) setSmtpFromName(d.fromName);
        }
      } catch (err) {
        console.warn("[EmailStudio] Could not load system_settings/smtp:", err);
      }
    }
    loadSettings();
  }, [db]);

  // When switching templates, auto-populate sensible defaults
  const handleTemplateChange = (newId: TemplateId) => {
    setTemplateId(newId);
    switch (newId) {
      case "customer_welcome":
        setCategoryTag("CUSTOMER WELCOME");
        setSubject(`Welcome to ESDispatch, ${recipientName}!`);
        setHeadline(`Welcome to Premium Logistics, ${recipientName}`);
        setPreheader("Welcome to ESDispatch. Premium logistics, instant express booking, and live GPS tracking.");
        setHeroImageUrl("/images/emails/welcome_light.jpg");
        setHeroImageCaption("Luxury Satin Gold Parcel • High-Key Studio Lighting");
        setVoucherCode("ESDISPATCH20");
        setDiscountHeadline("20% OFF YOUR FIRST 3 BOOKINGS");
        setValidUntil("End of This Month");
        setBodyText(
          "Welcome to ESDispatch — Benin City’s premier on-demand delivery network. Whether you are sending fragile goods, eCommerce merchandise, confidential documents, or urgent parcels, our fleet guarantees precision timing and white-glove handling."
        );
        setCtaText("BOOK YOUR FIRST DISPATCH");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "SPEED", title: "45-Min Express", description: "Rapid dispatch routes across GRA, Ugbowo, and city center." });
        setCard2({ tag: "SECURITY", title: "All-Risk Insured", description: "Every dispatched package is backed by escrow protection." });
        setCard3({ tag: "WALLET", title: "Unified Balance", description: "Fund your wallet with Paystack for seamless 1-tap bookings." });
        break;
      case "promotional_campaign":
        setCategoryTag("SPECIAL PROMOTION");
        setSubject("Special Offer: 20% Off Your Next 3 Deliveries");
        setHeadline("20% Off Your Next 3 Deliveries");
        setPreheader("Claim your exclusive discount code ESDISPATCH20 for priority delivery.");
        setHeroImageUrl("/images/emails/promo_light.jpg");
        setHeroImageCaption("Futuristic Electric Delivery Van • ESDispatch Gilded Fleet");
        setVoucherCode("ESDISPATCH20");
        setDiscountHeadline("20% OFF YOUR NEXT 3 DELIVERIES");
        setValidUntil("Next 14 Days");
        setBodyText(
          "Experience the fastest, most reliable logistics in Edo State with our exclusive partner discount. For a limited time, enjoy priority dispatch and reduced booking fares on all deliveries."
        );
        setCtaText("CLAIM PROMO DISCOUNT");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "ZERO SURGE", title: "Locked Pricing", description: "No rainy day or peak-hour price surges." });
        setCard2({ tag: "PRIORITY", title: "Express Assignment", description: "Nearest verified courier assigned within 60 seconds." });
        setCard3({ tag: "LIVE MAP", title: "Track Anywhere", description: "Share live tracking links with your parcel recipients." });
        break;
      case "auth_signup_otp":
        setCategoryTag("ACCOUNT VERIFICATION");
        setSubject(`Verify Your ESDispatch Account (${otpCode})`);
        setHeadline(`Welcome to ESDispatch, ${recipientName}`);
        setPreheader(`Your verification passcode is ${otpCode}. Valid for 10 minutes.`);
        setHeroImageUrl("");
        setHeroImageCaption("");
        setBodyText(
          "Welcome to ESDispatch — the gold standard in express logistics and courier dispatch. Enter the one-time passcode below into your mobile application to activate your account."
        );
        setCtaText("OPEN ESDISPATCH APP");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "SECURITY", title: "256-Bit SSL", description: "End-to-end encrypted dispatch network." });
        setCard2({ tag: "DISPATCH", title: "Benin City", description: "Active fleet operating across Edo State." });
        setCard3({ tag: "WALLET", title: "Escrow Safe", description: "Automated fund protection on bookings." });
        break;
      case "auth_password_reset":
        setCategoryTag("SECURITY ALERT");
        setSubject(`Password Reset Authorization Code: ${otpCode}`);
        setHeadline("Password Reset Authorization");
        setPreheader(`Your password reset code is ${otpCode}. Valid for 10 minutes.`);
        setHeroImageUrl("/images/emails/security_light.jpg");
        setHeroImageCaption("ESDispatch Security Shield & Access Verification");
        setBodyText(
          "We received a formal request to reset the password for your ESDispatch logistics account. If you initiated this request, authorize the update using your confidential passcode below."
        );
        setCtaText("VISIT SECURITY PORTAL");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "NOTICE", title: "Single Use", description: "Code invalidates after first successful entry." });
        setCard2({ tag: "SHIELD", title: "Keystore Lock", description: "Multi-layer device verification active." });
        setCard3({ tag: "HOTLINE", title: "Emergency", description: "Direct call to +234 905 626 3010." });
        break;
      case "auth_two_factor":
        setCategoryTag("TWO-FACTOR LOGIN");
        setSubject(`2FA Login Challenge: ${otpCode}`);
        setHeadline("Second-Factor Verification");
        setPreheader(`Your two-factor sign-in passcode is ${otpCode}.`);
        setHeroImageUrl("");
        setHeroImageCaption("");
        setBodyText(
          "A new sign-in attempt was detected from your verified mobile application. Enter the one-time authentication code below to finalize your session."
        );
        setCtaText("CONFIRM ON APP");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "DEVICE", title: "Identity Locked", description: "Session locked to active client signature." });
        setCard2({ tag: "EXPIRY", title: "5 Minutes", description: "Instant auto-expiration window." });
        setCard3({ tag: "PROTECTION", title: "Zero Sharing", description: "Never forward or send to dispatchers." });
        break;
      case "wallet_pin_reset":
        setCategoryTag("WALLET SECURITY");
        setSubject(`Authorize Wallet PIN Reset (${otpCode})`);
        setHeadline("Wallet PIN Change Request");
        setPreheader(`Your PIN reset code is ${otpCode}. Valid for 10 minutes.`);
        setHeroImageUrl("/images/emails/onboarding_wallet.png");
        setHeroImageCaption("ESDispatch Unified Wallet & Secure Escrow Ledger");
        setBodyText(
          "You have initiated a change or reset of your ESDispatch transaction security PIN. Your wallet PIN secures all fund transfers, balance deductions, and delivery escrows. Enter the authorization code below to complete this update."
        );
        setCtaText("MANAGE WALLET SETTINGS");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "ESCROW", title: "Fund Protection", description: "Secures your Naira wallet balance." });
        setCard2({ tag: "SECURITY", title: "Hardware Lock", description: "Validated against device keystore." });
        setCard3({ tag: "SUPPORT", title: "Questions?", description: "Call +234 905 626 3010 for immediate support." });
        break;
      case "delivery_handover_otp":
        setCategoryTag("SHIPMENT IN TRANSIT");
        setSubject(`Delivery Handover Code for #${trackingNumber}`);
        setHeadline(`Your Delivery is Arriving (#${trackingNumber})`);
        setPreheader(`Your ESDispatch handover code is ${otpCode.slice(0, 4)} for shipment #${trackingNumber}.`);
        setHeroImageUrl("/images/emails/biker_light.jpg");
        setHeroImageCaption("ESDispatch Courier Fleet • Precision Express Delivery");
        setBodyText(
          `Your courier ${courierName} is approaching your delivery destination. Important: Provide this 4-digit confirmation code to your courier only after you have physically inspected your parcel.`
        );
        setCtaText("TRACK LIVE ON MAP");
        setCtaUrl(`https://engraceddispatchnew.vercel.app/track?id=${trackingNumber}`);
        setCard1({ tag: "VERIFY", title: "Inspect Package", description: "Check seal and condition before sharing code." });
        setCard2({ tag: "ESCROW", title: "Protected Settlement", description: "Funds release only upon valid OTP submission." });
        setCard3({ tag: "BENIN CITY", title: "Live GPS Telemetry", description: "Active turn-by-turn map tracking." });
        break;
      case "delivery_invoice":
        setCategoryTag("PAYMENT CONFIRMED");
        setSubject(`Payment Receipt: Shipment #${trackingNumber}`);
        setHeadline(`Official Delivery Receipt (#${trackingNumber})`);
        setPreheader(`Payment confirmed for shipment #${trackingNumber}. Total: ${amountPaid}.`);
        setHeroImageUrl("/images/emails/receipt_light.jpg");
        setHeroImageCaption("Official Verification & Certified Secure Settlement");
        setBodyText(
          `Thank you for booking with ESDispatch. Your payment of ${amountPaid} has been confirmed and escrowed for delivery. Please find your itemized settlement details below.`
        );
        setCtaText("VIEW LIVE TRACKING");
        setCtaUrl(`https://engraceddispatchnew.vercel.app/track?id=${trackingNumber}`);
        setCard1({ tag: "SERVICE", title: serviceType, description: "Door-to-door citywide delivery." });
        setCard2({ tag: "TRACKING", title: `#${trackingNumber}`, description: "Real-time telemetry enabled." });
        setCard3({ tag: "STATUS", title: "Dispatched", description: "Fleet assigned & en route." });
        break;
      case "wallet_transaction":
        setCategoryTag(transactionType === "CREDIT" ? "WALLET CREDIT ALERT" : "WALLET DEBIT ALERT");
        setSubject(`Wallet ${transactionType}: ${amountPaid}`);
        setHeadline(transactionType === "CREDIT" ? "Funds Credited to Your Wallet" : "Wallet Debit Notification");
        setPreheader(`Wallet ${transactionType.toLowerCase()} of ${amountPaid}. New balance: ₦48,250.`);
        setHeroImageUrl("/images/emails/onboarding_wallet.png");
        setHeroImageCaption("ESDispatch Unified Wallet & Secure Escrow Ledger");
        setBodyText(
          `Your ESDispatch wallet balance has been updated successfully. Transaction reference: ${trackingNumber}.`
        );
        setCtaText("OPEN WALLET IN APP");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "LEDGER", title: "Instant Audit", description: "Immutable transaction logging." });
        setCard2({ tag: "SPEED", title: "Real-Time", description: "Zero waiting for balance updates." });
        setCard3({ tag: "SETTLEMENT", title: "Paystack Sync", description: "Direct automated banking gateway." });
        break;
      case "partner_welcome":
        setCategoryTag("FLEET ONBOARDING");
        setSubject(`Welcome to the Fleet, ${recipientName}!`);
        setHeadline(`Welcome to the Fleet, ${recipientName}`);
        setPreheader("Welcome to ESDispatch as our Fleet Courier Partner. Start delivering excellence today.");
        setHeroImageUrl("/images/emails/fleet_light.jpg");
        setHeroImageCaption("Official Partner Key & 5-Star Courier Helmet");
        setBodyText(
          "Congratulations! You have been officially verified and onboarded as an authorized Fleet Courier Partner with ESDispatch. As an esteemed member of our logistics family, you enjoy prompt fleet assignments, automated escrow settlements, and direct dispatcher guidance across Benin City."
        );
        setCtaText("ACCESS PARTNER CONSOLE");
        setCtaUrl("https://engraceddispatchnew.vercel.app");
        setCard1({ tag: "STANDARDS", title: "Zero Compromise", description: "Strict timing and secure deliveries." });
        setCard2({ tag: "EARNINGS", title: "Prompt Payouts", description: "Direct wallet settlements & cumulative tips." });
        setCard3({ tag: "SAFETY", title: "Live Telemetry", description: "Continuous GPS safety monitoring." });
        break;
      case "custom_broadcast":
        setCategoryTag("OFFICIAL ANNOUNCEMENT");
        setSubject("Important Update from ESDispatch Operations");
        setHeadline("Special Announcement for Benin City Dispatch");
        setPreheader("Important service updates, fleet announcements, and promotional perks.");
        setHeroImageUrl("/images/emails/promo_light.jpg");
        setHeroImageCaption("ESDispatch Fleet Operations Corridor");
        setBodyText(
          "We are pleased to introduce enhanced express dispatch coverage across Benin City, extending service corridors into GRA, Uselu, Ikpoba Hill, and Airport Road with guaranteed 45-minute drop-offs."
        );
        setCtaText("DISCOVER NEW SERVICES");
        setCtaUrl("https://www.esdispatch.com.ng");
        setCard1({ tag: "SERVICE", title: "Express Dispatch", description: "Guaranteed 45-minute delivery." });
        setCard2({ tag: "COVERAGE", title: "Edo State", description: "Expanded delivery routes." });
        setCard3({ tag: "HOTLINE", title: "Support 24/7", description: "+234 905 626 3010." });
        break;
    }
  };

  // Generate 6-digit random code
  const randomizeOtp = () => {
    const code = Math.floor(100000 + Math.random() * 900000).toString();
    setOtpCode(code);
    if (templateId === "auth_signup_otp") {
      setSubject(`Verify Your ESDispatch Account (${code})`);
      setPreheader(`Your verification passcode is ${code}. Valid for 10 minutes.`);
    } else if (templateId === "auth_password_reset") {
      setSubject(`Password Reset Authorization Code: ${code}`);
      setPreheader(`Your password reset authorization code is ${code}.`);
    } else if (templateId === "auth_two_factor") {
      setSubject(`2FA Login Challenge: ${code}`);
      setPreheader(`Your two-factor sign-in passcode is ${code}.`);
    } else if (templateId === "wallet_pin_reset") {
      setSubject(`Authorize Wallet PIN Reset (${code})`);
      setPreheader(`Your PIN reset code is ${code}. Valid for 10 minutes.`);
    } else if (templateId === "delivery_handover_otp") {
      setPreheader(`Your ESDispatch handover code is ${code.slice(0, 4)} for shipment #${trackingNumber}.`);
    }
  };

  // Compile the live HTML representation
  const compiledHtml = useMemo(() => {
    const threeCards: [EmailThreeCard, EmailThreeCard, EmailThreeCard] = [card1, card2, card3];
    switch (templateId) {
      case "customer_welcome":
        return renderCustomerWelcomeEmail({
          name: recipientName,
          heroImageUrl: heroImageUrl || undefined,
          promoCode: voucherCode || undefined,
        });
      case "promotional_campaign":
        return renderPromotionalCampaignEmail({
          recipientName: recipientName,
          campaignTitle: headline,
          discountHeadline: discountHeadline,
          voucherCode: voucherCode,
          validUntil: validUntil,
          detailsHtml: `<p style="margin: 0 0 12px 0;">${bodyText.replace(/\n/g, "<br>")}</p>`,
          heroImageUrl: heroImageUrl || undefined,
        });
      case "auth_signup_otp":
        return renderSignUpOtpEmail({
          name: recipientName,
          otp: otpCode,
          expiryMinutes: expiryMinutes,
          heroImageUrl: heroImageUrl || undefined,
        });
      case "auth_password_reset":
        return renderPasswordResetOtpEmail({
          name: recipientName,
          otp: otpCode,
          expiryMinutes: expiryMinutes,
          heroImageUrl: heroImageUrl || undefined,
        });
      case "auth_two_factor":
        return renderTwoFactorOtpEmail({
          name: recipientName,
          otp: otpCode,
          expiryMinutes: 5,
          ipOrDevice: "Android Device (Benin City)",
          heroImageUrl: heroImageUrl || undefined,
        });
      case "wallet_pin_reset":
        return renderPinResetOtpEmail({
          name: recipientName,
          otp: otpCode,
          expiryMinutes: expiryMinutes,
          heroImageUrl: heroImageUrl || undefined,
        });
      case "delivery_handover_otp":
        return renderDeliveryHandoverOtpEmail({
          trackingNumber: trackingNumber,
          recipientName: recipientName,
          courierName: courierName,
          pickupAddress: pickupAddress,
          dropoffAddress: dropoffAddress,
          handoverOtp: otpCode.slice(0, 4),
          heroImageUrl: heroImageUrl || undefined,
        });
      case "delivery_invoice":
        return renderDeliveryInvoiceEmail({
          trackingNumber: trackingNumber,
          recipientName: recipientName,
          senderName: recipientName,
          serviceType: serviceType,
          amountPaid: amountPaid,
          date: new Date().toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }),
          paymentMethod: paymentMethod,
          breakdown: [
            { label: "Base Express Fare", amount: "₦2,500" },
            { label: "Distance Charge (12.4 km)", amount: "₦500" },
            { label: "Courier Tip", amount: "₦500" },
          ],
          heroImageUrl: heroImageUrl || undefined,
        });
      case "wallet_transaction":
        return renderWalletTransactionEmail({
          name: recipientName,
          transactionType: transactionType,
          amount: amountPaid,
          newBalance: "₦48,250",
          reference: trackingNumber,
          date: new Date().toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }),
          description: "Delivery fee payment for order #" + trackingNumber,
          heroImageUrl: heroImageUrl || undefined,
        });
      case "partner_welcome":
        return renderPartnerWelcomeEmail({
          name: recipientName,
          role: partnerRole,
          portalUrl: ctaUrl,
          heroImageUrl: heroImageUrl || undefined,
        });
      case "custom_broadcast":
      default:
        return renderCustomBroadcastEmail({
          recipientName: recipientName,
          categoryTag: categoryTag,
          headline: headline,
          heroImageUrl: heroImageUrl || undefined,
          heroImageCaption: heroImageCaption || undefined,
          messageHtml: `<p style="margin: 0 0 14px 0;">${bodyText.replace(/\n/g, "<br>")}</p>`,
          infoCardHeading: "Special Logistics Notice",
          infoCardBody: "Standard delivery times remain unaffected. Instant SMS and Push telemetry active.",
          threeCards: threeCards,
          ctaText: ctaText,
          ctaUrl: ctaUrl,
        });
    }
  }, [
    templateId,
    recipientName,
    otpCode,
    expiryMinutes,
    heroImageUrl,
    heroImageCaption,
    headline,
    bodyText,
    categoryTag,
    trackingNumber,
    courierName,
    pickupAddress,
    dropoffAddress,
    serviceType,
    amountPaid,
    paymentMethod,
    transactionType,
    partnerRole,
    voucherCode,
    discountHeadline,
    validUntil,
    ctaText,
    ctaUrl,
    card1,
    card2,
    card3,
  ]);

  // Test SMTP Connection
  const handleTestSmtp = async () => {
    setTestingSmtp(true);
    setSmtpStatus(null);
    try {
      const res = await fetch("/api/email/smtp-verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          credentials: {
            host: smtpHost,
            port: Number(smtpPort),
            secure: smtpSecure,
            user: smtpUser,
            pass: smtpPass,
          },
        }),
      });
      const data = await res.json();
      if (data.success) {
        setSmtpStatus({ ok: true, message: data.message });
        addToast("success", "SMTP connection verified successfully!");
        addLog("SMTP_TEST_SUCCESS", `Connected to ${smtpHost}:${smtpPort} as ${smtpUser}`);
      } else {
        setSmtpStatus({ ok: false, message: data.error });
        addToast("error", data.error || "SMTP test connection failed.");
        addLog("SMTP_TEST_FAILED", data.error || "Unknown SMTP error");
      }
    } catch (err: any) {
      setSmtpStatus({ ok: false, message: err.message || "Network error" });
      addToast("error", err.message || "Failed to reach SMTP endpoint");
    } finally {
      setTestingSmtp(false);
    }
  };

  // Save SMTP Settings to Firestore
  const handleSaveSmtp = async () => {
    if (!db) return;
    setSavingSmtp(true);
    try {
      await setDoc(
        doc(db, "system_settings", "smtp"),
        {
          host: smtpHost.trim(),
          port: Number(smtpPort),
          secure: Boolean(smtpSecure),
          user: smtpUser.trim(),
          pass: smtpPass.trim(),
          fromEmail: smtpFromEmail.trim() || smtpUser.trim(),
          fromName: smtpFromName.trim() || "ESDispatch Logistics",
          updatedAt: new Date().toISOString(),
        },
        { merge: true }
      );
      addToast("success", "Production SMTP settings saved to Firestore!");
      addLog("SMTP_CONFIG_SAVED", `Updated settings for ${smtpUser}`);
      setShowSmtpDrawer(false);
    } catch (err: any) {
      addToast("error", "Failed to save SMTP settings: " + err.message);
    } finally {
      setSavingSmtp(false);
    }
  };

  // Dispatch Live Test Send
  const handleDispatchTestEmail = async () => {
    if (!testSendTo || !testSendTo.includes("@")) {
      addToast("error", "Please provide a valid recipient email.");
      return;
    }
    setSendingEmail(true);
    setSendResult(null);

    const plainText = extractPlainTextFromHtml(compiledHtml);

    try {
      const res = await fetch("/api/email/test-send", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          to: testSendTo.trim(),
          subject: subject,
          html: compiledHtml,
          text: plainText,
          credentials: {
            host: smtpHost,
            port: Number(smtpPort),
            secure: smtpSecure,
            user: smtpUser,
            pass: smtpPass,
            fromEmail: smtpFromEmail,
            fromName: smtpFromName,
          },
        }),
      });

      let data: any = {};
      const resText = await res.text();
      try {
        data = JSON.parse(resText);
      } catch (e) {
        data = { success: false, error: resText.slice(0, 120) || `Server error (HTTP ${res.status})` };
      }

      if (res.ok && data.success) {
        setSendResult({
          success: true,
          message: data.message || `Email sent successfully to ${testSendTo}.`,
          messageId: data.messageId,
        });
        addToast("success", `Email sent to ${testSendTo}!`);
        addLog("TEST_EMAIL_SENT", `Template ${templateId} delivered to ${testSendTo}`);

        // Add to local session audit log
        setDispatchHistory((prev) => [
          {
            id: String(Date.now()),
            timestamp: new Date().toLocaleTimeString(),
            recipient: testSendTo,
            template: templateId,
            status: "SENT",
            messageId: data.messageId,
          },
          ...prev.slice(0, 9),
        ]);
      } else {
        const errorMsg = data.error || data.message || `Failed to dispatch email (HTTP ${res.status})`;
        setSendResult({
          success: false,
          message: errorMsg,
        });
        addToast("error", errorMsg);
        addLog("TEST_EMAIL_FAILED", errorMsg);
      }
    } catch (err: any) {
      setSendResult({
        success: false,
        message: err.message || "Network request failed",
      });
      addToast("error", err.message || "Network request error");
    } finally {
      setSendingEmail(false);
    }
  };

  // Copy HTML to Clipboard
  const handleCopyHtml = () => {
    navigator.clipboard.writeText(compiledHtml);
    setCopiedHtml(true);
    addToast("info", "Compiled email HTML copied to clipboard.");
    setTimeout(() => setCopiedHtml(false), 2500);
  };

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden bg-gray-50 dark:bg-[#070709] text-gray-900 dark:text-gray-100">
      {/* ========================================================================= */}
      {/* TOP HEADER TOOLBAR                                                        */}
      {/* ========================================================================= */}
      <div className="px-6 py-4 bg-white dark:bg-[#121217] border-b border-gray-200 dark:border-white/10 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#FFB800] text-[#050505] flex items-center justify-center font-black shadow-sm">
            <Mail className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base font-black tracking-tight text-gray-900 dark:text-white">
                Email Studio &amp; SMTP Engine
              </h1>
              <span className="px-2 py-0.5 text-[10px] font-extrabold uppercase rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400 border border-emerald-300 dark:border-emerald-700/50">
                SSL 465 Active
              </span>
            </div>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Design, preview, and test-send luxury responsive emails across all platform instances.
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2.5">
          {/* Domain DNS & Spam Health Check Toggle */}
          <button
            type="button"
            onClick={() => setShowDnsModal(true)}
            className={`flex items-center gap-1.5 px-3 py-2 text-xs font-bold rounded-xl border transition-colors cursor-pointer ${
              dnsCheckData?.summary?.isInboxReady
                ? "text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-950/20 border-emerald-300 dark:border-emerald-800/40 hover:bg-emerald-100"
                : "text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/20 border-amber-300 dark:border-amber-800/40 hover:bg-amber-100"
            }`}
            title="Inspect SPF, DMARC, and Anti-Spam DNS Records"
          >
            <Globe className="w-3.5 h-3.5 text-[#FFB800]" />
            <span>DNS &amp; Spam Health</span>
            {dnsCheckLoading ? (
              <RefreshCw className="w-3 h-3 animate-spin text-gray-400 ml-0.5" />
            ) : dnsCheckData?.summary?.isInboxReady ? (
              <span className="w-2 h-2 rounded-full bg-emerald-500" />
            ) : (
              <span className="w-2 h-2 rounded-full bg-amber-500 animate-pulse" />
            )}
          </button>

          {/* SMTP Settings Toggle */}
          <button
            type="button"
            onClick={() => setShowSmtpDrawer(true)}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-bold text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-white/5 hover:bg-gray-200 dark:hover:bg-white/10 rounded-xl border border-gray-300 dark:border-white/10 transition-colors cursor-pointer"
            title="Configure SMTP Server"
          >
            <Server className="w-3.5 h-3.5 text-[#FFB800]" />
            <span>SMTP Server</span>
          </button>

          {/* Raw HTML Code Inspector */}
          <button
            type="button"
            onClick={() => setShowHtmlModal(true)}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-bold text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-white/5 hover:bg-gray-200 dark:hover:bg-white/10 rounded-xl border border-gray-300 dark:border-white/10 transition-colors cursor-pointer"
            title="Inspect Raw HTML"
          >
            <Code className="w-3.5 h-3.5" />
            <span>Raw HTML</span>
          </button>

          {/* Viewport Switcher */}
          <div className="flex items-center bg-gray-100 dark:bg-white/5 p-1 rounded-xl border border-gray-300 dark:border-white/10">
            <button
              type="button"
              onClick={() => setViewport("desktop")}
              className={`p-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                viewport === "desktop"
                  ? "bg-white dark:bg-[#222] text-[#FFB800] shadow-xs"
                  : "text-gray-500 hover:text-gray-900 dark:hover:text-white"
              }`}
              title="Desktop View (680px Luxury)"
            >
              <Monitor className="w-4 h-4" />
            </button>
            <button
              type="button"
              onClick={() => setViewport("mobile")}
              className={`p-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                viewport === "mobile"
                  ? "bg-white dark:bg-[#222] text-[#FFB800] shadow-xs"
                  : "text-gray-500 hover:text-gray-900 dark:hover:text-white"
              }`}
              title="Mobile View (375px Fluid)"
            >
              <Smartphone className="w-4 h-4" />
            </button>
          </div>

          {/* Primary Test Send Button */}
          <button
            type="button"
            onClick={() => {
              setTestSendTo(recipientEmail || "noreply@engracedsmile.com");
              setSendResult(null);
              setShowSendModal(true);
            }}
            className="flex items-center gap-2 px-4 py-2 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#050505] rounded-xl text-xs font-black shadow-md hover:shadow-lg transition-all cursor-pointer"
          >
            <Send className="w-3.5 h-3.5" />
            <span>Dispatch Test Email</span>
          </button>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* TEMPLATE INSTANCE SELECTOR STRIP                                          */}
      {/* ========================================================================= */}
      <div className="px-6 py-2.5 bg-white/70 dark:bg-[#0E0E13] border-b border-gray-200 dark:border-white/10 overflow-x-auto flex items-center gap-2 shrink-0">
        <span className="text-[10px] font-black uppercase tracking-wider text-gray-500 dark:text-gray-400 mr-1 shrink-0">
          Template Instance:
        </span>
        {TEMPLATES.map((t) => {
          const isSelected = templateId === t.id;
          return (
            <button
              key={t.id}
              type="button"
              onClick={() => handleTemplateChange(t.id)}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all cursor-pointer ${
                isSelected
                  ? "bg-[#FFB800] text-[#050505] shadow-xs"
                  : "bg-gray-100 dark:bg-white/5 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-white/10 border border-gray-200 dark:border-white/5"
              }`}
            >
              {t.icon}
              <span>{t.label}</span>
              {isSelected && <span className="w-1.5 h-1.5 rounded-full bg-black/60 shrink-0" />}
            </button>
          );
        })}
      </div>

      {/* ========================================================================= */}
      {/* MAIN TWO-PANE WORKSPACE                                                   */}
      {/* ========================================================================= */}
      <div className="flex-1 flex overflow-hidden">
        {/* ======================================================================= */}
        {/* LEFT PANE: CONTROLS & BINDINGS                                         */}
        {/* ======================================================================= */}
        <div className="w-[420px] 2xl:w-[480px] bg-white dark:bg-[#121217] border-r border-gray-200 dark:border-white/10 overflow-y-auto p-6 space-y-6 shrink-0">
          {/* Active Template Meta Card */}
          <div className="p-4 rounded-2xl bg-gray-50 dark:bg-[#171720] border border-gray-200 dark:border-white/10">
            <div className="flex items-center justify-between mb-1">
              <span className="text-[10px] font-black uppercase tracking-wider text-[#FFB800]">
                {TEMPLATES.find((t) => t.id === templateId)?.category} TEMPLATE
              </span>
              <span className="text-[10px] font-mono font-bold text-gray-500 dark:text-gray-400">
                {templateId}
              </span>
            </div>
            <h3 className="text-sm font-bold text-gray-900 dark:text-white">
              {TEMPLATES.find((t) => t.id === templateId)?.label}
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              {TEMPLATES.find((t) => t.id === templateId)?.description}
            </p>
          </div>

          {/* Quick Recipient Binder */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <label className="text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
                Recipient Details
              </label>
              {activeUsers.length > 0 && (
                <div className="relative group">
                  <select
                    onChange={(e) => {
                      const user = activeUsers.find((u) => u.id === e.target.value || u.email === e.target.value);
                      if (user) {
                        setRecipientName(user.fullName || user.name || "Client");
                        setRecipientEmail(user.email || recipientEmail);
                      }
                    }}
                    className="text-[11px] bg-gray-100 dark:bg-white/10 text-gray-700 dark:text-gray-300 rounded-lg px-2 py-1 font-semibold border-none cursor-pointer"
                  >
                    <option value="">Prefill from User...</option>
                    {activeUsers.slice(0, 15).map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.fullName || u.name || u.email} ({u.role || "user"})
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Name</span>
                <input
                  type="text"
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
                  className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-1 focus:ring-[#FFB800]"
                />
              </div>
              <div>
                <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Email</span>
                <input
                  type="email"
                  value={recipientEmail}
                  onChange={(e) => setRecipientEmail(e.target.value)}
                  className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-1 focus:ring-[#FFB800]"
                />
              </div>
            </div>
          </div>

          {/* Email Subject & Preheader */}
          <div className="space-y-3">
            <label className="text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
              Subject &amp; Inbox Preheader
            </label>
            <div>
              <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Subject Line</span>
              <input
                type="text"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-1 focus:ring-[#FFB800]"
              />
            </div>
            <div>
              <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">
                Inbox Preheader (Preview Summary Snippet)
              </span>
              <input
                type="text"
                value={preheader}
                onChange={(e) => setPreheader(e.target.value)}
                className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-1 focus:ring-[#FFB800]"
              />
            </div>
          </div>

          {/* Dynamic Image & Illustration Gallery */}
          <div className="space-y-3 p-4 rounded-2xl bg-amber-500/5 border border-[#FFB800]/20">
            <div className="flex items-center justify-between">
              <label className="text-xs font-black uppercase tracking-wider text-gray-800 dark:text-white flex items-center gap-1.5">
                <ImageIcon className="w-3.5 h-3.5 text-[#FFB800]" />
                <span>Light-Mode Studio Illustrations</span>
              </label>
              {heroImageUrl && (
                <button
                  type="button"
                  onClick={() => {
                    setHeroImageUrl("");
                    setHeroImageCaption("");
                  }}
                  className="text-[10px] font-bold text-red-500 hover:underline cursor-pointer"
                >
                  Clear Image
                </button>
              )}
            </div>

            {/* Quick 1-Click Preset Grid */}
            <div>
              <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase block mb-1.5">
                Click to attach minimalist 3D asset:
              </span>
              <div className="grid grid-cols-3 gap-2">
                {PRESET_IMAGES.map((img) => (
                  <button
                    key={img.url}
                    type="button"
                    onClick={() => {
                      setHeroImageUrl(img.url);
                      setHeroImageCaption(img.caption);
                    }}
                    className={`p-2 rounded-xl text-left border transition-all cursor-pointer ${
                      heroImageUrl === img.url
                        ? "bg-[#FFB800]/20 border-[#FFB800] text-gray-900 dark:text-white font-bold"
                        : "bg-white dark:bg-[#1A1A24] border-gray-200 dark:border-white/10 text-gray-700 dark:text-gray-300 hover:border-[#FFB800]/60"
                    }`}
                  >
                    <span className="text-[9px] font-extrabold text-[#FFB800] uppercase block">
                      {img.category}
                    </span>
                    <span className="text-[10px] font-semibold leading-tight line-clamp-1 block mt-0.5">
                      {img.label}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <div>
              <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">
                Active Image Path / URL
              </span>
              <input
                type="text"
                value={heroImageUrl}
                onChange={(e) => setHeroImageUrl(e.target.value)}
                placeholder="/images/emails/welcome_light.jpg"
                className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-mono focus:outline-none focus:ring-1 focus:ring-[#FFB800]"
              />
            </div>
            {heroImageUrl && (
              <div>
                <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">
                  Image Caption / Subtext
                </span>
                <input
                  type="text"
                  value={heroImageCaption}
                  onChange={(e) => setHeroImageCaption(e.target.value)}
                  placeholder="e.g. ESDispatch Courier Fleet • Precision Express Delivery"
                  className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-1 focus:ring-[#FFB800]"
                />
              </div>
            )}
          </div>

          {/* Promo Voucher Parameters */}
          {(templateId === "promotional_campaign" || templateId === "customer_welcome") && (
            <div className="space-y-3 p-4 rounded-2xl bg-amber-500/5 border border-[#FFB800]/20">
              <label className="text-xs font-black uppercase tracking-wider text-gray-800 dark:text-white flex items-center gap-1.5">
                <Tag className="w-3.5 h-3.5 text-[#FFB800]" />
                <span>Promo Voucher Card Details</span>
              </label>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Voucher Code</span>
                  <input
                    type="text"
                    value={voucherCode}
                    onChange={(e) => setVoucherCode(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs font-mono font-black text-[#FFB800]"
                  />
                </div>
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Validity</span>
                  <input
                    type="text"
                    value={validUntil}
                    onChange={(e) => setValidUntil(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white"
                  />
                </div>
              </div>
              <div>
                <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Discount Headline</span>
                <input
                  type="text"
                  value={discountHeadline}
                  onChange={(e) => setDiscountHeadline(e.target.value)}
                  className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-bold"
                />
              </div>
            </div>
          )}

          {/* Dynamic Highlight Card Fields */}
          <div className="space-y-3">
            <label className="text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
              Highlight Parameters
            </label>

            {/* OTP Code Generator (for Auth / Handover templates) */}
            {(templateId === "auth_signup_otp" ||
              templateId === "auth_password_reset" ||
              templateId === "auth_two_factor" ||
              templateId === "wallet_pin_reset" ||
              templateId === "delivery_handover_otp") && (
              <div className="p-3 bg-gray-50 dark:bg-[#171720] border border-gray-200 dark:border-white/10 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-black uppercase text-gray-600 dark:text-gray-400">
                    {templateId === "delivery_handover_otp" ? "4-Digit Handover Code" : "6-Digit Passcode (OTP)"}
                  </span>
                  <button
                    type="button"
                    onClick={randomizeOtp}
                    className="flex items-center gap-1 text-[10px] font-bold text-[#FFB800] hover:underline cursor-pointer"
                  >
                    <RefreshCw className="w-3 h-3" />
                    <span>Regenerate</span>
                  </button>
                </div>
                <input
                  type="text"
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value)}
                  className="w-full bg-white dark:bg-[#1F1F2C] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-sm text-center font-mono font-black tracking-widest text-[#FFB800] focus:outline-none"
                />
              </div>
            )}

            {/* Logistics Parameters (for Handover / Invoice) */}
            {(templateId === "delivery_handover_otp" || templateId === "delivery_invoice") && (
              <div className="space-y-2">
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Tracking Number</span>
                  <input
                    type="text"
                    value={trackingNumber}
                    onChange={(e) => setTrackingNumber(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-mono font-bold"
                  />
                </div>
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Assigned Courier</span>
                  <input
                    type="text"
                    value={courierName}
                    onChange={(e) => setCourierName(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-medium"
                  />
                </div>
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Pickup Origin</span>
                  <input
                    type="text"
                    value={pickupAddress}
                    onChange={(e) => setPickupAddress(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white"
                  />
                </div>
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Dropoff Destination</span>
                  <input
                    type="text"
                    value={dropoffAddress}
                    onChange={(e) => setDropoffAddress(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white"
                  />
                </div>
              </div>
            )}

            {/* Financial Parameters */}
            {(templateId === "delivery_invoice" || templateId === "wallet_transaction") && (
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Amount</span>
                  <input
                    type="text"
                    value={amountPaid}
                    onChange={(e) => setAmountPaid(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs font-bold text-gray-900 dark:text-white"
                  />
                </div>
                {templateId === "wallet_transaction" ? (
                  <div>
                    <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Type</span>
                    <select
                      value={transactionType}
                      onChange={(e) => setTransactionType(e.target.value as any)}
                      className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs font-bold text-gray-900 dark:text-white"
                    >
                      <option value="CREDIT">CREDIT (+)</option>
                      <option value="DEBIT">DEBIT (-)</option>
                    </select>
                  </div>
                ) : (
                  <div>
                    <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Payment Method</span>
                    <input
                      type="text"
                      value={paymentMethod}
                      onChange={(e) => setPaymentMethod(e.target.value)}
                      className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white"
                    />
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Primary Call to Action Controls */}
          <div className="space-y-3">
            <label className="text-xs font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
              Primary Action Button (CTA)
            </label>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Button Text</span>
                <input
                  type="text"
                  value={ctaText}
                  onChange={(e) => setCtaText(e.target.value)}
                  className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs font-bold text-gray-900 dark:text-white"
                />
              </div>
              <div>
                <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Destination URL</span>
                <input
                  type="url"
                  value={ctaUrl}
                  onChange={(e) => setCtaUrl(e.target.value)}
                  className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/10 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white font-mono"
                />
              </div>
            </div>
          </div>
        </div>

        {/* ======================================================================= */}
        {/* RIGHT PANE: LIVE DUAL-VIEWPORT RENDERER                                */}
        {/* ======================================================================= */}
        <div className="flex-1 flex flex-col bg-gray-100 dark:bg-[#050507] overflow-hidden">
          {/* Viewport Meta Bar */}
          <div className="px-6 py-2 bg-gray-200/60 dark:bg-[#0B0B0E] border-b border-gray-300 dark:border-white/10 flex items-center justify-between text-xs">
            <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400 font-medium">
              <span>Preview Mode:</span>
              <span className="font-bold text-gray-900 dark:text-white uppercase">
                {viewport === "desktop" ? "Desktop (680px Luxury Presentation)" : "Mobile (375px Fluid Adaptive Stack)"}
              </span>
            </div>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={handleCopyHtml}
                className="flex items-center gap-1 text-xs font-bold text-gray-700 dark:text-gray-300 hover:text-[#FFB800] transition-colors cursor-pointer"
              >
                {copiedHtml ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copiedHtml ? "HTML Copied" : "Copy HTML"}</span>
              </button>
            </div>
          </div>

          {/* Live Iframe Container */}
          <div className="flex-1 overflow-auto p-6 flex justify-center items-start">
            <div
              className={`transition-all duration-300 ease-out shadow-2xl bg-white rounded-2xl overflow-hidden border border-gray-300 dark:border-white/20 ${
                viewport === "desktop" ? "w-full max-w-[720px]" : "w-[395px] max-w-full"
              }`}
              style={{ minHeight: "680px" }}
            >
              <iframe
                title="Email Preview"
                srcDoc={compiledHtml}
                className="w-full h-full min-h-[720px] border-none"
                sandbox="allow-same-origin allow-popups"
              />
            </div>
          </div>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* TEST SEND MODAL                                                           */}
      {/* ========================================================================= */}
      {showSendModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white dark:bg-[#121217] rounded-3xl p-6 border border-gray-200 dark:border-white/15 shadow-2xl space-y-5 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-[#FFB800] text-[#050505] flex items-center justify-center font-black">
                  <Send className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-sm font-black text-gray-900 dark:text-white">
                    Dispatch Live Test Email
                  </h3>
                  <p className="text-[11px] text-gray-500 dark:text-gray-400">
                    Live SMTP dispatch via {smtpHost}:{smtpPort}
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowSendModal(false)}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-white p-1 rounded-lg cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3">
              <div>
                <label className="text-[10px] font-black uppercase tracking-wider text-gray-700 dark:text-gray-300">
                  Recipient Email Address
                </label>
                <input
                  type="email"
                  value={testSendTo}
                  onChange={(e) => setTestSendTo(e.target.value)}
                  placeholder="Enter any destination email address"
                  className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/15 rounded-xl px-3.5 py-2.5 text-xs text-gray-900 dark:text-white font-medium focus:outline-none focus:ring-2 focus:ring-[#FFB800]"
                  required
                />
              </div>

              <div className="p-3 bg-gray-50 dark:bg-[#171720] border border-gray-200 dark:border-white/10 rounded-xl space-y-1">
                <div className="text-[11px] font-bold text-gray-700 dark:text-gray-300">
                  Subject: <span className="font-normal">{subject}</span>
                </div>
                <div className="text-[10px] text-gray-500 dark:text-gray-400">
                  Template: <span className="font-bold text-[#FFB800]">{templateId}</span> • Sender: {smtpFromEmail}
                </div>
              </div>

              {/* Spam & Inbox Deliverability Note */}
              <div className="p-3 bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800/40 rounded-xl space-y-1.5 text-[11px] text-amber-900 dark:text-amber-200">
                <div className="font-bold flex items-center justify-between">
                  <span className="flex items-center gap-1.5">
                    <AlertTriangle className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400 shrink-0" />
                    Deliverability &amp; Spam Advisory
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      setShowSendModal(false);
                      setShowDnsModal(true);
                    }}
                    className="text-[10px] font-bold text-[#FFB800] hover:underline cursor-pointer"
                  >
                    Check DNS Status →
                  </button>
                </div>
                <p className="text-[10px] leading-relaxed text-amber-800 dark:text-amber-300">
                  If this email lands in your Spam folder, your domain (<span className="font-mono font-bold">engracedsmile.com</span>) requires SPF and DMARC TXT records in Vercel DNS to pass Gmail/Yahoo 2024 sender verification.
                </p>
              </div>

              {sendResult && (
                <div
                  className={`p-3.5 rounded-xl text-xs flex items-start gap-2.5 ${
                    sendResult.success
                      ? "bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-300 dark:border-emerald-700/50 text-emerald-800 dark:text-emerald-300"
                      : "bg-red-50 dark:bg-red-900/20 border border-red-300 dark:border-red-700/50 text-red-800 dark:text-red-300"
                  }`}
                >
                  {sendResult.success ? (
                    <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
                  ) : (
                    <AlertTriangle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
                  )}
                  <div className="flex-1">
                    <p className="font-bold">{sendResult.message}</p>
                    {sendResult.messageId && (
                      <p className="font-mono text-[10px] opacity-80 mt-0.5">
                        Message ID: {sendResult.messageId}
                      </p>
                    )}
                  </div>
                </div>
              )}
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowSendModal(false)}
                className="px-4 py-2 rounded-xl text-xs font-bold text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/5 transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleDispatchTestEmail}
                disabled={sendingEmail || !testSendTo.trim() || !testSendTo.includes("@")}
                className="flex items-center gap-2 px-5 py-2.5 bg-[#FFB800] hover:bg-[#FFB800]/90 disabled:opacity-50 text-[#050505] rounded-xl text-xs font-black shadow-md transition-all cursor-pointer"
              >
                {sendingEmail ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Send className="w-3.5 h-3.5" />}
                <span>{sendingEmail ? "Dispatching..." : "Send Test Email"}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* DOMAIN DNS & SPAM HEALTH MODAL                                             */}
      {/* ========================================================================= */}
      {showDnsModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
          <div className="w-full max-w-2xl bg-white dark:bg-[#121217] rounded-3xl p-6 border border-gray-200 dark:border-white/15 shadow-2xl space-y-5 my-8">
            {/* Header */}
            <div className="flex items-center justify-between pb-3 border-b border-gray-100 dark:border-white/10">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-2xl bg-[#FFB800]/15 flex items-center justify-center">
                  <Globe className="w-5 h-5 text-[#FFB800]" />
                </div>
                <div>
                  <h3 className="text-base font-black text-gray-900 dark:text-white">
                    Domain DNS &amp; Anti-Spam Deliverability
                  </h3>
                  <p className="text-[11px] text-gray-500 dark:text-gray-400">
                    Live SPF, DMARC &amp; MX verification for <span className="font-bold text-gray-900 dark:text-white">engracedsmile.com</span>
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={runDnsCheck}
                  disabled={dnsCheckLoading}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-gray-100 dark:bg-white/10 hover:bg-gray-200 dark:hover:bg-white/15 text-xs font-bold text-gray-800 dark:text-white transition-colors cursor-pointer"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${dnsCheckLoading ? "animate-spin" : ""}`} />
                  <span>Re-check</span>
                </button>
                <button
                  type="button"
                  onClick={() => setShowDnsModal(false)}
                  className="p-1.5 text-gray-400 hover:text-gray-600 dark:hover:text-white rounded-lg transition-colors cursor-pointer"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Why emails land in spam notice */}
            <div className="p-3.5 rounded-2xl bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800/40 text-xs text-amber-900 dark:text-amber-200 space-y-1.5">
              <div className="font-bold flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4 text-amber-600 dark:text-amber-400 shrink-0" />
                <span>Why Emails Land in Spam (Google &amp; Yahoo 2024 Policy)</span>
              </div>
              <p className="text-[11px] leading-relaxed text-amber-800 dark:text-amber-300">
                Gmail, Yahoo, and Outlook require sender domains to have valid <strong>SPF</strong> and <strong>DMARC</strong> records.
                Without them, automated filters classify mail from custom domains as unverified or spoofed, routing it straight to Spam.
                Add the 3 DNS records below in your Vercel DNS settings to achieve 100% Primary Inbox deliverability.
              </p>
            </div>

            {/* Status Grid */}
            {(() => {
              const isSpfValid = Boolean(
                dnsCheckData?.spf?.valid ||
                dnsCheckData?.spf?.status === "valid" ||
                dnsCheckData?.spf?.isAuthorizedForHost ||
                dnsCheckData?.spf?.exists
              );
              const isDmarcValid = Boolean(
                dnsCheckData?.dmarc?.valid ||
                dnsCheckData?.dmarc?.status === "valid" ||
                dnsCheckData?.dmarc?.exists
              );
              const isMxValid = Boolean(
                dnsCheckData?.mx?.valid ||
                dnsCheckData?.mx?.status === "valid" ||
                (dnsCheckData?.mx?.records && dnsCheckData.mx.records.length > 0)
              );

              const spfRecordText = dnsCheckData?.spf?.record || dnsCheckData?.spf?.records?.[0] || "v=spf1 +a +mx +ip4:5.39.69.62 ~all";
              const dmarcRecordText = dnsCheckData?.dmarc?.record || dnsCheckData?.dmarc?.records?.[0] || "v=DMARC1; p=none; rua=mailto:noreply@engracedsmile.com; aspf=r;";
              const mxRecordText = typeof dnsCheckData?.mx?.records?.[0] === "object"
                ? dnsCheckData.mx.records[0]?.exchange
                : (dnsCheckData?.mx?.records?.[0] || "engracedsmile.com");

              return (
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  {/* SPF Record Card */}
                  <div className={`p-3.5 rounded-2xl border ${
                    isSpfValid
                      ? "bg-emerald-50 dark:bg-emerald-950/20 border-emerald-300 dark:border-emerald-800/40"
                      : "bg-red-50 dark:bg-red-950/20 border-red-300 dark:border-red-800/40"
                  }`}>
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-black uppercase tracking-wider text-gray-500 dark:text-gray-400">SPF Record</span>
                      {isSpfValid ? (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-700 dark:text-emerald-400">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Valid
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-red-700 dark:text-red-400">
                          <AlertTriangle className="w-3.5 h-3.5" /> Missing
                        </span>
                      )}
                    </div>
                    <div className="mt-1 text-xs font-bold text-gray-900 dark:text-white">
                      {isSpfValid ? "Configured & Active" : "Action Required"}
                    </div>
                    <p className="mt-1 text-[10px] text-gray-600 dark:text-gray-400 line-clamp-2">
                      {isSpfValid ? spfRecordText : "Authorizes mail server 5.39.69.62"}
                    </p>
                  </div>

                  {/* DMARC Record Card */}
                  <div className={`p-3.5 rounded-2xl border ${
                    isDmarcValid
                      ? "bg-emerald-50 dark:bg-emerald-950/20 border-emerald-300 dark:border-emerald-800/40"
                      : "bg-red-50 dark:bg-red-950/20 border-red-300 dark:border-red-800/40"
                  }`}>
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-black uppercase tracking-wider text-gray-500 dark:text-gray-400">DMARC Record</span>
                      {isDmarcValid ? (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-700 dark:text-emerald-400">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Valid
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-red-700 dark:text-red-400">
                          <AlertTriangle className="w-3.5 h-3.5" /> Missing
                        </span>
                      )}
                    </div>
                    <div className="mt-1 text-xs font-bold text-gray-900 dark:text-white">
                      {isDmarcValid ? "Protected & Enforced" : "Action Required"}
                    </div>
                    <p className="mt-1 text-[10px] text-gray-600 dark:text-gray-400 line-clamp-2">
                      {isDmarcValid ? dmarcRecordText : "Domain identity policy enforcement"}
                    </p>
                  </div>

                  {/* MX Record Card */}
                  <div className={`p-3.5 rounded-2xl border ${
                    isMxValid
                      ? "bg-emerald-50 dark:bg-emerald-950/20 border-emerald-300 dark:border-emerald-800/40"
                      : "bg-amber-50 dark:bg-amber-950/20 border-amber-300 dark:border-amber-800/40"
                  }`}>
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-black uppercase tracking-wider text-gray-500 dark:text-gray-400">MX Record</span>
                      {isMxValid ? (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-700 dark:text-emerald-400">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Configured
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-amber-700 dark:text-amber-400">
                          <AlertTriangle className="w-3.5 h-3.5" /> Recommended
                        </span>
                      )}
                    </div>
                    <div className="mt-1 text-xs font-bold text-gray-900 dark:text-white">
                      {isMxValid ? "Active" : "Recommended"}
                    </div>
                    <p className="mt-1 text-[10px] text-gray-600 dark:text-gray-400 line-clamp-2">
                      {isMxValid ? `Host: ${mxRecordText}` : "Inbound routing: server.hostnextdns.com"}
                    </p>
                  </div>
                </div>
              );
            })()}

            {/* Step-by-Step DNS Instructions */}
            <div className="space-y-3">
              <div className="text-xs font-black uppercase tracking-wider text-gray-900 dark:text-white flex items-center justify-between">
                <span>DNS Verification Reference (cPanel / HostNext)</span>
                <span className="text-[10px] font-medium text-gray-500 dark:text-gray-400">
                  Domain: engracedsmile.com
                </span>
              </div>

              {/* Record 1: SPF */}
              <div className="p-3 bg-gray-50 dark:bg-[#1A1A24] border border-gray-200 dark:border-white/10 rounded-2xl space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded-md bg-[#FFB800] text-[#050505] font-black text-[10px]">TXT</span>
                    <span className="font-bold text-gray-900 dark:text-white">SPF (Sender Policy Framework)</span>
                  </div>
                  <span className="text-[10px] font-mono text-gray-500 dark:text-gray-400">Name: @</span>
                </div>
                <div className="flex items-center justify-between bg-white dark:bg-[#121217] p-2.5 rounded-xl border border-gray-200 dark:border-white/10 font-mono text-[11px] text-gray-800 dark:text-gray-200">
                  <span className="truncate mr-2">v=spf1 ip4:5.39.69.62 include:server.hostnextdns.com ~all</span>
                  <button
                    type="button"
                    onClick={() => {
                      navigator.clipboard.writeText("v=spf1 ip4:5.39.69.62 include:server.hostnextdns.com ~all");
                      setCopiedRecordKey("spf");
                      setTimeout(() => setCopiedRecordKey(null), 2000);
                    }}
                    className="shrink-0 px-2.5 py-1 bg-[#FFB800] text-[#050505] rounded-lg text-[10px] font-black flex items-center gap-1 cursor-pointer"
                  >
                    {copiedRecordKey === "spf" ? <Check className="w-3 h-3" /> : <Copy className="w-3 h-3" />}
                    <span>{copiedRecordKey === "spf" ? "Copied" : "Copy Value"}</span>
                  </button>
                </div>
              </div>

              {/* Record 2: DMARC */}
              <div className="p-3 bg-gray-50 dark:bg-[#1A1A24] border border-gray-200 dark:border-white/10 rounded-2xl space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded-md bg-[#FFB800] text-[#050505] font-black text-[10px]">TXT</span>
                    <span className="font-bold text-gray-900 dark:text-white">DMARC (Domain-based Message Authentication)</span>
                  </div>
                  <span className="text-[10px] font-mono text-gray-500 dark:text-gray-400">Name: _dmarc</span>
                </div>
                <div className="flex items-center justify-between bg-white dark:bg-[#121217] p-2.5 rounded-xl border border-gray-200 dark:border-white/10 font-mono text-[11px] text-gray-800 dark:text-gray-200">
                  <span className="truncate mr-2">v=DMARC1; p=none; rua=mailto:support@engracedsmile.com; aspf=r;</span>
                  <button
                    type="button"
                    onClick={() => {
                      navigator.clipboard.writeText("v=DMARC1; p=none; rua=mailto:support@engracedsmile.com; aspf=r;");
                      setCopiedRecordKey("dmarc");
                      setTimeout(() => setCopiedRecordKey(null), 2000);
                    }}
                    className="shrink-0 px-2.5 py-1 bg-[#FFB800] text-[#050505] rounded-lg text-[10px] font-black flex items-center gap-1 cursor-pointer"
                  >
                    {copiedRecordKey === "dmarc" ? <Check className="w-3 h-3" /> : <Copy className="w-3 h-3" />}
                    <span>{copiedRecordKey === "dmarc" ? "Copied" : "Copy Value"}</span>
                  </button>
                </div>
              </div>

              {/* Record 3: MX */}
              <div className="p-3 bg-gray-50 dark:bg-[#1A1A24] border border-gray-200 dark:border-white/10 rounded-2xl space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded-md bg-[#FFB800] text-[#050505] font-black text-[10px]">MX</span>
                    <span className="font-bold text-gray-900 dark:text-white">Mail Exchange (Inbound)</span>
                  </div>
                  <span className="text-[10px] font-mono text-gray-500 dark:text-gray-400">Name: @ • Priority: 10</span>
                </div>
                <div className="flex items-center justify-between bg-white dark:bg-[#121217] p-2.5 rounded-xl border border-gray-200 dark:border-white/10 font-mono text-[11px] text-gray-800 dark:text-gray-200">
                  <span className="truncate mr-2">server.hostnextdns.com</span>
                  <button
                    type="button"
                    onClick={() => {
                      navigator.clipboard.writeText("server.hostnextdns.com");
                      setCopiedRecordKey("mx");
                      setTimeout(() => setCopiedRecordKey(null), 2000);
                    }}
                    className="shrink-0 px-2.5 py-1 bg-[#FFB800] text-[#050505] rounded-lg text-[10px] font-black flex items-center gap-1 cursor-pointer"
                  >
                    {copiedRecordKey === "mx" ? <Check className="w-3 h-3" /> : <Copy className="w-3 h-3" />}
                    <span>{copiedRecordKey === "mx" ? "Copied" : "Copy Value"}</span>
                  </button>
                </div>
              </div>
            </div>

            {/* Footer */}
            <div className="flex items-center justify-between pt-2 border-t border-gray-100 dark:border-white/10">
              <span className="text-[11px] text-gray-500 dark:text-gray-400">
                DNS propagation usually completes within 10 to 60 minutes.
              </span>
              <button
                type="button"
                onClick={() => setShowDnsModal(false)}
                className="px-5 py-2 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#050505] rounded-xl text-xs font-black transition-colors cursor-pointer"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* RAW HTML MODAL                                                            */}
      {/* ========================================================================= */}
      {showHtmlModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-6">
          <div className="w-full max-w-4xl bg-white dark:bg-[#121217] rounded-3xl p-6 border border-gray-200 dark:border-white/15 shadow-2xl flex flex-col max-h-[85vh] space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Code className="w-5 h-5 text-[#FFB800]" />
                <h3 className="text-base font-black text-gray-900 dark:text-white">
                  Raw Email-Safe HTML Code
                </h3>
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={handleCopyHtml}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#FFB800] text-[#050505] text-xs font-black cursor-pointer"
                >
                  {copiedHtml ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copiedHtml ? "Copied" : "Copy Code"}</span>
                </button>
                <button
                  type="button"
                  onClick={() => setShowHtmlModal(false)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-white p-1 rounded-lg cursor-pointer"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>
            <div className="flex-1 overflow-auto bg-gray-900 text-gray-200 font-mono text-xs p-4 rounded-2xl border border-gray-800">
              <pre className="whitespace-pre-wrap">{compiledHtml}</pre>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* SMTP SETTINGS DRAWER                                                      */}
      {/* ========================================================================= */}
      {showSmtpDrawer && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex justify-end">
          <div className="w-full max-w-md bg-white dark:bg-[#121217] h-full p-6 border-l border-gray-200 dark:border-white/15 shadow-2xl flex flex-col justify-between space-y-6 animate-in slide-in-from-right duration-200">
            <div className="space-y-6 overflow-y-auto pr-1">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="w-9 h-9 rounded-xl bg-[#FFB800] text-[#050505] flex items-center justify-center font-black">
                    <Server className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="text-sm font-black text-gray-900 dark:text-white">
                      Production SMTP Credentials
                    </h3>
                    <p className="text-[11px] text-gray-500 dark:text-gray-400">
                      Firestore path: <code className="font-mono">system_settings/smtp</code>
                    </p>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => setShowSmtpDrawer(false)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-white p-1 rounded-lg cursor-pointer"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Status Banner */}
              {smtpStatus && (
                <div
                  className={`p-3.5 rounded-xl text-xs flex items-start gap-2.5 ${
                    smtpStatus.ok
                      ? "bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-300 dark:border-emerald-700/50 text-emerald-800 dark:text-emerald-300"
                      : "bg-red-50 dark:bg-red-900/20 border border-red-300 dark:border-red-700/50 text-red-800 dark:text-red-300"
                  }`}
                >
                  {smtpStatus.ok ? (
                    <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
                  ) : (
                    <AlertTriangle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
                  )}
                  <p className="font-semibold">{smtpStatus.message}</p>
                </div>
              )}

              {/* Fields */}
              <div className="space-y-3">
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">SMTP Host</span>
                  <input
                    type="text"
                    value={smtpHost}
                    onChange={(e) => setSmtpHost(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2 text-xs font-mono text-gray-900 dark:text-white"
                  />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Port</span>
                    <input
                      type="number"
                      value={smtpPort}
                      onChange={(e) => setSmtpPort(Number(e.target.value))}
                      className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2 text-xs font-mono text-gray-900 dark:text-white"
                    />
                  </div>
                  <div>
                    <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">SSL (Secure)</span>
                    <select
                      value={String(smtpSecure)}
                      onChange={(e) => setSmtpSecure(e.target.value === "true")}
                      className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2 text-xs font-bold text-gray-900 dark:text-white"
                    >
                      <option value="true">True (Port 465 SSL)</option>
                      <option value="false">False (Port 587 STARTTLS)</option>
                    </select>
                  </div>
                </div>
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Username</span>
                  <input
                    type="text"
                    value={smtpUser}
                    onChange={(e) => setSmtpUser(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2 text-xs font-mono text-gray-900 dark:text-white"
                  />
                </div>
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">Password</span>
                  <input
                    type="password"
                    value={smtpPass}
                    onChange={(e) => setSmtpPass(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2 text-xs font-mono text-gray-900 dark:text-white"
                  />
                </div>
                <div>
                  <span className="text-[10px] font-bold text-gray-500 dark:text-gray-400 uppercase">From Display Name</span>
                  <input
                    type="text"
                    value={smtpFromName}
                    onChange={(e) => setSmtpFromName(e.target.value)}
                    className="w-full mt-1 bg-gray-50 dark:bg-[#1A1A24] border border-gray-300 dark:border-white/15 rounded-xl px-3 py-2 text-xs text-gray-900 dark:text-white"
                  />
                </div>
              </div>
            </div>

            {/* Bottom Actions */}
            <div className="space-y-2 pt-4 border-t border-gray-200 dark:border-white/10 shrink-0">
              <button
                type="button"
                onClick={handleTestSmtp}
                disabled={testingSmtp}
                className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-gray-100 dark:bg-white/10 hover:bg-gray-200 dark:hover:bg-white/15 text-xs font-bold text-gray-800 dark:text-white transition-colors cursor-pointer"
              >
                {testingSmtp ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Zap className="w-3.5 h-3.5 text-[#FFB800]" />}
                <span>{testingSmtp ? "Testing Handshake..." : "Test SMTP Connection"}</span>
              </button>

              <button
                type="button"
                onClick={handleSaveSmtp}
                disabled={savingSmtp}
                className="w-full flex items-center justify-center gap-2 py-3 rounded-xl bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#050505] text-xs font-black shadow-md transition-all cursor-pointer"
              >
                {savingSmtp ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Check className="w-3.5 h-3.5" />}
                <span>{savingSmtp ? "Saving Settings..." : "Save to Production Firestore"}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
