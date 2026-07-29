import React from "react";
import { getMetalPurityOptions, METAL_MARKETS, METAL_UNITS } from "../../constants/assets";

export default function MetalAssetFields({ formData, handleChange, errors }) {
  return (
    <>
      <div className="form-group">
        <label>Metal Name *</label>
        <input
          type="text"
          name="name"
          placeholder="e.g. Gold Eagle, Silver Bar"
          value={formData.name}
          onChange={handleChange}
          className={errors.name ? "input-error" : ""}
        />
        {errors.name && <p className="error-message">{errors.name}</p>}
      </div>

      <div className="form-group">
        <label>Market metal</label>
        <select
          name="metalSymbol"
          value={formData.metalSymbol}
          onChange={handleChange}
          className="form-select"
        >
          {METAL_MARKETS.map(({ value, label }) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label>Purity</label>
        <select
          name="purityKarat"
          value={formData.purityKarat}
          onChange={handleChange}
          className="form-select"
        >
          {getMetalPurityOptions(formData.metalSymbol).map(({ value, label }) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label>Unit</label>
        <select
          name="unit"
          value={formData.unit}
          onChange={handleChange}
          className="form-select"
        >
          {METAL_UNITS.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </div>
    </>
  );
}
