"use client";

export default function Error({ error, reset }) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "100vh",
        padding: 24,
        textAlign: "center",
        fontFamily: "var(--font-inter)",
      }}
    >
      <h1 style={{ fontSize: 72, marginBottom: 16, color: "#F5A623" }}>
        Oops!
      </h1>
      <h2 style={{ fontSize: 24, marginBottom: 16, color: "#000" }}>
        Something went wrong
      </h2>
      <p style={{ color: "#666", marginBottom: 24, maxWidth: 480 }}>
        We encountered an unexpected error. Please try again or contact us if
        the problem persists.
      </p>
      <button
        onClick={reset}
        style={{
          padding: "12px 32px",
          backgroundColor: "#000",
          color: "#fff",
          border: "none",
          borderRadius: 8,
          fontSize: 16,
          cursor: "pointer",
        }}
      >
        Try Again
      </button>
    </div>
  );
}
