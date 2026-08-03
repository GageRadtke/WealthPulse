import React, { useState } from "react";
import AuthPage from "../pages/AuthPage";
import DashboardPage from "../pages/DashboardPage";
import { useAssets } from "../features/assets/hooks/useAssets";
import { VIEWS, API_BASE, APP_MODE } from "../constants/appConstants.js";
import { useAuth } from "../features/auth/hooks/useAuth.js";

function App() {
  // Authentication decides which top-level page is visible.
  const { user, loading, logout, setAuthenticatedUser } = useAuth();
  const [activeView, setActiveView] = useState(VIEWS.DASHBOARD);

  const handleLogin = (loggedInUser) => {
    setAuthenticatedUser(loggedInUser);
    setActiveView(VIEWS.DASHBOARD);
  };

  if (loading) {
    return <div className="app-loading">Checking authentication...</div>;
  }

  if (!user) {
    return <AuthPage onCustomLoginSuccess={handleLogin} />;
  }

  return <AuthenticatedApp user={user} logout={logout} activeView={activeView} setActiveView={setActiveView} />;
}

function AuthenticatedApp({ user, logout, activeView, setActiveView }) {
  // Portfolio state is loaded only after authentication succeeds.
  const {
    assets,
    saveAsset,
    deleteAsset,
    updateQuantity,
    refreshPrices,
    updateMetalPurity,
    clearAssets,
  } =
    useAssets(APP_MODE.LIVE);

  const handleLogout = () => {
    logout();
    clearAssets();
    setActiveView(VIEWS.DASHBOARD);
  };

  const profilePicUrl = user?.username
    ? `${API_BASE}/api/users/profile-picture/${encodeURIComponent(user.username)}`
    : null;

  return (
    <DashboardPage
      onLogout={handleLogout}
      username={user?.name || "Investor"}
      profileUsername={user?.username}
      portfolioAssets={assets}
      onAssetDeleted={deleteAsset}
      onAssetUpdated={updateQuantity}
      onAssetAdded={saveAsset}
      onRefreshPrices={refreshPrices}
      onAssetPurityUpdated={updateMetalPurity}
      activeView={activeView}
      setActiveView={setActiveView}
      initialProfilePic={profilePicUrl}
    />
  );
}

export default App;
