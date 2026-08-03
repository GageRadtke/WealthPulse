import React, { useMemo, useState } from "react";
import {
  calculateTotalPaidForMetals,
  calculateMetalCostDiff,
  convertGramsToTroyOunces,
  calculateJunkSilverOunces,
} from "../../../../shared/finance/metalCalculations.js";
import AssetTable from "../../../assets/components/tables/AssetTable.jsx";
import { calculateTotalWealth } from "../../../../shared/finance/portfolioCalculations.js";

/**
 * MetalsInDepthView
 *
 * Page-level composer for the "Metals In-Depth" dashboard tab.
 * Displays key precious-metals portfolio statistics and the full
 * metals holdings table.
 *
 * @param {Object}   props
 * @param {Object[]} props.metalAssets - Filtered metal asset objects from the portfolio
 * @param {Function} props.onDelete    - Callback to delete an asset by id
 * @param {Function} props.onUpdate    - Callback to update an asset's quantity
 */
export default function MetalsInDepthView({
  metalAssets = [],
  onDelete,
  onUpdate,
  onPurityUpdate,
}) {
  const [grams, setGrams] = useState("");
  const [junkSilverFaceValue, setJunkSilverFaceValue] = useState("");
  const gramsValue = Number(grams);
  const troyOunces = grams !== "" && Number.isFinite(gramsValue) && gramsValue >= 0
    ? convertGramsToTroyOunces(gramsValue)
    : null;
  const faceValue = Number(junkSilverFaceValue);
  const junkSilverOunces = junkSilverFaceValue !== ""
    && Number.isFinite(faceValue)
    && faceValue >= 0
    ? calculateJunkSilverOunces(faceValue)
    : null;

  const totalMeltValue = useMemo(
    () => calculateTotalWealth(metalAssets),
    [metalAssets],
  );
  const totalPaid = useMemo(
    () => calculateTotalPaidForMetals(metalAssets),
    [metalAssets],
  );
  const costDiff = useMemo(
    () => calculateMetalCostDiff(totalMeltValue, totalPaid),
    [totalMeltValue, totalPaid],
  );
  const isPositive = costDiff >= 0;

  return (
    <div className="mid-page">
      <header className="metal-summary-bar">
        <div className="metal-summary-bar__title">
          <span>Metals Portfolio</span>
          <h2>Valuation Summary</h2>
        </div>
        <div className="metal-summary-bar__metric">
          <span>Total Melt Value</span>
          <strong>
            $
            {totalMeltValue.toLocaleString(undefined, {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            })}
          </strong>
        </div>

        <div className="metal-summary-bar__metric">
          <span>Total Amount Paid</span>
          <strong>
            $
            {totalPaid.toLocaleString(undefined, {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            })}
          </strong>
        </div>

        <div className="metal-summary-bar__metric">
          <span>Cost vs. Melt Differential</span>
          <strong className={isPositive ? "text-success" : "text-danger"}>
            {isPositive ? "+" : ""}$
            {Math.abs(costDiff).toLocaleString(undefined, {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            })}
          </strong>
        </div>
      </header>

      <section className="metal-converter" aria-labelledby="metal-converter-title">
        <div className="metal-converter__intro">
          <span className="metal-converter__eyebrow">Weight Conversion</span>
          <h3 id="metal-converter-title">Grams to Troy Ounces</h3>
          <p>Precious metals use troy ounces. One troy ounce equals 31.1034768 grams.</p>
        </div>
        <div className="metal-converter__fields">
          <label>
            <span>Weight in grams</span>
            <div className="metal-converter__input">
              <input
                type="number"
                min="0"
                step="any"
                inputMode="decimal"
                value={grams}
                onChange={(event) => setGrams(event.target.value)}
                placeholder="Enter grams"
                aria-describedby="metal-converter-result"
              />
              <span>g</span>
            </div>
          </label>
          <span className="metal-converter__equals" aria-hidden="true">=</span>
          <div className="metal-converter__result" id="metal-converter-result" aria-live="polite">
            <span>Troy ounces</span>
            <strong>{troyOunces === null ? "—" : troyOunces.toFixed(4)}</strong>
            <small>oz t</small>
          </div>
          <button
            type="button"
            className="secondary-btn"
            onClick={() => setGrams("")}
            disabled={grams === ""}
          >
            Clear
          </button>
        </div>
      </section>

      <section className="metal-converter junk-silver-converter" aria-labelledby="junk-silver-title">
        <div className="metal-converter__intro">
          <span className="metal-converter__eyebrow">90% U.S. Coin Silver</span>
          <h3 id="junk-silver-title">Junk Silver Calculator</h3>
          <p>Estimate silver weight from face value using 0.715 troy ounces per $1 face value.</p>
        </div>
        <div className="metal-converter__fields">
          <label>
            <span>Coin face value</span>
            <div className="metal-converter__input">
              <span className="junk-silver-converter__currency">$</span>
              <input
                type="number"
                min="0"
                step="any"
                inputMode="decimal"
                value={junkSilverFaceValue}
                onChange={(event) => setJunkSilverFaceValue(event.target.value)}
                placeholder="Enter face value"
                aria-describedby="junk-silver-result"
              />
            </div>
          </label>
          <span className="metal-converter__equals" aria-hidden="true">=</span>
          <div className="metal-converter__result" id="junk-silver-result" aria-live="polite">
            <span>Estimated silver weight</span>
            <strong>{junkSilverOunces === null ? "—" : junkSilverOunces.toFixed(3)}</strong>
            <small>oz t</small>
          </div>
          <button
            type="button"
            className="secondary-btn"
            onClick={() => setJunkSilverFaceValue("")}
            disabled={junkSilverFaceValue === ""}
          >
            Clear
          </button>
        </div>
      </section>

      {/* ── Holdings Table ── */}
      <section className="mid-table-section">
        <h3 className="mid-section-title">Precious Metals Holdings</h3>
        <AssetTable
          assets={metalAssets}
          onDelete={onDelete}
          onUpdate={onUpdate}
          onPurityUpdate={onPurityUpdate}
        />
      </section>
    </div>
  );
}
