/**
 * Base Asset Structure:
 * {
 *   type: 'STOCK' | 'METAL',
 *   name: string,
 *   quantity: number,
 *   amountPaid: number,
 *   price?: number,
 *   lastUpdated: string (ISO 8601)
 * }
 *
 * Type-Specific Properties:
 * STOCK: ticker, sector, assetSubType, dividendYield, payoutRatio, divRate, cagr5Yr
 * BOND: bondRating, couponRate
 * METAL: unit
 */

/**
 * Parses numeric inputs safely. Returns undefined for invalid or empty values.
 * @param {any} value 
 * @returns {number | undefined}
 */
export function parseNumber(value) {
  if (value === "" || value === undefined || value === null) {
    return undefined;
  }

  const number = Number(value);
  return Number.isFinite(number) ? number : undefined;
}

/**
 * Returns a non-negative finite number suitable for financial calculations.
 * Rates are stored as decimal fractions (for example, 0.035 means 3.5%).
 */
export function toNonNegativeNumber(value, fallback = 0) {
  const number = parseNumber(value);
  return number !== undefined && number >= 0 ? number : fallback;
}

export function getAssetMarketValue(asset = {}) {
  // `value` is deliberately not used: it is not part of the canonical asset schema.
  return toNonNegativeNumber(asset.quantity) * toNonNegativeNumber(asset.price);
}

export function getAssetCostBasis(asset = {}) {
  return toNonNegativeNumber(asset.amountPaid);
}

export function getAssetDisplayType(asset = {}) {
  if (asset.type?.toUpperCase() === "METAL") return "Metal";

  const subType = asset.assetSubType?.trim().toUpperCase();
  if (["ETF", "BOND", "STOCK"].includes(subType)) {
    return subType === "BOND" ? "Bond" : subType;
  }
  return "Stock";
}

export function normalizePurityKarat(value, metalSymbol = "XAU") {
  const purity = parseNumber(value);
  const isSilver = String(metalSymbol).toUpperCase() === "XAG";
  const supportedPurities = isSilver
    ? [9999, 999, 958, 950, 935, 925, 900, 835, 830, 800]
    : [10, 14, 18, 22, 24];
  return supportedPurities.includes(purity) ? purity : (isSilver ? 999 : 24);
}

/**
 * Normalizes incoming form data into the canonical WealthPulse Asset schema.
 * 
 * @param {string} type - 'STOCK' | 'METAL'
 * @param {Object} formData - Raw form inputs
 * @returns {Object} Normalized asset payload ready for backend or state store
 */
export function buildAsset(type, formData = {}) {
  const normalizedType = String(type ?? "").trim().toUpperCase();
  const isStock = normalizedType === "STOCK";
  const isBond = isStock && formData.assetSubType === "BOND";

  const ticker = formData.ticker?.trim().toUpperCase();
  const name = formData.name?.trim();

  // 1. Base Normalized Structure (Guaranteed on every asset)
  const baseAsset = {
    type: normalizedType,
    name: name || (isStock ? ticker : "Unnamed Asset"),
    quantity: toNonNegativeNumber(formData.quantity),
    amountPaid: toNonNegativeNumber(formData.amountPaid),
    price: parseNumber(formData.price) === undefined
      ? undefined
      : toNonNegativeNumber(formData.price),
    lastUpdated: new Date().toISOString(),
  };

  // 2. STOCK & BOND Assets
  if (isStock) {
    const stockProps = {
      ...baseAsset,
      ticker,
      sector: formData.sector?.trim() || "General",
      assetSubType: formData.assetSubType || "STOCK",
      dividendYield: parseNumber(formData.dividendYield),
      payoutRatio: parseNumber(formData.payoutRatio),
      divRate: parseNumber(formData.divRate),
      cagr5Yr: parseNumber(formData.cagr5Yr),
    };

    if (isBond) {
      stockProps.bondRating = formData.bondRating?.trim().toUpperCase() || undefined;
      stockProps.couponRate = parseNumber(formData.couponRate);
    }

    return stockProps;
  }

  // 3. METAL Assets (Strict normalization — no legacy weight/ounces props)
  return {
    ...baseAsset,
    // Persist the market symbol so a generic product name (for example,
    // "Bullion") can still receive the correct metal spot quote.
    ticker: (formData.metalSymbol || ticker || "XAU").trim().toUpperCase(),
    purityKarat: normalizePurityKarat(formData.purityKarat, formData.metalSymbol),
    unit: formData.unit || "oz",
  };
}
