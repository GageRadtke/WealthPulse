export const APP_MODE = {
  LIVE: "live",
};

export const VIEWS = {
  DASHBOARD: "dashboard",
  STOCKS: "stocks",
  METALS: "metals",
  LEARNING: "learning",
};

export const API_BASE =
  import.meta.env.VITE_API_URL ||
  "http://localhost:8283";
