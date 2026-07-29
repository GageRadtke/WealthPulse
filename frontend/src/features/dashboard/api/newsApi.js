import apiClient from "../../../api/client";

export const getMarketNews = (cancelToken) =>
  apiClient.get("/news/markets", { cancelToken });
