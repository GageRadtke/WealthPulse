import React, { useEffect, useState } from "react";
import apiClient from "../../../api/client";

export default function MarketTracker() {
  const [quotes, setQuotes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchQuotes = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await apiClient.get("/market-tracker/quotes");
        setQuotes(response.data || []);
      } catch (err) {
        console.error("Error loading market tracker:", err);
        setError("Unable to load current index performance.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchQuotes();
    const refreshTimer = window.setInterval(fetchQuotes, 60 * 60 * 1000);
    return () => window.clearInterval(refreshTimer);
  }, []);

  return (
    <div className="market-tracker-card">
      <div className="market-tracker-header">
        <div>
          <span className="market-tracker-icon">📈</span>
          <h4 className="market-tracker-heading">Current Market Tracker</h4>
        </div>
        <span className="market-tracker-refresh">Updates hourly</span>
      </div>

      {isLoading ? (
        <p className="market-tracker-status">Loading index performance...</p>
      ) : error ? (
        <p className="market-tracker-error">{error}</p>
      ) : (
        <div className="market-tracker-grid">
          {quotes.map((quote) => (
            <div key={quote.ticker} className="market-tracker-item">
              <div className="market-tracker-item-header">
                <span className="market-tracker-symbol">{quote.ticker}</span>
                <span className="market-tracker-status-pill">
                  {quote.status === "ok" ? "Live" : "Unavailable"}
                </span>
              </div>
              <div className="market-tracker-price">
                {quote.price != null ? `$${quote.price.toFixed(2)}` : "—"}
              </div>
              <div className="market-tracker-name">{quote.name}</div>
              <div className="market-tracker-description">
                {quote.description}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
