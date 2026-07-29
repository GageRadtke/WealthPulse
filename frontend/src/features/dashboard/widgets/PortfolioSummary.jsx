import React, { useEffect, useState } from "react";
import apiClient from "../../../api/client";
import { createCachedPriceMap, getAssetPrice } from "../../../shared/finance/marketPricing";
import { calculateAssetCurrentValue } from "../../../shared/finance/portfolioCalculations";
import { formatCurrency } from "../../../shared/utils/formatters";
import { getAssetDisplayType } from "../../assets/builders/assetBuilder";

/**
 * PortfolioSummary
 *
 * Quick overview table listing every tracked asset with its type,
 * quantity/weight, market price, and total current valuation.
 * Shown on the Portfolio Overview page.
 *
 * @param {Object}   props
 * @param {Object[]} props.portfolioAssets - All portfolio assets (stocks + metals combined)
 */
export default function PortfolioSummary({ assets = [], onRefreshPrices }) {
  const [cachedPrices, setCachedPrices] = useState({});
  const [isRefreshing, setIsRefreshing] = useState(false);
  const portfolioAssets = [
    ...(assets?.stocks ?? []),
    ...(assets?.metals ?? []),
  ];

  useEffect(() => {
    apiClient.get("/cache/status")
      .then(({ data }) => {
        setCachedPrices(createCachedPriceMap(Array.isArray(data) ? data : []));
      })
      .catch(() => setCachedPrices({}));
  }, []);

  const totalLiquidWealth = portfolioAssets.reduce((total, asset) => {
    const price = getAssetPrice(asset, cachedPrices);
    return total + calculateAssetCurrentValue({ ...asset, price });
  }, 0);

  const handleRefresh = async () => {
    if (typeof onRefreshPrices !== "function") return;
    try {
      setIsRefreshing(true);
      await onRefreshPrices();
    } finally {
      setIsRefreshing(false);
    }
  };

  return (
    <div className="quick-overview-section">
      <div className="overview-header-row">
        <h3 className="overview-title">Quick Portfolio Overview</h3>
        <div className="overview-actions">
          <button
            type="button"
            className="secondary-btn"
            onClick={handleRefresh}
            disabled={isRefreshing}
          >
            {isRefreshing ? "Refreshing prices..." : "Refresh market prices"}
          </button>
          <div className="liquidation-wealth-box">
            <span className="liquidation-label">Current Liquidation Wealth:</span>
            <span className="liquidation-value">
              {formatCurrency(totalLiquidWealth)}
            </span>
          </div>
        </div>
      </div>

      {portfolioAssets.length === 0 ? (
        <p className="placeholder-table empty-state">
          No portfolio positions tracked. Use the form Below to add your first
          asset!
        </p>
      ) : (
        <table className="fin-table overview-table">
          <colgroup>
            <col className="overview-col-asset" />
            <col className="overview-col-type" />
            <col className="overview-col-number" />
            <col className="overview-col-number" />
            <col className="overview-col-number" />
            <col className="overview-col-number" />
          </colgroup>
          <thead>
            <tr>
              <th className="fin-th">Asset Name / Ticker</th>
              <th className="fin-th">Asset Type</th>
              <th className="fin-th text-right">Quantity / Weight</th>
              <th className="fin-th text-right">Market / Spot Price</th>
              <th className="fin-th text-right">Price You Paid</th>
              <th className="fin-th text-right">Current Valuation</th>
            </tr>
          </thead>
          <tbody>
            {portfolioAssets.map((asset, index) => {
              const qty = parseFloat(asset.quantity) || 0;
              const price = getAssetPrice(asset, cachedPrices);
              const valuation = price === null ? null : calculateAssetCurrentValue({ ...asset, price });
              const amountPaid = parseFloat(asset.amountPaid) || 0;
              const displayType = getAssetDisplayType(asset);

              return (
                <tr key={asset.id || index}>
                  <td className="fin-td text-left">
                    <strong className="asset-ticker">
                      {asset.type?.toUpperCase() === "STOCK"
                        ? asset.ticker || asset.name || "Stock"
                        : asset.name || asset.ticker || "Physical Asset"}
                    </strong>
                    {(asset.type?.toUpperCase() === "STOCK"
                      ? asset.name && asset.name !== asset.ticker
                      : asset.ticker && asset.ticker !== asset.name) && (
                      <>
                        <br />
                        <span className="asset-name">
                          {asset.type?.toUpperCase() === "STOCK"
                            ? asset.name
                            : asset.ticker}
                        </span>
                      </>
                    )}
                  </td>
                  <td className="fin-td">
                    <span className={`badge ${asset.type?.toLowerCase()} badge--${displayType.toLowerCase()}`}>
                      {displayType}
                    </span>
                  </td>
                  <td className="fin-td text-right">
                    {qty.toLocaleString()}{" "}
                    {asset.type?.toUpperCase() === "STOCK" ? "Shares" : "Oz"}
                  </td>
                  <td className="fin-td text-right">
                    {price === null ? "Unavailable" : `$${price.toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                    })}`}
                  </td>
                  <td className="fin-td text-right font-bold text-success">
                    ${amountPaid.toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}
                  </td>
                  <td className="fin-td text-right font-bold text-success">
                    {valuation === null ? "Unavailable" : `$${valuation.toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}`}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
