export function parseCsvAssets(rawText) {
  if (!rawText || !rawText.trim()) return { payloads: [], skippedCount: 0 };

  const lines = rawText.split("\n");
  const parsedPayloads = [];
  let skippedCount = 0;

  lines.forEach((line, index) => {
    const cleanLine = line.trim();

    if (!cleanLine || cleanLine.toLowerCase().startsWith("type,")) return;

    const columns = cleanLine.split(",");

    const hasContent = columns.some((col) => col.trim().length > 0);
    if (!hasContent) return;

    const rawType = columns[0]?.trim().toUpperCase();
    const rawTicker = columns[1]?.trim();
    const rawName = columns[2]?.trim();
    const rawSector = columns[3]?.trim();
    const rawQty = columns[4]?.trim();
    const rawPrice = columns[5]?.trim();
    const rawAmountPaid = columns[6]?.trim();

    if (rawType !== "STOCK" && rawType !== "METAL") {
      console.warn(`Line ${index + 1} skipped: Unknown asset type "${rawType}".`);
      skippedCount++;
      return;
    }

    if (!rawQty || isNaN(parseFloat(rawQty))) {
      console.warn(`Line ${index + 1} skipped: Quantity "${rawQty}" is invalid.`);
      skippedCount++;
      return;
    }

    const parsedQuantity = parseFloat(rawQty);
    const parsedPrice = rawPrice ? parseFloat(rawPrice) : undefined;
    const parsedAmountPaid = rawAmountPaid ? parseFloat(rawAmountPaid) : 0;

    if (
      !Number.isFinite(parsedQuantity) ||
      (parsedPrice !== undefined && !Number.isFinite(parsedPrice)) ||
      !Number.isFinite(parsedAmountPaid)
    ) {
      console.warn(
        `Line ${index + 1} skipped: Price or amount paid is not numeric.`,
      );
      skippedCount++;
      return;
    }

    const assetPayload = buildAsset(rawType, {
      ticker: rawTicker,
      name: rawName,
      sector: rawSector,
      quantity: parsedQuantity,
      price: parsedPrice,
      amountPaid: parsedAmountPaid,
    });

    parsedPayloads.push(assetPayload);
  });

  return {
    payloads: parsedPayloads,
    skippedCount,
  };
}
import { buildAsset } from "../builders/assetBuilder";
