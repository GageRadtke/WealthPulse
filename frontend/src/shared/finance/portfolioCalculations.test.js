import { describe, expect, it } from "vitest";
import {
  calculateProgressPercent,
  calculateRemainingGoalAmount,
  calculateSplitAllocations,
  calculateTargetedAllocation,
  calculateTotalWealth,
} from "./portfolioCalculations";

describe("portfolio calculations", () => {
  it("totals stock market values", () => {
    const assets = [
      { type: "STOCK", quantity: 2, price: 150 },
      { type: "STOCK", quantity: 5, price: 20 },
    ];

    expect(calculateTotalWealth(assets)).toBe(400);
  });

  it("handles empty and invalid holdings without returning NaN", () => {
    expect(calculateTotalWealth()).toBe(0);
    expect(calculateTotalWealth([{ type: "STOCK", quantity: "bad", price: 20 }])).toBe(0);
  });

  it("caps goal progress and remaining value at their valid bounds", () => {
    expect(calculateProgressPercent(1_250, 1_000)).toBe(100);
    expect(calculateRemainingGoalAmount(1_250, 1_000)).toBe(0);
    expect(calculateProgressPercent(250, 1_000)).toBe(25);
    expect(calculateRemainingGoalAmount(250, 1_000)).toBe(750);
  });

  it("calculates targeted and gold-silver split allocations", () => {
    const targeted = calculateTargetedAllocation(100_000, 15);

    expect(targeted).toBe(15_000);
    expect(calculateSplitAllocations(targeted, 60)).toEqual({
      goldTarget: 9_000,
      silverTarget: 6_000,
    });
  });
});
