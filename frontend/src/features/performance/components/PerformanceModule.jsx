import { useMemo, useState } from "react";
import {
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip,
} from "chart.js";
import { Line } from "react-chartjs-2";
import usePortfolioPerformance from "../hooks/usePortfolioPerformance";

ChartJS.register(CategoryScale, Legend, LinearScale, LineElement, PointElement, Tooltip);

const PERIODS = ["1M", "3M", "1Y", "5Y", "ALL"];
const money = (value) =>
  Number(value || 0).toLocaleString(undefined, {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
  });
const signedMoney = (value) => `${Number(value) >= 0 ? "+" : ""}${money(value)}`;
const signedPercent = (value) => `${Number(value) >= 0 ? "+" : ""}${Number(value || 0).toFixed(2)}%`;

function Metric({ label, value, tone = "" }) {
  return (
    <div className="performance-metric">
      <span>{label}</span>
      <strong className={tone}>{value}</strong>
    </div>
  );
}

function PerformerList({ title, rows = [] }) {
  return (
    <div className="performance-performers-list">
      <h5>{title}</h5>
      {rows.length ? rows.map((row) => (
        <div key={`${title}-${row.symbol}`} className="performance-performer-row">
          <span><strong>{row.symbol}</strong><small>{row.name}</small></span>
          <span className={row.gain >= 0 ? "text-success" : "text-danger"}>
            {signedPercent(row.returnPercent)} · {signedMoney(row.gain)}
          </span>
        </div>
      )) : <p className="muted-text">No priced holdings with cost basis.</p>}
    </div>
  );
}

export default function PerformanceModule({ refreshKey = 0 }) {
  const [period, setPeriod] = useState("1Y");
  const [benchmark, setBenchmark] = useState("SPY");
  const { data, loading, error } = usePortfolioPerformance(period, benchmark, refreshKey);

  const chartData = useMemo(() => ({
    labels: data?.series?.map((point) => point.date) || [],
    datasets: [
      {
        label: "Your portfolio",
        data: data?.series?.map((point) => point.portfolioIndex) || [],
        borderColor: "#8b5cf6",
        backgroundColor: "rgba(139, 92, 246, .15)",
        tension: 0.25,
      },
      {
        label: benchmark,
        data: data?.series?.map((point) => point.benchmarkIndex) || [],
        borderColor: "#22c55e",
        backgroundColor: "transparent",
        tension: 0.25,
      },
    ],
  }), [data, benchmark]);

  return (
    <section className="mini-card performance-module">
      <header className="performance-header">
        <div>
          <h4>Portfolio Performance</h4>
          <p className="muted-text">Growth, contributions, cost basis, and benchmark comparison.</p>
        </div>
        <div className="performance-controls">
          <div className="performance-periods" aria-label="Performance timeframe">
            {PERIODS.map((value) => (
              <button
                type="button"
                key={value}
                className={period === value ? "active" : ""}
                onClick={() => setPeriod(value)}
              >
                {value === "ALL" ? "All" : value}
              </button>
            ))}
          </div>
          <label>
            Benchmark
            <select value={benchmark} onChange={(event) => setBenchmark(event.target.value)}>
              <option value="SPY">S&amp;P 500 (SPY)</option>
              <option value="VTI">U.S. Market (VTI)</option>
              <option value="QQQ">Nasdaq-100 (QQQ)</option>
            </select>
          </label>
        </div>
      </header>

      {loading && <p className="muted-text">Calculating performance…</p>}
      {error && <p className="news-error-text">{error}</p>}
      {!loading && data && (
        <>
          {data.historyMessage && <p className="performance-history-note">{data.historyMessage}</p>}
          <div className="performance-metrics">
            <Metric label="Current value" value={money(data.summary.currentValue)} />
            <Metric label="Cost basis" value={money(data.summary.costBasis)} />
            <Metric
              label="Investment growth"
              value={signedMoney(data.summary.investmentGrowth)}
              tone={data.summary.investmentGrowth >= 0 ? "text-success" : "text-danger"}
            />
            <Metric
              label="Unrealized gain/loss"
              value={signedMoney(data.summary.unrealizedGain)}
              tone={data.summary.unrealizedGain >= 0 ? "text-success" : "text-danger"}
            />
            <Metric
              label="Realized gain/loss"
              value={signedMoney(data.summary.realizedGain)}
              tone={data.summary.realizedGain >= 0 ? "text-success" : "text-danger"}
            />
            <Metric label="Net contributions" value={money(data.summary.netContributions)} />
          </div>

          <div className="performance-chart">
            {data.series?.length > 1 ? (
              <Line
                data={chartData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: { legend: { labels: { color: "#cbd5e1" } } },
                  scales: {
                    x: { ticks: { color: "#94a3b8", maxTicksLimit: 8 }, grid: { display: false } },
                    y: { ticks: { color: "#94a3b8" }, grid: { color: "rgba(148,163,184,.15)" } },
                  },
                }}
              />
            ) : (
              <div className="performance-empty-chart">
                Your first daily snapshot is saved. The comparison chart will appear after more history is recorded.
              </div>
            )}
          </div>

          <div className="performance-comparison">
            <Metric label="Portfolio return" value={signedPercent(data.summary.portfolioReturnPercent)} />
            <Metric
              label={`${benchmark} return`}
              value={data.summary.benchmarkAvailable
                ? signedPercent(data.summary.benchmarkReturnPercent)
                : "Unavailable"}
            />
          </div>

          <div className="performance-bottom-grid">
            <div>
              <h5>Allocation change</h5>
              <div className="allocation-change-grid">
                {Object.entries(data.allocation.end || {}).map(([name, endValue]) => (
                  <div key={name}>
                    <span>{name}</span>
                    <strong>{Number(data.allocation.start?.[name] || 0).toFixed(1)}% → {Number(endValue).toFixed(1)}%</strong>
                  </div>
                ))}
              </div>
            </div>
            <div className="performance-performers">
              <PerformerList title="Best performers" rows={data.performers?.best} />
              <PerformerList title="Worst performers" rows={data.performers?.worst} />
            </div>
          </div>
        </>
      )}
    </section>
  );
}
