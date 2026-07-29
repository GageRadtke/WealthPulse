export const formatDateTime = (ts) => {
    if (!ts) return "—";
    try {
      const d = new Date(ts);
      return d.toLocaleString();
    } catch {
      return String(ts);
    }
  };

export const SIZE_LABELS = {
  1: "1oz",
  0.5: "1/2oz",
  0.25: "1/4oz",
  0.1: "1/10oz",
};

export function formatMetalSize(size) {
  return SIZE_LABELS[size] ?? `${size}oz`;
}

export const formatCurrency = (value) => {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
  }).format(Number(value) || 0);
};
