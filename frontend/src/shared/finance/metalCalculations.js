import {
  DENOMINATION_SIZES,
  GRAMS_PER_TROY_OUNCE,
  JUNK_SILVER_MULTIPLIER,
} from "./metalRates";
import {
  getAssetCostBasis,
  normalizePurityKarat,
  toNonNegativeNumber,
} from "../../features/assets/builders/assetBuilder";


/**
 * Calculates melt value from a GoldAPI spot quote in USD per troy ounce.
 * Gram weights are converted using 31.1034768 grams per troy ounce.
 */
export function getMetalPurityFactor(purityKarat, metalSymbol = "XAU") {
  // Spot is quoted for fine metal. Gold jewellery marked 24K is conventionally
  // treated as 99.9% fine; all lower karats use karat / 24.
  const purity = normalizePurityKarat(purityKarat, metalSymbol);
  if (String(metalSymbol).toUpperCase() === "XAG") {
    return purity / 1000;
  }
  return purity === 24 ? 0.999 : purity / 24;
}

export function convertMetalWeightToTroyOunces(weight, unit = "oz") {
  const normalizedWeight = toNonNegativeNumber(weight);
  return unit === "g" ? normalizedWeight / GRAMS_PER_TROY_OUNCE : normalizedWeight;
}

export function calculateMetalMeltValue(
  weight,
  spotPricePerTroyOunce,
  purityKarat = 24,
  unit = "oz",
  metalSymbol = "XAU",
) {
  return convertMetalWeightToTroyOunces(weight, unit)
    * toNonNegativeNumber(spotPricePerTroyOunce)
    * getMetalPurityFactor(purityKarat, metalSymbol);
}

// Backward-compatible alias for existing callers.
export const getCurrentMetalPrice = (metalAssets = [], metalName, fallbackPrice) => {
  const matchingAsset = metalAssets.find((asset) => {
    const name = `${asset.name ?? ""} ${asset.ticker ?? ""}`.toLowerCase();
    return asset.type?.toUpperCase() === "METAL" && name.includes(metalName);
  });

  return matchingAsset?.price ?? matchingAsset?.spotPrice ?? matchingAsset?.currentPrice ?? fallbackPrice;
};

export const getStackBreakdown = (value, ouncePrice) => {
  let remaining = value;

  return DENOMINATION_SIZES.map((size) => {
    const costPerUnit = size * ouncePrice;
    let count = Math.floor(remaining / costPerUnit);

    if (size === 0.1 && count === 0 && remaining > costPerUnit / 2) {
      count = 1;
    }

    remaining -= count * costPerUnit;
    return { size, count };
  }).filter((item) => item.count > 0);
};

export const convertGramsToTroyOunces = (grams) => {
  return Number((toNonNegativeNumber(grams) / GRAMS_PER_TROY_OUNCE).toFixed(4));
};

export const calculateJunkSilverOunces = (faceValue) => {
  return Number((toNonNegativeNumber(faceValue) * JUNK_SILVER_MULTIPLIER).toFixed(3));
};


export const calculatePreciousMetalCostBasis = (totalSpent, totalOunces) => {
  const ounces = toNonNegativeNumber(totalOunces);
  if (ounces <= 0) return 0;
  return toNonNegativeNumber(totalSpent) / ounces;
};

export const calculateMetalAllocation = (totalOunces, silverRatio) => {
  const ounces = toNonNegativeNumber(totalOunces);
  const ratio = toNonNegativeNumber(silverRatio);
  const goldOunces = ounces > 0 ? ounces / (1 + ratio) : 0;
  const silverOunces = ounces > 0 ? goldOunces * ratio : 0;
  return { goldOunces, silverOunces };
};

export const calculateTotalPaidForMetals = (metalAssets = []) => {
  return metalAssets.reduce((sum, asset) => sum + getAssetCostBasis(asset), 0);
};

export const calculateMetalCostDiff = (totalMeltValue, totalPaid) => {
  return toNonNegativeNumber(totalMeltValue) - toNonNegativeNumber(totalPaid);
};

export const calculateMetalWeightBreakdown = (metalAssets = []) => {
  const WeightByMetal = {};
  metalAssets.forEach((a) => {
    const key = a.name ?? "Unknown";
    WeightByMetal[key] = (WeightByMetal[key] ?? 0)
      + convertMetalWeightToTroyOunces(a.quantity, a.unit);
  });
  return Object.entries(WeightByMetal);
};
