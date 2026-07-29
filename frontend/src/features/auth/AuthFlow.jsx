import React, { useState } from "react";
import LearningCenter from "../learning/components/LearningCenter";
import LoginCard from "./components/LoginCard";
import RegisterCard from "./components/RegisterCard";
import SplashWelcome from "./components/SplashWelcome";
import { login, register } from "./api/authApi";

export default function AuthFlow({ onCustomLoginSuccess }) {
  const [currentScreen, setCurrentScreen] = useState("login");
  const [loginErrorMessage, setLoginErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  /**
   * Validate credentials against the Spring Boot /api/auth/login endpoint.
   * On success: JWT stored by authApi, onCustomLoginSuccess called with user info.
   * On failure: error message shown to the user.
   */
  const handleLoginValidation = async ({ username, password }) => {
    setIsLoading(true);
    setLoginErrorMessage("");

    try {
      const result = await login({
        username: username.trim(),
        password,
      });

      if (onCustomLoginSuccess) {
        onCustomLoginSuccess(result.user, result.token);
      }
    } catch (err) {
      const serverError = err.response?.data?.error;
      setLoginErrorMessage(serverError || "Invalid username or password.");
      setCurrentScreen("failed_login");
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Register a new account via /api/auth/register.
   * Returns true on success (RegisterCard handles navigation), false on conflict.
   */
  const handleUserRegistration = async (newProfilePayload) => {
    setIsLoading(true);
    try {
      const sanitizedUsernamePrefix = newProfilePayload.username
        .trim()
        .toLowerCase()
        .replace(/[^a-z0-9]/g, "");
      const result = await register(
        newProfilePayload.username.trim(),
        newProfilePayload.password,
        newProfilePayload.email?.trim() ||
          `${sanitizedUsernamePrefix}@wealthpulse.local`,
      );
      // Auto-login after successful registration
      if (onCustomLoginSuccess) {
        onCustomLoginSuccess(result.user, result.token);
      }
      return true;
    } catch (err) {
      const serverError = err.response?.data?.error;
      if (serverError) {
        alert(serverError);
      }
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fin-app-container">
      <nav className="fin-navbar">
        <h2 className="fin-logo">Wealth Pulse</h2>
        <div>
          <button
            className={`fin-nav-btn ${currentScreen === "login" ? "active-tab" : ""}`}
            onClick={() => setCurrentScreen("login")}
          >
            Sign In
          </button>
          <button
            className={`fin-nav-btn ${currentScreen === "learning" ? "active-tab" : ""}`}
            onClick={() => setCurrentScreen("learning")}
          >
            Free Educational Center
          </button>
        </div>
      </nav>

      <main className="fin-main-content">
        {currentScreen === "login" && (
          <div className="splash-welcome-wrapper">
            <SplashWelcome />
            <LoginCard
              onLoginAttempt={handleLoginValidation}
              onNavigateToRegister={() => setCurrentScreen("register")}
              isLoading={isLoading}
            />
          </div>
        )}

        {currentScreen === "failed_login" && (
          <div className="auth-card error-state">
            <h3 className="alert-error">Authentication Access Denied</h3>
            <p>{loginErrorMessage}</p>
            <button
              onClick={() => setCurrentScreen("login")}
              className="primary-btn"
            >
              Return to Login
            </button>
          </div>
        )}

        {currentScreen === "learning" && <LearningCenter />}

        {currentScreen === "register" && (
          <RegisterCard
            onRegisterSubmit={handleUserRegistration}
            onCancel={() => setCurrentScreen("login")}
            isLoading={isLoading}
          />
        )}
      </main>
    </div>
  );
}
