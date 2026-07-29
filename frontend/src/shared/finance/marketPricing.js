const METAL_SYMBOL_ALIASES = {
  AU: "XAU",
  XAU: "XAU",
  AG: "XAG",
  XAG: "XAG",
  PT: "XPT",
  XPT: "XPT",
  PD: "XPD",
  XPD: "XPD",
};

function normalizeMetalSymbol(symbol) {
  return METAL_SYMBOL_ALIASES[String(symbol ?? "").trim().toUpperCase()];
}

export function getMetalMarketSymbol(asset = {}) {
  const ticker = asset.ticker?.trim().toUpperCase();
  const normalizedTicker = normalizeMetalSymbol(ticker);
  if (normalizedTicker) return normalizedTicker;

  const description = `${asset.name ?? ""} ${asset.ticker ?? ""}`.toUpperCase();
  if (description.includes("GOLD") || description.includes("XAU") || description.trim() === "BULLION") return "XAU";
  if (description.includes("SILVER") || description.includes("XAG")) return "XAG";
  if (description.includes("PLATINUM") || description.includes("XPT")) return "XPT";
  if (description.includes("PALLADIUM") || description.includes("XPD")) return "XPD";
  return "XAU";
}

export function getMarketCacheKey(asset = {}) {
  return asset.type?.toUpperCase() === "METAL"
    ? getMetalMarketSymbol(asset)
    : asset.ticker?.trim().toUpperCase();
}

export function createCachedPriceMap(entries = []) {
  return entries.reduce((prices, entry) => {
    const price = Number(entry.spotPrice);
    if (entry.ticker && Number.isFinite(price) && price > 0) {
      const ticker = entry.ticker.toUpperCase();
      prices[normalizeMetalSymbol(ticker) || ticker] = price;
    }
    return prices;
  }, {});
}

export function getAssetPrice(asset = {}, cachedPrices = {}) {
  // Metal quotes are shared by market symbol. Prefer that canonical quote so
  // every product in XAU, XAG, XPT, or XPD always renders together.
  if (asset.type?.toUpperCase() === "METAL") {
    const cachedPrice = Number(cachedPrices[getMarketCacheKey(asset)]);
    if (Number.isFinite(cachedPrice) && cachedPrice > 0) return cachedPrice;
  }

  const storedPrice = Number(asset.price);
  if (Number.isFinite(storedPrice) && storedPrice > 0) return storedPrice;

  const cachedPrice = Number(cachedPrices[getMarketCacheKey(asset)]);
  return Number.isFinite(cachedPrice) && cachedPrice > 0 ? cachedPrice : null;
}

export function applyCachedPrices(assets = [], cachedPrices = {}) {
  const sharedPrices = { ...cachedPrices };
  const cachedMarketKeys = new Set(
    Object.entries(cachedPrices)
      .filter(([, price]) => Number.isFinite(Number(price)) && Number(price) > 0)
      .map(([key]) => key),
  );

  // If the cache endpoint is unavailable, use the newest valid holding price
  // as the shared quote for that metal instead of leaving sibling rows blank.
  const newestPriceDates = {};
  assets.forEach((asset) => {
    if (asset.type?.toUpperCase() !== "METAL") return;
    const price = Number(asset.price);
    if (!Number.isFinite(price) || price <= 0) return;

    const marketKey = getMarketCacheKey(asset);
    if (cachedMarketKeys.has(marketKey)) return;

    const updatedAt = Date.parse(asset.lastUpdated);
    const comparableDate = Number.isFinite(updatedAt) ? updatedAt : 0;
    if (newestPriceDates[marketKey] === undefined || comparableDate >= newestPriceDates[marketKey]) {
      sharedPrices[marketKey] = price;
      newestPriceDates[marketKey] = comparableDate;
    }
  });

  return assets.map((asset) => {
    const price = getAssetPrice(asset, sharedPrices);
    return price === null || price === Number(asset.price) ? asset : { ...asset, price };
  });
}
