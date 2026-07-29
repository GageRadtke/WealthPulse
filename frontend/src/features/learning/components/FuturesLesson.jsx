import React from "react";
import { courseInstance } from "../data/futuresData";
import Card from "../../../shared/components/Card";
import VideoLessonGrid from "./lessonBlocks/VideoLessonGrid";

const definitions = courseInstance.getDefinitions();
const mechanics = courseInstance.getMarketMechanics();
const roadmap = courseInstance.getRoadmap();
const risks = courseInstance.getRisks();
const videos = courseInstance.getVideoLessons();

export default function FuturesLesson() {
  return (
    <div className="learning-fade-in-transition">
      <h2 className="learning-section-title">
        Futures Contracts: Advanced Derivatives
      </h2>

      {/* ── Core Definitions ── */}
      <div className="learning-card-layout-grid">
        {Object.values(definitions).map((def) => (
          <Card key={def.title} title={def.title}>
            <p className="learning-body-text">{def.definition}</p>
            {def.keyTraits && (
              <ul className="lesson-section-list">
                {def.keyTraits.map((trait) => (
                  <li key={trait}>{trait}</li>
                ))}
              </ul>
            )}
            {def.types && (
              <ul className="lesson-section-list">
                {def.types.map((t) => (
                  <li key={t}>{t}</li>
                ))}
              </ul>
            )}
            {def.pdtExemption && (
              <p className="learning-body-text" style={{ marginTop: "0.5rem", fontStyle: "italic" }}>
                💡 {def.pdtExemption}
              </p>
            )}
          </Card>
        ))}
      </div>

      {/* ── Market Mechanics ── */}
      <div className="learning-tips-container-card">
        <h3>Market Structure & Hours</h3>
        <div className="learning-card-layout-grid">
          <Card title="Trading Hours">
            <p className="learning-body-text">
              <strong>{mechanics.hours.schedule}</strong>
            </p>
            <p className="learning-body-text">{mechanics.hours.details}</p>
          </Card>
          <Card title={mechanics.limits.mechanism}>
            <p className="learning-body-text">{mechanics.limits.purpose}</p>
          </Card>
        </div>
      </div>

      {/* ── Getting Started Roadmap ── */}
      <div className="learning-tips-container-card">
        <h3>📋 Getting Started Roadmap</h3>
        <div className="trading-steps-list" style={{ margin: "1.5rem 0" }}>
          {roadmap.map((item) => (
            <div key={item.step} className="trading-step-item" style={{ marginBottom: "1.5rem" }}>
              <h4>Step {item.step}: {item.title}</h4>
              <p className="learning-body-text">{item.action}</p>
            </div>
          ))}
        </div>
      </div>

      {/* ── Video Lessons ── */}
      <VideoLessonGrid videos={videos} />

      {/* ── Risk Warnings ── */}
      <div className="learning-tips-container-card learning-risk-card">
        <h3>⚠️ Key Risks to Understand</h3>
        <ul className="learning-tips-list">
          {risks.map((risk) => (
            <li key={risk.type}>
              <strong>{risk.type}:</strong> {risk.warning}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
