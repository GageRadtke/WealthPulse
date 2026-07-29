import React from "react";
import { getCurrentUsername } from "../../../features/auth/api/authApi";

/**
 * DashboardNavbar — unified top navigation bar.
 *
 * Controls all view switching in a single place:
 *   - Portfolio Overview
 *   - Stocks In-Depth
 *   - Metals In-Depth
 *   - Learning Center
 */
export default function DashboardNavbar({
  currentView,
  setCurrentView,
  onLogout,
}) {
  const navItems = [
    { id: "dashboard", label: "📊 Portfolio Overview" },
    { id: "stocks", label: "📈 Stocks In-Depth" },
    { id: "metals", label: "🪙 Metals In-Depth" },
    { id: "learning", label: "📚 Learning Center" },
  ];

  return (
    <div className="top-navbar">
      <div className="navbar-menu">
        {navItems.map(({ id, label }) => (
          <button
            key={id}
            className={`nav-item ${currentView === id ? "active" : ""}`}
            onClick={() => setCurrentView(id)}
            type="button"
          >
            {label}
          </button>
        ))}
      </div>
      <div className="navbar-actions">
        <span className="status-indicator">⚙️ Java API: Connected</span>
        <span className="auth-username">
          Signed in as: {getCurrentUsername() || "(unknown)"}
        </span>
        <button
          className="logout-btn top-logout"
          onClick={onLogout}
          type="button"
        >
          Logout
        </button>
      </div>
    </div>
  );
}
