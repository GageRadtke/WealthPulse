import React, { useMemo } from "react";
import { Bar, Doughnut, Line, Scatter } from "react-chartjs-2";
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip,
} from "chart.js";

ChartJS.register(
  ArcElement,
  BarElement,
  CategoryScale,
  Filler,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip,
);

const COLORS = ["#8b5cf6", "#14b8a6", "#3b82f6", "#fbbf24", "#f43f5e", "#84cc16", "#f97316", "#06b6d4", "#ec4899", "#94a3b8"];
const number = (value) => Number(value ?? 0) || 0;
const percent = (value) => {
  const parsed = number(value);
  return Math.abs(parsed) <= 1 ? parsed * 100 : parsed;
};
const valueOf = (asset) => number(asset.quantity) * number(asset.price);
const tickerOf = (asset) => asset.ticker || asset.name || "Unknown";
const annualIncomeOf = (asset) =>
  number(asset.divRate) > 0
    ? number(asset.quantity) * number(asset.divRate)
    : valueOf(asset) * (percent(asset.dividendYield) / 100);

const baseOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: "right", labels: { color: "#cbd5e1", boxWidth: 12, usePointStyle: true } },
    tooltip: { backgroundColor: "#111827", borderColor: "#334155", borderWidth: 1 },
  },
  scales: {
    x: { ticks: { color: "#94a3b8" }, grid: { display: false } },
    y: { beginAtZero: true, ticks: { color: "#94a3b8" }, grid: { color: "rgba(148,163,184,.15)" } },
  },
};

function ChartCard({ title, children, wide = false }) {
  return (
    <article className={`stock-chart-card${wide ? " stock-chart-card--wide" : ""}`}>
      <h4>{title}</h4>
      <div className="stock-chart-canvas">{children}</div>
    </article>
  );
}

function groupValues(assets, keySelector) {
  const grouped = {};
  assets.forEach((asset) => {
    const key = keySelector(asset) || "Unclassified";
    grouped[key] = (grouped[key] || 0) + valueOf(asset);
  });
  return Object.entries(grouped).sort((a, b) => b[1] - a[1]);
}

export default function DashboardCharts({ stockAssets = [], metalAssets = [], showDiversification = false }) {
  // Defense in depth: chart data is filtered here even when a caller passes a
  // mixed portfolio. Metals can never enter the Stocks In-Depth analytics.
  const stocks = useMemo(
    () => stockAssets.filter((asset) => asset.type?.toUpperCase() === "STOCK"),
    [stockAssets],
  );

  const labels = stocks.map(tickerOf);
  const values = stocks.map(valueOf);
  const incomes = stocks.map(annualIncomeOf);
  const sectors = groupValues(stocks, (asset) => asset.sector || "Unclassified");
  const totalValue = values.reduce((sum, value) => sum + value, 0);
  const annualIncome = incomes.reduce((sum, value) => sum + value, 0);
  const allocationData = { labels, datasets: [{ data: values, backgroundColor: labels.map((_, i) => COLORS[i % COLORS.length]), borderWidth: 0 }] };
  const sectorData = { labels: sectors.map(([sector]) => sector), datasets: [{ data: sectors.map(([, value]) => value), backgroundColor: sectors.map((_, i) => COLORS[(i + 2) % COLORS.length]), borderWidth: 0 }] };

  const trajectory = Array.from({ length: 11 }, (_, year) => {
    const weightedGrowth = stocks.reduce((sum, asset) => sum + valueOf(asset) * (percent(asset.cagr5Yr) / 100), 0);
    const growthRate = totalValue > 0 ? weightedGrowth / totalValue : 0;
    return totalValue * Math.pow(1 + Math.max(-0.25, growthRate), year) + annualIncome * year;
  });

  if (!stocks.length) {
    return <section className="stock-analytics"><p className="stock-analytics-empty">Add a stock asset to populate Stock Analytics.</p></section>;
  }

  if (showDiversification) {
    const metalsValue = metalAssets.reduce((sum, asset) => sum + valueOf(asset), 0);
    return (
      <section className="dashboard-charts">
        <div className="chart-grid">
          <div className="chart-card chart-card--compact">
            <h4>Portfolio Allocation</h4>
            <div className="stock-chart-canvas"><Doughnut data={{ labels: ["Stocks", "Metals"], datasets: [{ data: [totalValue, metalsValue], backgroundColor: ["#8b5cf6", "#f59e0b"], borderWidth: 0 }] }} options={{ ...baseOptions, scales: undefined }} /></div>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="stock-analytics">
      <header className="stock-analytics-header">
        <div><span>Total Stock Value</span><strong>${totalValue.toLocaleString(undefined, { maximumFractionDigits: 0 })}</strong></div>
        <div><span>Annual Income</span><strong>${annualIncome.toLocaleString(undefined, { maximumFractionDigits: 0 })}</strong></div>
        <div><span>Monthly Average</span><strong>${(annualIncome / 12).toLocaleString(undefined, { maximumFractionDigits: 0 })}</strong></div>
        <div><span>Portfolio Yield</span><strong>{totalValue ? ((annualIncome / totalValue) * 100).toFixed(2) : "0.00"}%</strong></div>
      </header>

      <h3>1. Core Portfolio Architecture</h3>
      <div className="stock-chart-grid">
        <ChartCard title="Total Value Allocation"><Doughnut data={allocationData} options={{ ...baseOptions, scales: undefined }} /></ChartCard>
        <ChartCard title="Income Generation (Annual)"><Bar data={{ labels, datasets: [{ label: "Annual Income", data: incomes, backgroundColor: "#22c55e" }] }} options={baseOptions} /></ChartCard>
      </div>

      <h3>2. Risk & Diversification</h3>
      <div className="stock-chart-grid">
        <ChartCard title="Sector Weighting" wide><Doughnut data={sectorData} options={{ ...baseOptions, scales: undefined }} /></ChartCard>
      </div>

      <h3>3. Performance & Growth</h3>
      <div className="stock-chart-grid">
        <ChartCard title="Yield vs. Yield on Cost (YOC)"><Bar data={{ labels, datasets: [{ label: "Current Yield", data: stocks.map((a) => percent(a.dividendYield)), backgroundColor: "#7dd3fc" }, { label: "Yield on Cost", data: stocks.map((a) => number(a.amountPaid) > 0 ? annualIncomeOf(a) / number(a.amountPaid) * 100 : 0), backgroundColor: "#2563eb" }] }} options={baseOptions} /></ChartCard>
        <ChartCard title="5-Year Dividend Growth (CAGR)"><Bar data={{ labels, datasets: [{ label: "Growth %", data: stocks.map((a) => percent(a.cagr5Yr)), backgroundColor: stocks.map((a) => percent(a.cagr5Yr) < 0 ? "#ef4444" : "#3b82f6") }] }} options={baseOptions} /></ChartCard>
      </div>

      <h3>4. Sustainability & Health</h3>
      <div className="stock-chart-grid">
        <ChartCard title="Payout Ratio Heatmap"><Bar data={{ labels, datasets: [{ label: "Payout %", data: stocks.map((a) => percent(a.payoutRatio)), backgroundColor: stocks.map((a) => percent(a.payoutRatio) > 80 ? "#ef4444" : percent(a.payoutRatio) > 60 ? "#f59e0b" : "#10b981") }] }} options={baseOptions} /></ChartCard>
        <ChartCard title="Income Efficiency"><Scatter data={{ datasets: [{ label: "Stocks", data: stocks.map((a) => ({ x: valueOf(a), y: annualIncomeOf(a), ticker: tickerOf(a) })), backgroundColor: stocks.map((_, i) => COLORS[i % COLORS.length]), pointRadius: stocks.map((a) => Math.max(5, Math.min(18, percent(a.dividendYield) * 2))) }] }} options={{ ...baseOptions, parsing: false, plugins: { ...baseOptions.plugins, tooltip: { ...baseOptions.plugins.tooltip, callbacks: { label: ({ raw }) => `${raw.ticker}: $${raw.y.toFixed(2)} income` } } }, scales: { x: { ...baseOptions.scales.x, title: { display: true, text: "Market Value", color: "#94a3b8" } }, y: { ...baseOptions.scales.y, title: { display: true, text: "Annual Income", color: "#94a3b8" } } } }} /></ChartCard>
      </div>

      <h3>5. Portfolio Hierarchy</h3>
      <div className="stock-treemap">
        {stocks.slice().sort((a, b) => valueOf(b) - valueOf(a)).map((asset, index) => (
          <div key={asset.id || tickerOf(asset)} style={{ flexGrow: Math.max(valueOf(asset), 1), background: COLORS[index % COLORS.length] }}>
            <strong>{tickerOf(asset)}</strong><span>{asset.sector || "Unclassified"}</span>
          </div>
        ))}
      </div>

      <h3>6. 10-Year Wealth Trajectory</h3>
      <ChartCard title="Projected Stock Portfolio Value" wide>
        <Line data={{ labels: trajectory.map((_, year) => `Year ${year}`), datasets: [{ label: "Projected Value", data: trajectory, borderColor: "#8b5cf6", backgroundColor: "rgba(139,92,246,.18)", fill: true, tension: .3 }] }} options={baseOptions} />
      </ChartCard>
    </section>
  );
}
