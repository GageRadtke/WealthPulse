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
      <LearningTabs activeTab={activeTab} onTabChange={setActiveTab} />

      <div className="learning-content">
        <ActiveLesson />
      </div>
    </div>
  );
}

