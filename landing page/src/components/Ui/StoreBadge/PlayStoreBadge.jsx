import Link from "next/link";

const PlayStoreBadge = ({ dark = false }) => (
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
      <path d="M3.609 1.814L13.792 12 3.61 22.186a.996.996 0 01-.61-.92V2.734a1 1 0 01.609-.92zm10.89 10.893l2.302 2.302-10.937 6.333 8.635-8.635zm3.199-3.199l2.807 1.626a1 1 0 010 1.732l-2.807 1.626L15.206 12l2.492-2.492zM5.864 2.658L16.8 8.99l-2.302 2.302-8.634-8.634z" />
    </svg>
    <div style={{ display: "flex", flexDirection: "column", lineHeight: 1.2 }}>
      <span style={{ fontSize: 10, opacity: 0.7 }}>GET IT ON</span>
      <span style={{ fontSize: 16, fontWeight: 700 }}>Google Play</span>
    </div>
  </Link>
);

export default PlayStoreBadge;
