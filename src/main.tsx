import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { App } from "./App";
import "./app/globals.css";
import "./assets/css/bootstrap.min.css";
import "./assets/css/app.css";
import "./assets/css/main.css";
import "./assets/css/react-adjustment.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);
