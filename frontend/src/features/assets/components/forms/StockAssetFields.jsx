import React from "react";
import { ASSET_SUB_TYPES } from "../../constants/assets";

export default function StockAssetFields({ formData, handleChange, setFormData, errors }) {
  return (
    <>
      <div className="form-group">
        <label>Asset Sub-Type</label>
        <div className="toggle-btn-group">
          {ASSET_SUB_TYPES.map(({ value, label }) => (
            <button
              key={value}
              type="button"
              className={`btn-toggle btn-toggle--sm ${
                formData.assetSubType === value ? "active" : ""
              }`}
              onClick={() =>
                setFormData((prev) => ({ ...prev, assetSubType: value }))
              }
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="form-group">
        <label>Ticker Symbol *</label>
        <input
          type="text"
          name="ticker"
          placeholder="e.g. AAPL"
          value={formData.ticker}
          onChange={handleChange}
          className={errors.ticker ? "input-error" : ""}
        />
        {errors.ticker && <p className="error-message">{errors.ticker}</p>}
      </div>

      <div className="form-group">
        <label>Company Name</label>
        <input
          type="text"
          name="name"
          placeholder="e.g. Apple Inc."
          value={formData.name}
          onChange={handleChange}
        />
      </div>

      <div className="form-group">
        <label>Sector</label>
        <input
          type="text"
          name="sector"
          placeholder="e.g. Technology"
          value={formData.sector}
          onChange={handleChange}
        />
      </div>

    </>
  );
}
