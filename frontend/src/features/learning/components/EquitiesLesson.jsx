import React, { useState } from "react";
import {
  EQUITIES_CARDS,
  EQUITIES_VIDEO_LESSONS,
  GLOSSARY_TERMS,
  TRADING_INSTRUCTIONS,
  STRATEGY_ROWS,
  RED_FLAGS,
  OPTIONS_TRADING_CARD,
} from "../data/learningContent";
import DiversifierTool from "../../dashboard/components/DiversifierTool";
import LessonCardGrid from "./lessonBlocks/LessonCardGrid";
import StrategyTable from "./lessonBlocks/StrategyTable";
import VideoLessonGrid from "./lessonBlocks/VideoLessonGrid";

// --- FLASHCARD STACK COMPONENT ---
function FlashcardStack({ terms }) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);

  if (!terms || terms.length === 0) return null;

  const currentCard = terms[currentIndex];

  const handleFlip = () => {
    setIsFlipped(!isFlipped);
  };

  const handleNext = (e) => {
    e.stopPropagation(); // Prevents flipping the card when clicking "Next"
    setIsFlipped(false);
    setCurrentIndex((prevIndex) => (prevIndex + 1) % terms.length);
  };

  const handlePrev = (e) => {
    e.stopPropagation(); // Prevents flipping the card when clicking "Previous"
    setIsFlipped(false);
    setCurrentIndex((prevIndex) => (prevIndex - 1 + terms.length) % terms.length);
  };

  return (
    <div className="flashcard-game-container" style={{ maxWidth: "550px", margin: "2rem auto", textAlign: "center" }}>
      {/* Flashcard Body */}
      <div
        className={`flashcard-card ${isFlipped ? "flipped" : ""}`}
        onClick={handleFlip}
        style={{
          minHeight: "220px",
          backgroundColor: "var(--color-surface-raised, #fff)",
          border: "1px solid var(--color-border, #ddd)",
          borderRadius: "var(--radius-md, 8px)",
          boxShadow: "var(--shadow-sm, 0 4px 6px rgba(0,0,0,0.05))",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          padding: "2rem",
          cursor: "pointer",
          userSelect: "none",
          transition: "all 0.2s ease-in-out",
          marginBottom: "1.5rem"
        }}
      >
        {!isFlipped ? (
          <div>
            <span style={{ fontSize: "0.8rem", color: "var(--color-text-muted, #777)", textTransform: "uppercase", letterSpacing: "1px" }}>
              Click to Flip
            </span>
            <h3 style={{ marginTop: "1rem", fontSize: "1.8rem", color: "var(--color-heading)" }}>
              {currentCard.label}
            </h3>
          </div>
        ) : (
          <div>
            <span style={{ fontSize: "0.8rem", color: "#3182ce", textTransform: "uppercase", letterSpacing: "1px" }}>
              Definition
            </span>
            <p className="learning-body-text" style={{ marginTop: "1rem", fontSize: "1.2rem", lineHeight: "1.6" }}>
              {currentCard.desc}
            </p>
          </div>
        )}
      </div>

      {/* Flashcard Controls */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "0 0.5rem" }}>
        <button
          onClick={handlePrev}
          className="news-modal-close-x"
          style={{ width: "auto", padding: "0 1.25rem", fontSize: "0.9rem", display: "flex", alignItems: "center" }}
        >
          ← Prev
        </button>

        <span style={{ fontSize: "0.9rem", fontWeight: "600", color: "var(--color-text-muted, #666)" }}>
          {currentIndex + 1} / {terms.length}
        </span>

        <button
          onClick={handleNext}
          className="news-modal-wire-link"
          style={{ border: "none", cursor: "pointer", padding: "10px 20px" }}
        >
          Next Card →
        </button>
      </div>
    </div>
  );
}

// --- MAIN EQUITIES LESSON COMPONENT ---
export default function EquitiesLesson() {
  return (
    <div className="learning-fade-in-transition">
      <h2 className="learning-section-title">
        Traditional Equities (Stocks & ETFs)
      </h2>

      <div className="learning-glossary-section">
        <h3 className="learning-section-title learning-subsection-title" style={{ textAlign: "center" }}>
          Stocks, Bonds & ETFs Flashcard Game
        </h3>
        <FlashcardStack terms={GLOSSARY_TERMS} />
      </div>

      <div className="learning-intro-section">
        <p className="learning-body-text core-intro">
          Now that you have seen some of the most commonly used terms, no need
          to freak out yet. As you continue, I am introducing some small lessons
          to help your understanding of the stock market. I will be covering the
          basics of stocks and ETFs, how to acquire and trade them, strategy
          selection, and risk mitigation. I will also be providing some video
          lessons to help you understand the concepts better. Let's get started!
          <br />
          <br />
          Disclaimer: This is not financial advice. I am not the owner of any of
          the provided videos; all videos are property of their creators and
          shared here by way of YouTube share.
        </p>
      </div>

      <LessonCardGrid cards={EQUITIES_CARDS} />
      <VideoLessonGrid videos={EQUITIES_VIDEO_LESSONS} />

      <div className="learning-tips-container-card trading instructions-card">
        <h3>How To: Beginner's Trading & Order Execution</h3>
        <p className="learning-body-text core-intro">
          Investors trade equities by opening accounts with online brokerages
          (like Fidelity, E*TRADE, or Robinhood), linking their bank, and
          entering ticker symbols to submit market or limit orders. Orders are
          routed for execution, but execution time and price depend on the order
          type, available liquidity, and market conditions.
        </p>

        <div className="learning-tips-container-card beginner-concepts-card">
          <h3>Beginners Concepts</h3>

          {/* DYNAMIC LOOP FOR TRADING INSTRUCTIONS STEPS */}
          <div className="trading-steps-list" style={{ margin: "1.5rem 0" }}>
            {TRADING_INSTRUCTIONS.map((instruction) => (
              <div
                key={instruction.step}
                className="trading-step-item"
                style={{ marginBottom: "1.5rem" }}
              >
                <h4>
                  Step {instruction.step}: {instruction.title}
                </h4>
                {instruction.body && (
                  <p className="learning-body-text">{instruction.body}</p>
                )}

                {/* Specific rendering logic for Step 4 mechanics if present */}
                {instruction.mechanism && (
                  <div
                    className="execution-mechanics"
                    style={{
                      paddingLeft: "1rem",
                      borderLeft: "2px solid #ccc",
                    }}
                  >
                    <p>
                      <em>{instruction.mechanism}</em>
                    </p>
                    {instruction.orders &&
                      instruction.orders.map((order, idx) => (
                        <div key={idx} style={{ marginTop: "1rem" }}>
                          <h5> {order.name}</h5>
                          <p className="learning-body-text">
                            <strong>How:</strong> {order.how}
                          </p>
                          <p className="learning-body-text">
                            <strong>Pros/Cons:</strong> {order.prosCons}
                          </p>
                          <p className="learning-body-text">
                            <strong>When:</strong> {order.when}
                          </p>
                        </div>
                      ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="learning-tips-container-card beginner-concepts-card">
          <h3>Core Concepts Every Beginner Must Know</h3>
          <p className="learning-body-text learning-spaced-text">
            <strong> Core Concepts Every Beginner Must Know:</strong> Compound
            Growth (The Snowball Effect): This is an investor's best friend.
            When your investments earn money, that profit is reinvested to earn
            its own profit. Over 10, 20, or 30 years, this creates a massive
            snowball effect that turns small, consistent contributions into
            significant wealth. <br /><br />Diversification (Don't Put All Your Eggs in One
            Basket): Instead of buying just one single stock (like only buying
            Apple), beginners are usually better off buying ETFs
            (Exchange-Traded Funds). An ETF acts like a pre-packaged basket of
            hundreds of stocks. If one company struggles, the others help
            balance it out. <br /><br /> Golden Rules for Getting Started If you are
            absolute beginner, a great approach is to build a solid investing
            foundation first. If you later become curious about trading, only
            use a very small amount of money that you are completely comfortable
            losing as "learning capital."
          </p>
        </div>
      </div>

      <div className="learning-tips-container-card">
        <h3>Strategy Selection</h3>
        <StrategyTable rows={STRATEGY_ROWS} />
        <p className="learning-body-text learning-spaced-text">
          <strong>Platform Recommendation:</strong> Avoid day trading as a
          beginner. It acts like high-risk speculation. If you do stick to high-volume top 100 S&P index entities
          to improve exit liquidity.
        </p>
      </div>

      <div
        className="learning-tips-container-card options-trading-card"
        style={{ marginTop: "2rem" }}
      >
        <h3>{OPTIONS_TRADING_CARD.title}</h3>
        <p className="learning-body-text" style={{ marginBottom: "1.5rem" }}>
          {OPTIONS_TRADING_CARD.intro}
        </p>

        <div
          className="options-types-grid"
          style={{
            display: "grid",
            gap: "1.5rem",
            gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
            marginBottom: "1.5rem",
          }}
        >
          {OPTIONS_TRADING_CARD.types.map((type, idx) => (
            <div
              key={idx}
              style={{
                padding: "1rem",
                border: "1px solid #ddd",
                borderRadius: "6px",
                backgroundColor: "rgba(0,0,0,0.02)",
              }}
            >
              <h4>{type.name}</h4>
              <p className="learning-body-text">
                <strong>Market Outlook:</strong> {type.view}
              </p>
              <p className="learning-body-text">
                <strong>Contract Mechanism:</strong> {type.right}
              </p>
              <p className="learning-body-text">
                <strong>Analogy:</strong> <em>{type.analogy}</em>
              </p>
              <p className="learning-body-text" style={{ marginTop: "0.5rem" }}>
                <strong>How it profits:</strong> {type.winCondition}
              </p>
            </div>
          ))}
        </div>

        {/* Strike Price Explanation section */}
        <div
          style={{
            padding: "1rem",
            borderLeft: "3px solid #3182ce",
            backgroundColor: "rgba(49, 130, 206, 0.05)",
            marginBottom: "1.5rem",
          }}
        >
          <h4>{OPTIONS_TRADING_CARD.strikeExplanation.title}</h4>
          <p className="learning-body-text">
            {OPTIONS_TRADING_CARD.strikeExplanation.body}
          </p>
        </div>
      </div>

      <DiversifierTool contextType="equities" />

      <div className="learning-tips-container-card learning-risk-card">
        <h3>Risk Mitigation & Red Flags</h3>
        <ul className="learning-tips-list">
          {RED_FLAGS.map((redFlag) => (
            <li key={redFlag.label}>
              <strong>{redFlag.label}:</strong> {redFlag.desc}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
