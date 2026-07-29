import { getAssetMarketValue, toNonNegativeNumber } from "../../features/assets/builders/assetBuilder";
import { calculateMetalMeltValue } from "./metalCalculations";
import { getMetalMarketSymbol } from "./marketPricing";

export const calculateAssetCurrentValue = (asset = {}) => {
  return asset.type?.toUpperCase() === "METAL"
    ? calculateMetalMeltValue(
      asset.quantity,
      asset.price,
      asset.purityKarat,
      asset.unit,
      getMetalMarketSymbol(asset),
    )
    : getAssetMarketValue(asset);
};

export const calculateTotalWealth = (allAssets = []) => {
  return allAssets.reduce((sum, asset) => sum + calculateAssetCurrentValue(asset), 0);
};

export const calculateProgressPercent = (currentWealth, targetWealth) => {
  const current = toNonNegativeNumber(currentWealth);
  const target = toNonNegativeNumber(targetWealth);
  return target > 0 ? Math.min((current / target) * 100, 100) : 0;
};

export const calculateRemainingGoalAmount = (currentWealth, targetWealth) => {
  const target = toNonNegativeNumber(targetWealth);
  return Math.max(target - toNonNegativeNumber(currentWealth), 0);
};

export const calculateTargetedAllocation = (totalWealth, targetPercent) => {
  const percentage = Math.min(toNonNegativeNumber(targetPercent), 100);
  return toNonNegativeNumber(totalWealth) * (percentage / 100);
};

export const calculateSplitAllocations = (targetedValue, goldPercent) => {
  const goldPercentage = Math.min(toNonNegativeNumber(goldPercent), 100);
  const value = toNonNegativeNumber(targetedValue);
  const goldTarget = value * (goldPercentage / 100);
  const silverTarget = value * ((100 - goldPercentage) / 100);
  return { goldTarget, silverTarget };
};
