import React, { useState } from "react";
import EquitiesLesson from "./EquitiesLesson";
import FuturesLesson from "./FuturesLesson";
import LearningTabs from "./LearningTabs";
import StackingLesson from "./StackingLesson";

const LESSON_COMPONENTS = {
  equities: EquitiesLesson,
  stacking: StackingLesson,
  futures: FuturesLesson,
};

export default function LearningCenter() {
  const [activeTab, setActiveTab] = useState("equities");

  const ActiveLesson = LESSON_COMPONENTS[activeTab] || EquitiesLesson;

  return (
    <div className="learning-center">
      <section className="learning-hero">
        <div className="learning-hero-copy">
          <span className="learning-eyebrow">WEALTHPULSE ACADEMY</span>
          <h1>Money concepts,<br /><span>made more human.</span></h1>
          <p>Pick a path, learn in small bites, and put each idea into practice.</p>
          <div className="learning-hero-meta" aria-label="Course highlights">
            <span><strong>3</strong> learning paths</span>
            <span><strong>10+</strong> quick lessons</span>
            <span><strong>Free</strong> forever</span>
          </div>
        </div>
        <div className="learning-hero-art" aria-hidden="true">
          <span className="learning-orbit learning-orbit-one">$</span>
          <span className="learning-orbit learning-orbit-two">%</span>
          <div className="learning-hero-chart">
            <span></span><span></span><span></span><span></span>
          </div>
          <div className="learning-hero-badge">Grow your<br /><strong>money IQ</strong></div>
        </div>
      </section>

      <div className="learning-path-heading">
        <div>
          <span className="learning-kicker">CHOOSE YOUR PATH</span>
          <h2>What do you want to learn?</h2>
        </div>
        <p>No prior knowledge needed. Jump in anywhere.</p>
      </div>
      <LearningTabs activeTab={activeTab} onTabChange={setActiveTab} />

      <div className="learning-content" role="tabpanel">
        <ActiveLesson />
      </div>
    </div>
  );
}
