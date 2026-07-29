import React, { useState } from "react";
import { useDiversifier } from "../hooks/useDiversifier";
import { formatCurrency } from "../../../shared/utils/formatters";
import { calculateMetalAllocation } from "../../../shared/finance/metalCalculations";

/**
 * ResultBadge — simple styled wrapper for displaying a calculated result.
 */
function ResultBadge({ variant = "", children }) {
  return <div className={`result-badge ${variant}`}>{children}</div>;
}

/**
 * NumberField — a labelled numeric input.
 */
function NumberField({ label, value, onChange }) {
  return (
    <div className="learning-number-field">
      <label className="learning-field-label">{label}</label>
      <input
        type="number"
        className="learning-field-input"
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        min="0"
        step="any"
      />
    </div>
  );
}

/**
 * DiversifierTool
 *
 * Interactive learning visualizer used in two contexts:
 *   - "equities"  → Options Call/Put intrinsic value calculator
 *   - "stacking"  → Precious metals cost basis & allocation simulator
 *
 * @param {Object} props
 * @param {"equities"|"stacking"} props.contextType - Which tool variant to render
 */
export default function DiversifierTool({ contextType = "equities" }) {
  const {
    stockPrice,
    setStockPrice,
    strikePrice,
    setStrikePrice,
    callValue,
    putValue,
    totalSpent,
    setTotalSpent,
    totalOunces,
    setTotalOunces,
    calculatedBasis,
  } = useDiversifier();

  const [silverRatio, setSilverRatio] = useState(10);

  // ── Equities: Options Intrinsic Value Calculator ─────────────────
  if (contextType === "equities") {
    return (
      <div className="learning-visualizer-panel">
        <h3>Options Intrinsic Value Calculator</h3>
        <p className="learning-tool-hint">
          Enter a stock price and strike price to see the intrinsic value of a
          Call and Put option at expiration.
        </p>

        <div className="learning-control-row">
          <NumberField
            label="Stock Market Price ($)"
            value={stockPrice}
            onChange={setStockPrice}
          />
          <NumberField
            label="Option Strike Price ($)"
            value={strikePrice}
            onChange={setStrikePrice}
          />
        </div>

        <div className="learning-results-display">
          <ResultBadge variant="learning-success-badge">
            <strong>Call Intrinsic Value:</strong> {formatCurrency(callValue)}
          </ResultBadge>
          <ResultBadge variant="learning-danger-badge">
            <strong>Put Intrinsic Value:</strong> {formatCurrency(putValue)}
          </ResultBadge>
        </div>
      </div>
    );
  }

  // ── Stacking: Cost Basis & Allocation Simulator ──────────────────
  if (contextType === "stacking") {
    const { goldOunces, silverOunces } = calculateMetalAllocation(totalOunces, silverRatio);

    return (
      <div className="learning-visualizer-panel">
        <h3>Dollar-Cost Averaging Cost Basis Simulator</h3>
        <p className="learning-tool-hint">
          Calculate your exact precious metals cost basis and model custom asset
          allocations.
        </p>

        <div className="learning-control-row">
          <NumberField
            label="Total Fiat Capital Expended ($)"
            value={totalSpent}
            onChange={setTotalSpent}
          />
          <NumberField
            label="Total Troy Ounces Acquired (oz)"
            value={totalOunces}
            onChange={setTotalOunces}
          />
        </div>

        {/* Dynamic Allocation Slider Section */}
        <div className="learning-slider-container">
          <div className="learning-slider-header">
            <span className="learning-slider-label">
              Silver to Gold Target Ratio
            </span>
            <span className="learning-slider-value">
              <strong>{silverRatio}:1</strong>
            </span>
          </div>
          <input
            type="range"
            min="1"
            max="120"
            value={silverRatio}
            onChange={(e) => setSilverRatio(Number(e.target.value))}
            className="learning-ratio-slider"
          />
          <div className="learning-allocation-breakdown">
            <div>
              Gold Target Allocation:{" "}
              <strong>{goldOunces.toFixed(2)} oz</strong>
            </div>
            <div>
              Silver Target Allocation:{" "}
              <strong>{silverOunces.toFixed(2)} oz</strong>
            </div>
          </div>
        </div>

        <div className="learning-results-display">
          <ResultBadge variant="learning-success-badge">
            <strong>Simulated True Cost Basis:</strong>{" "}
            {formatCurrency(calculatedBasis)} / troy oz
          </ResultBadge>
        </div>
      </div>
    );
  }

  return null;
}
