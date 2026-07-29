import { describe, expect, it } from "vitest";
import {
  calculateCallValue,
  calculatePutValue,
  OptionContract,
} from "./optionsCalculations";

describe("options calculations", () => {
  it("calculates call intrinsic value", () => {
    expect(calculateCallValue(125, 100)).toBe(25);
    expect(calculateCallValue(90, 100)).toBe(0);
  });

  it("calculates put intrinsic value", () => {
    expect(calculatePutValue(90, 100)).toBe(10);
    expect(calculatePutValue(125, 100)).toBe(0);
  });

  it("exposes both values through an option contract", () => {
    const contract = new OptionContract(95, 100);

    expect(contract.callValue).toBe(0);
    expect(contract.putValue).toBe(5);
  });

  it("handles invalid inputs without returning NaN", () => {
    expect(calculateCallValue("invalid", 100)).toBe(0);
    expect(calculatePutValue(100, undefined)).toBe(0);
  });
});
