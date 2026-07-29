/**
 * Validates the asset form fields based on asset type and sub-type.
 * @param {string} type - 'STOCK' | 'METAL'
 * @param {Object} formData - Form input values
 * @returns {Object} errors object mapped by field name
 */
export function validateAssetForm(type, formData) {
  const errors = {};
  const trimmedTicker = formData.ticker?.trim();
  const trimmedName = formData.name?.trim();

  if (type === "STOCK" && !trimmedTicker) {
    errors.ticker = "Please enter a stock ticker.";
  }

  if (type === "METAL" && !trimmedName) {
    errors.name = "Please enter a metal name (e.g., Gold, Silver).";
  }

  if (!formData.quantity || isNaN(parseFloat(formData.quantity))) {
    errors.quantity = "Please enter a valid numeric quantity.";
  }

  if (formData.quantity && parseFloat(formData.quantity) <= 0) {
    errors.quantity = "Quantity must be greater than zero.";
  }

  return errors;
}