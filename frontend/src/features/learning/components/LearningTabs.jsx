import React from "react";
import { LEARNING_TABS } from "../data/learningContent";

export default function LearningTabs({ activeTab, onTabChange }) {
  return (
    <div className="learning-tabs-container" role="tablist" aria-label="Learning paths">
      {LEARNING_TABS.map((tab) => (
        <button
          key={tab.id}
          className={`learning-tab-button ${
            activeTab === tab.id ? "learning-active-tab" : ""
          }`}
          onClick={() => onTabChange(tab.id)}
          type="button"
          role="tab"
          aria-selected={activeTab === tab.id}
        >
          <span className="learning-tab-icon" aria-hidden="true">{tab.icon}</span>
          <span className="learning-tab-copy">
            <strong>{tab.label}</strong>
            <small>{tab.shortLabel}</small>
          </span>
          <span className="learning-tab-arrow" aria-hidden="true">→</span>
        </button>
      ))}
    </div>
  );
}
