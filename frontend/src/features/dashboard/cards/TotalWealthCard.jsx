import { formatCurrency } from "../../../shared/utils/formatters";
import { calculateTotalWealth } from "../../../shared/finance/portfolioCalculations";

export default function TotalWealthCard({ assets }) {
  const stocksTotal = calculateTotalWealth(assets.stocks);
  const metalsTotal = calculateTotalWealth(assets.metals);

  const totalWealth = stocksTotal + metalsTotal;

  return (
    <div className="mini-card wealth-card">
      <h4>Total Calculated Retirement Wealth</h4>

      <p className="money">{formatCurrency(totalWealth)}</p>

      <div className="wealth-breakdown">
        <span>Stocks: {formatCurrency(stocksTotal)}</span>
        <span>Metals: {formatCurrency(metalsTotal)}</span>
      </div>

      <div className="warning-note">
        <strong>⚠ Disclaimer:</strong>
        This represents tracked retirement assets. Real-world wealth also
        includes real estate, cash, liabilities, and other investments.
      </div>
    </div>
  );
}
