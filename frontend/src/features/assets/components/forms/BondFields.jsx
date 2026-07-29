import React from "react";

export default function BondFields({ formData, handleChange }) {
  return (
    <>
      <div className="form-group">
        <label>Bond Credit Rating</label>
        <input
          type="text"
          name="bondRating"
          placeholder="e.g. AAA, AA-, BBB+"
          value={formData.bondRating}
          onChange={handleChange}
        />
      </div>

      <div className="form-group">
        <label>Coupon Rate (%)</label>
        <input
          type="number"
          step="any"
          name="couponRate"
          placeholder="4.5"
          value={formData.couponRate}
          onChange={handleChange}
        />
      </div>
    </>
  );
}
