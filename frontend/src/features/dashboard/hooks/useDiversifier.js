import { useState } from "react";
import { OptionContract } from "../../../shared/finance/optionsCalculations";
import { calculatePreciousMetalCostBasis } from "../../../shared/finance/metalCalculations";

export function useDiversifier() {
  // Option Visualizer State
  const [stockPrice, setStockPrice] = useState(100);
  const [strikePrice, setStrikePrice] = useState(100);

  // Metal Cost Basis Simulator State
  const [totalSpent, setTotalSpent] = useState(1500);
  const [totalOunces, setTotalOunces] = useState(50);

  const option = new OptionContract(stockPrice, strikePrice);
  return {
    stockPrice,
    setStockPrice,
    strikePrice,
    setStrikePrice,
    callValue: option.callValue,
    putValue: option.putValue,

    totalSpent,
    setTotalSpent,
    totalOunces,
    setTotalOunces,
    calculatedBasis: calculatePreciousMetalCostBasis(totalSpent, totalOunces),
  };
}
