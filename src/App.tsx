import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { HomePage } from "./pages/HomePage";
import { AdminPage } from "./pages/AdminPage";
import { PublicTrackingPage } from "./pages/PublicTrackingPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { InteractionSimulator } from "./components/interaction/InteractionSimulator";

export const App: React.FC = () => {
  return (
    <>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/engdadmin" element={<AdminPage />} />
        <Route path="/engadmin" element={<Navigate to="/engdadmin" replace />} />
        <Route path="/track/:id" element={<PublicTrackingPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
      <InteractionSimulator />
    </>
  );
};
