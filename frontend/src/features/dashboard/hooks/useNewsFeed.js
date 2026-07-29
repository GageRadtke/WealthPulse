import { useState, useEffect, useCallback } from "react";
import { getMarketNews } from "../api/newsApi";


const FALLBACK_NEWS = [
  {
    title:
      "Market Update: Precious metals show resilience amidst shifting Treasury yields.",
    source: "CNBC Markets",
    storyLink: "https://www.cnbc.com/markets/",
    pubDate: "Recent",
    summary:
      "Precious metals showed strong resilience today despite fluctuations in major Treasury yield brackets.",
    isFeatured: true,
  },
  {
    title:
      "Tech Stocks Rally: Major indices bounce back as semiconductor demand reaches new heights.",
    source: "MarketWatch",
    storyLink: "https://www.marketwatch.com/",
    isFeatured: true,
  },
  {
    title: "Global Commodity Report: Gold holding steady above support levels.",
    source: "APMEX News",
    storyLink: "https://www.apmex.com/gold-silver-market-news",
    isFeatured: false,
  },
];

export function useNewsFeed() {
  const [news, setNews] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [showAllNews, setShowAllNews] = useState(false);
  const [lastRefreshed, setLastRefreshed] = useState(null);

  const getFormattedTime = () =>
    new Date().toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });

  const fetchMarketNews = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await getMarketNews();
      const data = response.data;

      setNews(Array.isArray(data) ? data : []);
      setLastRefreshed(getFormattedTime());
    } catch (error) {
      console.error("Error loading news feed pipelines:", error);

      setErrorMessage(
        "Failed to stream real-time updates. Using static local briefing instead.",
      );

      setNews(FALLBACK_NEWS);
      setLastRefreshed(getFormattedTime());
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMarketNews();
  }, [fetchMarketNews]);

  const featuredNews = news.filter(
    (item) => item.isFeatured || item.type?.toLowerCase() === "featured",
  );

  let displayedNews = news;
  if (!showAllNews) {
    if (featuredNews.length > 0) {
      displayedNews = featuredNews;
    } else {
      displayedNews = news.slice(0, 5);
    }
  }

  return {
    displayedNews,
    news,
    isLoading,
    errorMessage,
    showAllNews,
    setShowAllNews,
    lastRefreshed,
    refreshNews: fetchMarketNews,
  };
}
