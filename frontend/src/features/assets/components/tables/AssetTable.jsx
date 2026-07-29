import MetalTable from "./MetalTable";
import StockTable from "./StockTable";

export default function AssetTable({ assets = [], onDelete, onUpdate, onPurityUpdate }) {
  const stockAssets = assets.filter(
    (asset) => asset.type?.toUpperCase() === "STOCK", );

  const metalAssets = assets.filter(
    (asset) => asset.type?.toUpperCase() === "METAL", );

  const hasDiv = stockAssets.some( (asset) => asset.dividendYield != null || asset.divRate != null );

const hasCagr = stockAssets.some((asset) => asset.cagr5Yr != null );

  return (
    <div className="portfolio-tables-wrapper">
      {stockAssets.length > 0 && (
        <StockTable
  stockAssets={stockAssets}
  hasDiv={hasDiv}
  hasCagr={hasCagr}
  onDelete={onDelete}
  onUpdate={onUpdate}
/>
      )}

      {metalAssets.length > 0 && (
        <MetalTable
          metalAssets={metalAssets}
          onDelete={onDelete}
          onUpdate={onUpdate}
          onPurityUpdate={onPurityUpdate}
        />
      )}
    </div>
  );
}
