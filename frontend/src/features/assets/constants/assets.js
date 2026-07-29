export const ASSET_SUB_TYPES = [
  { value: "STOCK", label: "Stock" },
  { value: "ETF", label: "ETF" },
  { value: "BOND", label: "Bond" },
];

export const METAL_UNITS = [
  { value: "oz", label: "Troy Ounce (oz)" },
  { value: "g", label: "Grams (g)" },
];

export const METAL_MARKETS = [
  { value: "XAU", label: "Gold" },
  { value: "XAG", label: "Silver" },
  { value: "XPT", label: "Platinum" },
  { value: "XPD", label: "Palladium" },
];

export const GOLD_PURITY_KARATS = [10, 14, 18, 22, 24].map((karat) => ({
  value: karat,
  label: `${karat}K`,
}));

export const SILVER_PURITY_FINENESS = [
  { value: 9999, label: "9999 Ultra-Fine (99.99%)" },
  { value: 999, label: "999 Fine Silver (99.9%)" },
  { value: 958, label: "958 Britannia (95.8%)" },
  { value: 950, label: "950 French 1st Standard (95.0%)" },
  { value: 935, label: "935 Argentium (93.5%)" },
  { value: 925, label: "925 Sterling (92.5%)" },
  { value: 900, label: "900 Coin Silver (90.0%)" },
  { value: 835, label: "835 Scandinavian (83.5%)" },
  { value: 830, label: "830 Scandinavian (83.0%)" },
  { value: 800, label: "800 Continental (80.0%)" },
];

export function getMetalPurityOptions(metalSymbol) {
  return String(metalSymbol).toUpperCase() === "XAG"
    ? SILVER_PURITY_FINENESS
    : GOLD_PURITY_KARATS;
}

// Kept as the default export for existing gold/platinum/palladium callers.
