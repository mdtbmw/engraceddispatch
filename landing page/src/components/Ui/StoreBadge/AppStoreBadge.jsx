import Link from "next/link";

const AppStoreBadge = ({ dark = false }) => (
  <Link
    href="#"
    className="zubuz-store-badge"
    style={{
      display: "inline-flex",
      alignItems: "center",
      gap: 10,
      padding: "10px 20px",
      borderRadius: 14,
      backgroundColor: dark ? "#000" : "#fff",
      border: dark ? "none" : "1px solid #e0e0e0",
      color: dark ? "#fff" : "#000",
      textDecoration: "none",
      transition: "all 0.3s ease",
    }}
    onMouseEnter={(e) => {
      e.currentTarget.style.transform = "translateY(-2px)";
      e.currentTarget.style.boxShadow = "0 8px 24px rgba(0,0,0,0.12)";
    }}
    onMouseLeave={(e) => {
      e.currentTarget.style.transform = "translateY(0)";
      e.currentTarget.style.boxShadow = "none";
    }}
  >
    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
      <path d="M17.05 20.28c-.98.95-2.05.8-3.08.35-1.09-.46-2.09-.48-3.24 0-1.44.62-2.2.44-3.06-.35C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z" />
    </svg>
    <div style={{ display: "flex", flexDirection: "column", lineHeight: 1.2 }}>
      <span style={{ fontSize: 10, opacity: 0.7 }}>Download on the</span>
      <span style={{ fontSize: 16, fontWeight: 700 }}>App Store</span>
    </div>
  </Link>
);

export default AppStoreBadge;
