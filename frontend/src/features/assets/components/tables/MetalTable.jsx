import { useState } from "react";
import { formatDateTime } from "../../../../shared/utils/formatters.js";
import { calculateMetalMeltValue } from "../../../../shared/finance/metalCalculations.js";
import { getMetalMarketSymbol } from "../../../../shared/finance/marketPricing";
import { normalizePurityKarat, parseNumber, toNonNegativeNumber } from "../../builders/assetBuilder";
import { getMetalPurityOptions } from "../../constants/assets";
import QuantityEditor from "../QuantityEditor";

function getMarketMetalLabel(asset) {
  const labels = {
    XAU: "Gold",
    XAG: "Silver",
    XPT: "Platinum",
    XPD: "Palladium",
  };
  return labels[getMetalMarketSymbol(asset)];
}

export default function MetalTable({ metalAssets, onUpdate, onDelete, onPurityUpdate }) {
  const [inputQuantities, setInputQuantities] = useState({});
  const [purityError, setPurityError] = useState("");
  const [updatingPurityId, setUpdatingPurityId] = useState(null);

  const handleInputChange = (assetId, value) => {
    setInputQuantities((previous) => ({
      ...previous,
      [assetId]: value,
    }));
  };

  const handlePurityChange = async (assetId, purityKarat) => {
    if (typeof onPurityUpdate !== "function") {
      setPurityError("Purity updates are not connected. Reload the page and try again.");
      return;
    }

    try {
      setPurityError("");
      setUpdatingPurityId(assetId);
      await onPurityUpdate(assetId, purityKarat);
    } catch (error) {
      const message = error.response?.data?.error || error.message;
      setPurityError(`Could not save purity: ${message || "please try again."}`);
    } finally {
      setUpdatingPurityId(null);
    }
  };

  return (
    <div className="table-section">
      <h3 className="section-title">Precious Metals Holdings</h3>
      {purityError && <p className="error-message" role="alert">{purityError}</p>}
      {metalAssets.length === 0 ? (
        <p className="no-assets-text">No physical metals currently added.</p>
      ) : (
        <div className="table-scroll-wrapper">
          <table className="portfolio-table precious-metals-table">
            <thead>
              <tr>
                <th className="text-left">Asset</th>
                <th className="text-left">Market Metal</th>
                <th className="text-left">Purity</th>
                <th className="text-right">Weight</th>
                <th className="text-right">Spot Price (Per Troy Oz)</th>
                <th className="text-right">Melt Value</th>
                <th className="text-right">Amount Paid</th>
                <th className="text-right">Cost Differential</th>
                <th className="text-center">Last Updated</th>
                <th className="text-center">Update Weight</th>
                <th className="text-center">Action</th>
              </tr>
            </thead>
            <tbody>
              {metalAssets.map((asset) => {
                const quantity = toNonNegativeNumber(asset.quantity);
                const marketSymbol = getMetalMarketSymbol(asset);
                const purityKarat = normalizePurityKarat(asset.purityKarat, marketSymbol);
                // `asset.price` is the sole canonical market/spot price. The
                // backend maps all gold product names to XAU before setting it.
                const spotPricePerTroyOunce = parseNumber(asset.price);
                const hasMarketPrice = spotPricePerTroyOunce !== undefined && spotPricePerTroyOunce > 0;
                const meltValue = hasMarketPrice
                  ? calculateMetalMeltValue(quantity, spotPricePerTroyOunce, purityKarat, asset.unit, marketSymbol)
                  : null;
                const totalPaid = toNonNegativeNumber(asset.amountPaid);
                const costDifference = meltValue === null ? null : meltValue - totalPaid;
                const costClass = costDifference !== null && costDifference < 0 ? "negative" : "positive";

                return (
                  <tr key={asset.id}>
                    <td className="text-left">
                      <strong className="asset-ticker">
                        {asset.name || "Physical Asset"}
                      </strong>
                      {asset.ticker && asset.ticker !== asset.name && (
                        <>
                          <br />
                          <span className="asset-name">{asset.ticker}</span>
                        </>
                      )}
                    </td>
                    <td className="text-left">
                      <span className="unit-badge">{getMarketMetalLabel(asset)}</span>
                    </td>
                    <td className="text-left">
                      <select
                        aria-label={`Purity for ${asset.name || "metal asset"}`}
                        className="form-select"
                        value={purityKarat}
                        disabled={updatingPurityId === asset.id}
                        onChange={(event) => handlePurityChange(asset.id, Number(event.target.value))}
                      >
                        {getMetalPurityOptions(marketSymbol).map(({ value, label }) => (
                          <option key={value} value={value}>{label}</option>
                        ))}
                      </select>
                    </td>
                    <td className="text-right quantity-text">
                      {quantity.toFixed(2)} {asset.unit || "oz"}
                    </td>
                    <td className="text-right">
                      {hasMarketPrice ? `$${spotPricePerTroyOunce.toFixed(2)}` : "Unavailable"}
                    </td>
                    <td className="text-right">
                      {meltValue === null ? "Unavailable" : `$${meltValue.toFixed(2)}`}
                    </td>
                    <td className="text-right">${totalPaid.toFixed(2)}</td>
                    <td className={`text-right ${costClass}`}>
                      {costDifference === null
                        ? "Unavailable"
                        : `${costDifference < 0 ? "-" : "+"}$${Math.abs(costDifference).toFixed(2)}`}
                    </td>
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
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
