import React from "react";
import { LEARNING_TABS } from "../data/learningContent";

export default function LearningTabs({ activeTab, onTabChange }) {
  return (
    <div className="learning-tabs-container">
      {LEARNING_TABS.map((tab) => (
        <button
          key={tab.id}
          className={`learning-tab-button ${
            activeTab === tab.id ? "learning-active-tab" : ""
          }`}
          onClick={() => onTabChange(tab.id)}
          type="button"
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}
