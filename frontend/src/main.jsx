import { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import App from "./app/App.jsx";
import "./styles/index.css";
import { AuthProvider } from "./features/auth/context/AuthContext.jsx";

// Application entry point: global providers belong here; feature state stays in
// its feature hooks or contexts.
ReactDOM.createRoot(document.getElementById("root")).render(
  <StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>,
);
