import React, { useState } from "react";
import {
  CURRENT_GOLD_MARKET_PRICE,
  CURRENT_SILVER_MARKET_PRICE,
} from "../../../shared/finance/metalRates";
import {
  getCurrentMetalPrice,
  getStackBreakdown,
} from "../../../shared/finance/metalCalculations";
import {
  calculateSplitAllocations,
  calculateTargetedAllocation,
  calculateTotalWealth,
} from "../../../shared/finance/portfolioCalculations";
import { formatCurrency, formatMetalSize } from "../../../shared/utils/formatters";

export default function PortfolioDiversifier({
  stockAssets = [],
  metalAssets = [],
  assets,
}) {
  const stocks = assets?.stocks || stockAssets;
  const metals = assets?.metals || metalAssets;

  const [targetPercent, setTargetPercent] = useState(10);
  const [goldPercent, setGoldPercent] = useState(50); // Default 50% Gold, 50% Silver

  const totalWealth = calculateTotalWealth(stocks);
  const targetedValue = calculateTargetedAllocation(totalWealth, targetPercent);

  const { goldTarget, silverTarget } = calculateSplitAllocations(
    targetedValue,
    goldPercent,
  );

  const goldMarketPrice = getCurrentMetalPrice(
    metals,
    "gold",
    CURRENT_GOLD_MARKET_PRICE,
  );
  const silverMarketPrice = getCurrentMetalPrice(
    metals,
    "silver",
    CURRENT_SILVER_MARKET_PRICE,
  );

  const goldStack = getStackBreakdown(goldTarget, goldMarketPrice);
  const silverStack = getStackBreakdown(silverTarget, silverMarketPrice);

  return (
    <div className="mini-card diversifier-card">
      <h4>Portfolio Diversifier Tool For Stackers</h4>
      <div className="goal-input-group">
        <label>Allocation Goal of Current Portfolio Wealth (%):</label>
        <input
          type="number"
          value={targetPercent}
          onChange={(e) => setTargetPercent(Number(e.target.value))}
          max="100"
        />
      </div>

      <div className="goal-input-group diversifier-split-control">
        <label>Gold vs Silver Allocation Split:</label>
        <div className="diversifier-split-labels">
          <span>
            Gold: <strong>{goldPercent}%</strong>
          </span>
          <span>
            Silver: <strong>{100 - goldPercent}%</strong>
          </span>
        </div>
        <input
          className="diversifier-split-slider"
          type="range"
          min="0"
          max="100"
          value={goldPercent}
          onChange={(e) => setGoldPercent(Number(e.target.value))}
        />
      </div>

      {totalWealth === 0 ? (
        <div className="explanation-textbox empty-state">
          <p>
            Add some assets to your portfolio to see your physical gold and
            silver "stack" suggestion!
          </p>
        </div>
      ) : (
        <>
          <div className="explanation-textbox">
            <p>
              Based on your current wealth, {formatCurrency(totalWealth)} if you allocated{" "}
              <strong>{targetPercent}%</strong> to precious metals, you would
              need <strong>{formatCurrency(goldTarget)}</strong> in
              gold and <strong>{formatCurrency(silverTarget)}</strong>{" "}
              in silver.
            </p>
          </div>

          <div className="diversifier-output">
            <h5>Your Physical Stack Suggestion:</h5>
            <div className="stack-suggestion">
              <div className="metal-group">
                <strong>Gold Pieces:</strong>
                {goldStack.length > 0 ? (
                  <ul>
                    {goldStack.map((item, i) => (
                      <li key={i}>
                        {item.count} x {formatMetalSize(item.size)} rounds/bars
                      </li>
                    ))}
                  </ul>
                ) : (
                  <span> None</span>
                )}
              </div>

              <div className="metal-group">
                <strong>Silver Pieces:</strong>
                {silverStack.length > 0 ? (
                  <ul>
                    {silverStack.map((item, i) => (
                      <li key={i}>
                        {item.count} x {formatMetalSize(item.size)} rounds/bars
                      </li>
                    ))}
                  </ul>
                ) : (
                  <span> None</span>
                )}
              </div>
            </div>
            <small className="muted-text">
              Current market prices: Gold {formatCurrency(goldMarketPrice)}/oz | Silver{" "}
              {formatCurrency(silverMarketPrice)}/oz.
            </small>
          </div>
        </>
      )}
    </div>
  );
}
