import React from "react";

export default function NewsModal({ activeStory, onClose }) {
  if (!activeStory) return null;

  return (
    <div className="news-modal-overlay" onClick={onClose}>
      <div className="news-modal-content" onClick={(event) => event.stopPropagation()}>
        <div className="news-modal-header">
          <span className="news-modal-meta">
            {activeStory.source} • {activeStory.pubDate || "Recent"}
          </span>
          <button
            type="button"
            onClick={onClose}
            className="news-modal-close-x"
          >
            &times;
          </button>
        </div>

        <h3 className="news-modal-title">{activeStory.title}</h3>

        <p className="news-modal-summary">
          {activeStory.summary ||
            "No extended summary data available for this news wire entry."}
        </p>

        <div className="news-modal-footer">
          {activeStory.storyLink && activeStory.storyLink !== "#" && (
            <a
              href={activeStory.storyLink}
              target="_blank"
              rel="noopener noreferrer"
              className="news-modal-wire-link"
            >
              Read Full Wire
            </a>
          )}
          <button
            type="button"
            onClick={onClose}
            className="news-modal-close-btn"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
