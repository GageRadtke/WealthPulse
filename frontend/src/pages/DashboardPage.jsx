import React from "react";
import DashboardLayout from "../features/dashboard/layout/DashboardLayout";
import ProfileUploader from "../features/auth/components/ProfileUploader";

export default function DashboardPage({
  onLogout,
  username = "Investor",
  profileUsername = null,
  portfolioAssets = [],
  initialProfilePic = null,
  onAssetAdded,
  onRefreshPrices,
  onAssetPurityUpdated,
  onAssetDeleted,
  onAssetUpdated,
  activeView,
  setActiveView,
}) {
  const groupedAssets = portfolioAssets.reduce(
    (groups, asset) => {
      const type = asset.type?.toLowerCase();
      if (type === "stock") groups.stock.push(asset);
      if (type === "metal") groups.metal.push(asset);
      return groups;
    },
    { stock: [], metal: [] }
  );

  const welcomeHeader = (
    <div className="welcome-header">
      <div>
        <h3>Welcome, {username}!</h3>
        <p className="subtitle">
          All metal weights in WealthPulse use troy ounces. To add a product
          measured in grams, use the converter on the Metals In-Depth page.
        </p>
      </div>
      <ProfileUploader
        username={profileUsername || username}
        initialProfilePic={initialProfilePic}
      />
    </div>
  );

  return (
    <div className="dashboard-container top-nav-layout">
      <DashboardLayout
        onLogout={onLogout}
        welcomeHeader={welcomeHeader}
        portfolioAssets={portfolioAssets}
        onAssetAdded={onAssetAdded}
        onRefreshPrices={onRefreshPrices}
        onAssetPurityUpdated={onAssetPurityUpdated}
        onAssetDeleted={onAssetDeleted}
        onAssetUpdated={onAssetUpdated}
        activeView={activeView}
        setActiveView={setActiveView}
        stockAssets={groupedAssets.stock}
        metalAssets={groupedAssets.metal}
      />
    </div>
  );
}
