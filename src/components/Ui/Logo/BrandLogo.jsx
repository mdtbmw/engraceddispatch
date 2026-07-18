import Link from "next/link";

const LogoIcon = ({ fill = "currentColor" }) => (
  <svg
    viewBox="0 0 24 24"
    style={{ height: "100%", width: "auto", fill, display: "block" }}
  >
    <path
      fill-rule="evenodd"
      clip-rule="evenodd"
      d="M0 12C5.05631e-07 5.37258 5.37258 0 12 0C18.6274 0 24 5.37258 24 12C17.3736 12 12.0016 6.629 12 0.0028711C11.9984 6.629 6.62645 12 0 12ZM12 24C12 17.3725 17.3726 12 24 12C24 18.6274 18.6274 24 12 24ZM12 24C12 17.3725 6.62742 12 0 12C0 18.6274 5.37258 24 12 24Z"
    />
  </svg>
);

const BrandLogo = ({ dark }) => {
  const color = dark ? "#fff" : "#1a1a2e";
  return (
    <Link
      href="/"
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: 8,
        textDecoration: "none",
        color,
      }}
    >
      <span
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          width: 36,
          height: 36,
          borderRadius: 10,
          background: dark ? "rgba(255,255,255,0.15)" : "#F5A623",
          padding: 6,
        }}
      >
        <span style={{ height: 22, color: dark ? "#fff" : "#000" }}>
          <LogoIcon fill="currentColor" />
        </span>
      </span>
      <span style={{ fontSize: 20, fontWeight: 700, letterSpacing: "-0.3px" }}>
        Engraced Dispatch
      </span>
    </Link>
  );
};

export default BrandLogo;
