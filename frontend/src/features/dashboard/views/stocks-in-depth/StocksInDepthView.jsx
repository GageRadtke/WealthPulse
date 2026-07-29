import React from "react";
import AssetTable from "../../../assets/components/tables/AssetTable";
import DashboardCharts from "../metals-in-depth/DashboardCharts";

/**
 * StocksInDepthView
 *stockAssets array and
 * renders the full analytical suite for stock holdings.
 *
 * @param {Object}   props
 * @param {Object[]} props.stockAssets  - Filtered stock asset objects from the portfolio
 * @param {Function} props.onDelete     - Callback to delete an asset by id
 * @param {Function} props.onUpdate     - Callback to update an asset's quantity
 */
export default function StocksInDepthView({
  stockAssets = [],
  onDelete,
  onUpdate,
}) {
  return (
    <div className="sid-page">
      {/* ── Holdings ── */}
      <section className="sid-table-section">
        <h3 className="sid-section-title">Stock Equity Holdings</h3>
        <AssetTable
          assets={stockAssets}
          onDelete={onDelete}
          onUpdate={onUpdate}
        />
      </section>

      <DashboardCharts
        stockAssets={stockAssets}
      />
    </div>
  );
}
