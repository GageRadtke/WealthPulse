import { useState } from "react";
import { parseCsvAssets } from "../utils/csvParser";

export default function BulkUploadForm({ onAssetAdded }) {
  const [bulkText, setBulkText] = useState("");
  const [isProcessingBulk, setIsProcessingBulk] = useState(false);

  const handleBulkSubmit = async (event) => {
    event.preventDefault();

    if (!bulkText.trim()) {
      alert("Please paste CSV data before clicking process.");
      return;
    }

    if (typeof onAssetAdded !== "function") {
      alert("Bulk upload is unavailable because the portfolio save handler is missing.");
      return;
    }

    const parseResult = parseCsvAssets(bulkText);
    const parsedAssets = parseResult.payloads;
    const skippedCount = parseResult.skippedCount;

    if (parsedAssets.length === 0) {
      alert(
        `Bulk upload failed. Processed 0 entries. Check developer console for layout errors.`,
      );
      return;
    }

    try {
      setIsProcessingBulk(true);
      // Process in order so repeated tickers/product names always see the
      // previously updated quantity and cost basis before the next merge.
      let savedCount = 0;
      let failedCount = 0;
      for (const assetPayload of parsedAssets) {
        try {
          await onAssetAdded(assetPayload);
          savedCount += 1;
        } catch {
          failedCount += 1;
        }
      }

      if (savedCount > 0) {
        setBulkText("");
      }

      alert(
        `Bulk Process Complete!\nSuccessfully saved: ${savedCount} records.\nSkipped/Invalid rows: ${skippedCount}\nBackend failures: ${failedCount}`,
      );
    } finally {
      setIsProcessingBulk(false);
    }
  };

  return (
    <div className="form-card bulk-card">
      <h3 className="form-title">Bulk Add Positions</h3>
      <p className="form-subtitle">
        Paste 7-column comma-separated lines directly matching the layout
        framework.
      </p>

      <form onSubmit={handleBulkSubmit} className="wealth-pulse-form">
        <div className="form-group">
          <label>
            CSV Data Input (Type, Ticker, Name, Sector, Qty, AmountPaid)
          </label>
          <textarea
            placeholder="STOCK,AAPL,Apple Inc,Technology,10,1755.00&#10;METAL,GOLD,Gold Bullion,Precious Metals,4700.00"
            rows="8"
            className="bulk-textarea"
            value={bulkText}
            onChange={(event) => setBulkText(event.target.value)}
          />
        </div>
        <button
          type="submit"
          className="secondary-btn"
          disabled={isProcessingBulk}
        >
          {isProcessingBulk ? "Processing..." : "Process Bulk Upload"}
        </button>
      </form>
    </div>
  );
}
