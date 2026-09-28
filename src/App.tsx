import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { HomePage } from "./pages/HomePage";
import { AdminPage } from "./pages/AdminPage";
import { PublicTrackingPage } from "./pages/PublicTrackingPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { EmailVerifiedPage } from "./pages/EmailVerifiedPage";
import { PrivacyPage, TermsPage } from "./pages/LegalPages";
import { SignInPage, SignUpPage, ResetPasswordPage } from "./pages/AuthPages";

export const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/engdadmin" element={<AdminPage />} />
      <Route path="/engadmin" element={<Navigate to="/engdadmin" replace />} />
      <Route path="/track/:id" element={<PublicTrackingPage />} />
      <Route path="/verified" element={<EmailVerifiedPage />} />
      <Route path="/privacy" element={<PrivacyPage />} />
      <Route path="/terms" element={<TermsPage />} />
      <Route path="/sign-in" element={<SignInPage />} />
      <Route path="/sign-up" element={<SignUpPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
