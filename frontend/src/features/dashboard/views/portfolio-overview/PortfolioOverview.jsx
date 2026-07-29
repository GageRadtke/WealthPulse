import TotalWealthCard from "../../cards/TotalWealthCard.jsx";
import PortfolioSummary from "../../widgets/PortfolioSummary.jsx";
import FinancialGoals from "../../widgets/FinancialGoals.jsx";
import LiveNewsFeed from "../../components/LiveNewsFeed.jsx";
import MarketTracker from "../../widgets/MarketTracker.jsx";
import PortfolioDiversifier from "../../components/PortfolioDiversifier.jsx";
import DashboardCharts from "../metals-in-depth/DashboardCharts.jsx";
import AssetManager from "../../../assets/components/AssetManager.jsx";
import PerformanceModule from "../../../performance/components/PerformanceModule.jsx";

export default function PortfolioOverview({ assets, onAssetAdded, onRefreshPrices }) {
  return (
    <div className="dashboard-grid">
      {/* Summary */}

      <div className="grid-row">
        <TotalWealthCard assets={assets} />

        <FinancialGoals assets={assets} />
      </div>

      <PerformanceModule
        refreshKey={assets.portfolio
          .map((asset) => `${asset.id}:${asset.quantity}:${asset.price}:${asset.amountPaid}`)
          .join("|")}
      />

      {/* News */}
      <div className="full-width-row">
        <LiveNewsFeed />
      </div>

      {/* Market quotes and portfolio allocation */}
      <div className="grid-row market-allocation-row">
        <MarketTracker />
        <DashboardCharts
          stockAssets={assets.stocks}
          metalAssets={assets.metals}
          showDiversification
        />
      </div>

      <PortfolioDiversifier assets={assets} />

      <PortfolioSummary assets={assets} onRefreshPrices={onRefreshPrices} />

      <AssetManager onAssetAdded={onAssetAdded} />
    </div>
  );
}
