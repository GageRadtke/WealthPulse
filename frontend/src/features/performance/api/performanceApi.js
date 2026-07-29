import apiClient from "../../../api/client";

export const getPortfolioPerformance = (period = "1Y", benchmark = "SPY") =>
  apiClient.get("/portfolio/performance", { params: { period, benchmark } });
