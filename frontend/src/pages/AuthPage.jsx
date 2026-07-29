import React from "react";
import AuthFlow from "../features/auth/AuthFlow";

export default function AuthPage({ onCustomLoginSuccess }) {
  return <AuthFlow onCustomLoginSuccess={onCustomLoginSuccess} />;
}
