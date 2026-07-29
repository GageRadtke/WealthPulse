import React, { useState } from "react";
import { useNewsFeed } from "../hooks/useNewsFeed";
import NewsModal from "./NewsModal";

export default function LiveNewsFeed() {
  // Pulling all the heavy lifting from our custom hook!
  const {
    displayedNews,
    isLoading,
    errorMessage,
    showAllNews,
    setShowAllNews,
    lastRefreshed,
    refreshNews,
  } = useNewsFeed();

  // Modal active tracking state stays here since it's purely UI
  const [activeStory, setActiveStory] = useState(null);

  return (
    <div className="news-ticker-card">
      <div className="news-header-row">
        <div className="news-title-area">
          <span className="news-icon">📰</span>
          <h4 className="news-heading">Live Market News Feed</h4>

          <button
            type="button"
            className="refresh-icon-btn"
            title="Refresh News Feed"
            onClick={refreshNews}
            disabled={isLoading}
          >
            🔄
          </button>
        </div>

        {!isLoading && displayedNews.length > 0 && (
          <div className="news-actions-wrapper">
            {lastRefreshed && (
              <span className="news-timestamp-text">
                Synced: {lastRefreshed}
              </span>
            )}
            <button
              type="button"
              className="secondary-btn news-toggle-btn"
              onClick={() => setShowAllNews(!showAllNews)}
            >
              {showAllNews ? "Back to Featured" : "View More Headlines"}
            </button>
          </div>
        )}
      </div>

      <div className="news-scroll-box">
        {isLoading ? (
          <p className="news-status-text">
            Streaming live market data feeds...
          </p>
        ) : errorMessage && displayedNews.length === 0 ? (
          <p className="news-error-text">{errorMessage}</p>
        ) : displayedNews.length === 0 ? (
          <p className="news-status-text">
            No current news bulletins match this category view.
          </p>
        ) : (
          <div className="news-items-container">
            {displayedNews.map((item, index) => {
              const itemKey = item.id || item.storyLink || item.link || index;
              return (
                <div key={itemKey} className="news-item">
                  <button
                    type="button"
                    onClick={() => setActiveStory(item)}
                    className="news-headline-link-btn"
                  >
                    {item.title || "Untitled Story"}
                  </button>
                  <span className="news-source-span">
                    {item.source || "Unknown Source"}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <NewsModal
        activeStory={activeStory}
        onClose={() => setActiveStory(null)}
      />
    </div>
  );
}
