import React, { useState } from "react";
import { buildAsset } from "../../builders/assetBuilder";
import { validateAssetForm } from "../../validation/assetValidation";

import StockAssetFields from "./StockAssetFields";
import MetalAssetFields from "./MetalAssetFields";
import BondFields from "./BondFields";

// Factory function prevents object reference mutation issues
const createInitialFormState = () => ({
  ticker: "",
  name: "",
  sector: "",
  quantity: "",
  price: "",
  amountPaid: "",
  assetSubType: "STOCK",
  bondRating: "",
  couponRate: "",
  unit: "oz",
  metalSymbol: "XAU",
  purityKarat: "24",
});

export default function AssetForm({ onAssetAdded }) {
  const [type, setType] = useState("STOCK");
  const [formData, setFormData] = useState(createInitialFormState());
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      if (name !== "metalSymbol") return { ...prev, [name]: value };
      const isSilver = value === "XAG";
      const isCurrentPurityCompatible = isSilver
        ? [9999, 999, 958, 950, 935, 925, 900, 835, 830, 800].includes(Number(prev.purityKarat))
        : [10, 14, 18, 22, 24].includes(Number(prev.purityKarat));
      return {
        ...prev,
        metalSymbol: value,
        purityKarat: isCurrentPurityCompatible ? prev.purityKarat : (isSilver ? "999" : "24"),
      };
    });

    // Clear individual field error as user types
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const handleTypeChange = (selectedType) => {
    setType(selectedType);
    setFormData(createInitialFormState());
    setErrors({});
    setSubmitError("");
  };

  const isBond = type === "STOCK" && formData.assetSubType === "BOND";

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError("");

    // Improvement 1 & 3: Modular Validation + Inline Error State
    const validationErrors = validateAssetForm(type, formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    const assetPayload = buildAsset(type, formData);

    if (typeof onAssetAdded !== "function") {
      console.warn("onAssetAdded handler is not yet wired to a state mechanism.");
      return;
    }

    try {
      setIsSaving(true);
      await onAssetAdded(assetPayload);

      setFormData(createInitialFormState());
      setErrors({});
    } catch (err) {
      const serverMsg =
        err?.response?.data?.error || err?.response?.data || err?.message;
      setSubmitError(
        serverMsg
          ? `Server Error: ${serverMsg}`
          : "Could not save asset. Please ensure the backend API is reachable."
      );
      console.error("Asset save error:", err);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="asset-form-inner">
      <h3 className="form-title">Add New Asset</h3>

      {/* Global Submit Error Banner */}
      {submitError && (
        <div className="form-alert error-alert" role="alert">
          {submitError}
        </div>
      )}

      {/* Asset Type Toggle */}
      <div className="form-group type-toggle-buttons">
        <label className="toggle-label">Asset Type</label>
        <div className="toggle-btn-group">
          <button
            type="button"
            className={`btn-toggle ${type === "STOCK" ? "active" : ""}`}
            onClick={() => handleTypeChange("STOCK")}
          >
            Stock / Equity
          </button>
          <button
            type="button"
            className={`btn-toggle ${type === "METAL" ? "active" : ""}`}
            onClick={() => handleTypeChange("METAL")}
          >
            Precious Metal
          </button>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="wealth-pulse-form" noValidate>
        {/* Improvement 2: Isolated field groups */}
        {type === "STOCK" ? (
          <StockAssetFields
            formData={formData}
            handleChange={handleChange}
            setFormData={setFormData}
            errors={errors}
          />
        ) : (
          <MetalAssetFields
            formData={formData}
            handleChange={handleChange}
            errors={errors}
          />
        )}

        {/* Shared Numeric Inputs */}
        <div className="form-group">
          <label>
            {type === "STOCK" ? "Quantity (Shares) *" : "Weight *"}
          </label>
          <input
            type="number"
            step="any"
            name="quantity"
            placeholder={type === "STOCK" ? "0.00" : "1.00"}
            value={formData.quantity}
            onChange={handleChange}
            className={errors.quantity ? "input-error" : ""}
          />
          {errors.quantity && <p className="error-message">{errors.quantity}</p>}
        </div>

        <div className="form-group fallback-spacing">
          <label>Total Amount Paid ($)</label>
          <input
            type="number"
            step="any"
            name="amountPaid"
            placeholder="0.00"
            value={formData.amountPaid}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label>
            {type === "METAL"
              ? "Current Spot Price ($ per Troy Oz, optional)"
              : "Current Market Price ($ per Share, optional)"}
          </label>
          <input
            type="number"
            step="any"
            min="0"
            name="price"
            placeholder="Leave blank to use the market quote"
            value={formData.price}
            onChange={handleChange}
          />
          <small className="field-help">
            Use this only when a live quote is unavailable. A future market refresh may replace it.
          </small>
        </div>

        {/* Bond-specific subfields */}
        {isBond && (
          <BondFields formData={formData} handleChange={handleChange} />
        )}

        <button
          type="submit"
          className="primary-btn wide-btn"
          disabled={isSaving}
        >
          {isSaving ? "Saving..." : "Add To Portfolio"}
        </button>
      </form>
    </div>
  );
}
