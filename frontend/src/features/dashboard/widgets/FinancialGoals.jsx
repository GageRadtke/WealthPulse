import React, { useMemo, useState } from "react";
import {
  calculateTotalWealth,
  calculateProgressPercent,
  calculateRemainingGoalAmount,
} from "../../../shared/finance/portfolioCalculations";

export default function FinancialGoals({ assets, stockAssets = [], metalAssets = [] }) {
  const [targetWealth, setTargetWealth] = useState(1000000);

  // The dashboard supplies grouped assets; keep the individual arrays as a
  // backwards-compatible fallback for other consumers of this widget.
  const portfolioAssets = useMemo(
    () => [
      ...(assets?.stocks ?? stockAssets),
      ...(assets?.metals ?? metalAssets),
    ],
    [assets, stockAssets, metalAssets],
  );

  const currentWealth = useMemo(
    () => calculateTotalWealth(portfolioAssets),
    [portfolioAssets],
  );
  const progressPercent = useMemo(
    () => calculateProgressPercent(currentWealth, targetWealth),
    [currentWealth, targetWealth]
  );
  const remainingAmount = useMemo(
    () => calculateRemainingGoalAmount(currentWealth, targetWealth),
    [currentWealth, targetWealth]
  );

  return (
    <section className="mini-card goals-card">
      <div className="card-heading-row">
        <div>
          <h4>How Close Are You to Your Portfolio Goal</h4>
          <p className="muted-text">
            Track progress against your next wealth milestone.
          </p>
        </div>
      </div>

      <div className="goal-input-group">
        <label htmlFor="targetWealth">Target value</label>
        <input
          id="targetWealth"
          type="number"
          min="0"
          step="100"
          value={targetWealth}
          onChange={(event) => setTargetWealth(event.target.valueAsNumber || 0)}
        />
      </div>

      <div className="goal-metrics">
        <span>${currentWealth.toFixed(2)} tracked</span>
        <span>${remainingAmount.toFixed(2)} remaining</span>
      </div>

      <div className="progress-container" aria-label="Portfolio goal progress">
        <div
          className="progress-bar"
          style={{ width: `${progressPercent}%` }}
        />
      </div>
      <p className="goal-progress-text">{progressPercent.toFixed(1)}% funded</p>
    </section>
  );
}
