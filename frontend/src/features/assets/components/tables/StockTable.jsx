import { useState } from "react";
import QuantityEditor from "../QuantityEditor";
import { formatDateTime } from "../../../../shared/utils/formatters";
import { getAssetDisplayType } from "../../builders/assetBuilder";

export default function StockTable({
  stockAssets,
  hasDiv,
  hasCagr,
  onUpdate,
  onDelete,
}) {
  const [inputQuantities, setInputQuantities] = useState({});

  const handleInputChange = (assetId, value) => {
    setInputQuantities((previous) => ({ ...previous, [assetId]: value }));
  };

  const renderHoldingsTable = (title, assets) => {
    if (assets.length === 0) return null;

    return (
      <div className="table-section">
        <h3 className="section-title">{title}</h3>
        <div className="table-scroll-wrapper">
          <table className="portfolio-table">
            <thead>
              <tr>
                <th className="text-left">Asset</th>
                <th className="text-left">Sector</th>
                <th className="text-left">Type</th>
                <th className="text-right">Qty (Shares)</th>
                <th className="text-right">Market Price</th>
                <th className="text-right">Market Value</th>
                <th className="text-right">Amount Paid</th>
                {hasDiv && <th className="text-right">Div Yield</th>}
                {hasDiv && <th className="text-right">Div Rate</th>}
                {hasCagr && <th className="text-right">5yr CAGR</th>}
                <th className="text-center">Last Updated</th>
                <th className="text-center">Update Qty</th>
                <th className="text-center">Action</th>
              </tr>
            </thead>
            <tbody>
              {assets.map((asset) => (
                <tr key={asset.id}>
                  <td className="text-left">
                    <strong className="asset-ticker">{asset.ticker}</strong>
                    <br />
                    <span className="asset-name">{asset.name}</span>
                  </td>
                  <td className="text-left sector-text">
                    {asset.sector ?? "General"}
                  </td>
                  <td className="text-left">
                    <span
                      className={`sub-type-badge sub-type-badge--${getAssetDisplayType(asset).toLowerCase()}`}
                    >
                      {getAssetDisplayType(asset)}
                    </span>
                  </td>

                  <td className="text-right quantity-text">
                    {(asset.quantity ?? 0).toFixed(3)}
                  </td>
                  <td className="text-right">
                    ${(asset.price ?? 0).toFixed(2)}
                  </td>
                  <td className="text-right font-bold text-success">
                    ${(Number(asset.price ?? 0) * Number(asset.quantity ?? 0)).toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}
                  </td>
                  <td className="text-right">
                    ${(asset.amountPaid ?? 0).toFixed(2)}
                  </td>
                  {hasDiv && (
                    <td className="text-right">
                      {asset.dividendYield != null
                        ? `${(asset.dividendYield * 100).toFixed(2)}%`
                        : "—"}
                    </td>
                  )}
                  {hasDiv && <td className="text-right">{asset.divRate != null ? `$${Number(asset.divRate).toFixed(2)}` : "—"}</td>}
                  {hasCagr && (
                    <td className="text-right">
                      {asset.cagr5Yr != null ? (
                        <span
                          className={
                            asset.cagr5Yr >= 0 ? "text-success" : "text-danger"
                          }
                        >
                          {asset.cagr5Yr >= 0 ? "+" : ""}
                          {Number(asset.cagr5Yr).toFixed(1)}%
                        </span>
                      ) : (
                        "—"
                      )}
                    </td>
                  )}
                  <td className="text-center timestamp-text">
                    {formatDateTime(asset.lastUpdated)}
                  </td>

                  <td className="text-center">
                    <QuantityEditor
                      assetId={asset.id}
                      quantity={inputQuantities[asset.id] || ""}
                      onQuantityChange={handleInputChange}
                      onUpdate={onUpdate}
                    />
                  </td>

                  <td className="text-center">
                    <button
                      onClick={() => onDelete(asset.id)}
                      className="btn-remove-asset"
                    >
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const stocks = stockAssets.filter(
    (asset) => !["BOND", "ETF"].includes(asset.assetSubType?.toUpperCase()),
  );
  const bonds = stockAssets.filter(
    (asset) => asset.assetSubType?.toUpperCase() === "BOND",
  );
  const etfs = stockAssets.filter(
    (asset) => asset.assetSubType?.toUpperCase() === "ETF",
  );

  return (
    <>
      {renderHoldingsTable("Stock Holdings", stocks)}
      {renderHoldingsTable("Bond Holdings", bonds)}
      {renderHoldingsTable("ETF Holdings", etfs)}
    </>
  );
}
