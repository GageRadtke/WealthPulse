// Layout
import DashboardNavbar from "./DashboardNavbar";

//views
import { VIEWS } from "../../../constants/appConstants";
import StocksInDepthView from "../views/stocks-in-depth/StocksInDepthView";
import MetalsInDepthView from "../views/metals-in-depth/MetalsInDepthView";
import LearningCenter from "../../../features/learning/components/LearningCenter";
import PortfolioOverview from "../views/portfolio-overview/PortfolioOverview";

export default function DashboardLayout({
  onLogout,
  welcomeHeader,
  stockAssets = [],
  metalAssets = [],
  portfolioAssets = [],
  onAssetAdded,
  onRefreshPrices,
  onAssetPurityUpdated,
  onAssetDeleted,
  onAssetUpdated,
  activeView,
  setActiveView,
}) {
  const assets = {
    stocks: stockAssets,
    metals: metalAssets,
    portfolio: portfolioAssets,
  };

  const renderCurrentView = () => {
    switch (activeView) {
      case VIEWS.STOCKS:
        return (
          <StocksInDepthView
            stockAssets={assets.stocks}
            onDelete={onAssetDeleted}
            onUpdate={onAssetUpdated}
            onPurityUpdate={onAssetPurityUpdated}
          />
        );
      case VIEWS.METALS:
        return (
          <MetalsInDepthView
            metalAssets={assets.metals}
            onDelete={onAssetDeleted}
            onUpdate={onAssetUpdated}
            onPurityUpdate={onAssetPurityUpdated}
          />
        );
      case VIEWS.LEARNING:
        return <LearningCenter />;

      default:
        return (
          <PortfolioOverview
            assets={assets}
            onAssetAdded={onAssetAdded}
            onRefreshPrices={onRefreshPrices}
          />
        );
    }
  };

  // Only show the welcome header on portfolio pages (not Learning Center)
  const showWelcomeHeader = activeView !== VIEWS.LEARNING;

  return (
    <>
      <DashboardNavbar
        currentView={activeView}
        setCurrentView={setActiveView}
        onLogout={onLogout}
      />
      <div className="main-content grid-override">
        {showWelcomeHeader && (
          <>
            {welcomeHeader}
            <hr className="divider" />
          </>
        )}
        {renderCurrentView()}
      </div>
    </>
  );
}
