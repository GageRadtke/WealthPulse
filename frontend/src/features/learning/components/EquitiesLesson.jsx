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
    <div className="flashcard-game-container">
      <div
        className={`flashcard-card ${isFlipped ? "flipped" : ""}`}
        onClick={handleFlip}
      >
        {!isFlipped ? (
          <div>
            <span className="flashcard-label">
              Click to Flip
            </span>
            <h3 className="flashcard-title">
              {currentCard.label}
            </h3>
          </div>
        ) : (
          <div>
            <span className="flashcard-label flashcard-label--definition">
              Definition
            </span>
            <p className="learning-body-text flashcard-definition">
              {currentCard.desc}
            </p>
          </div>
        )}
      </div>

      <div className="flashcard-controls">
        <button
          onClick={handlePrev}
          className="secondary-btn flashcard-button"
          type="button"
        >
          ← Prev
        </button>

        <span className="flashcard-counter">
          {currentIndex + 1} / {terms.length}
        </span>

        <button
          onClick={handleNext}
          className="primary-btn flashcard-button"
          type="button"
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
        <h3 className="learning-section-title learning-subsection-title learning-centered-title">
          Stocks, Bonds & ETFs Flashcard Game
        </h3>
        <FlashcardStack terms={GLOSSARY_TERMS} />
      </div>

      <div className="learning-intro-section">
        <span className="learning-intro-icon" aria-hidden="true">✦</span>
        <div>
          <h3>Learn the market without the jargon</h3>
          <p className="learning-body-text core-intro">
            Start with stocks and ETFs, then build confidence with trading,
            strategy, and risk basics. Use the cards, videos, and mini tools in
            any order that works for you.
          </p>
          <small>Educational content only — not financial advice.</small>
        </div>
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

          <div className="trading-steps-list">
            {TRADING_INSTRUCTIONS.map((instruction) => (
              <div
                key={instruction.step}
                className="trading-step-item"
              >
                <h4>
                  Step {instruction.step}: {instruction.title}
                </h4>
                {instruction.body && (
                  <p className="learning-body-text">{instruction.body}</p>
                )}

                {instruction.mechanism && (
                  <div className="execution-mechanics">
                    <p>
                      <em>{instruction.mechanism}</em>
                    </p>
                    {instruction.orders &&
                      instruction.orders.map((order) => (
                        <div key={order.name} className="execution-order">
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

      <div className="learning-tips-container-card options-trading-card">
        <h3>{OPTIONS_TRADING_CARD.title}</h3>
        <p className="learning-body-text options-card-intro">
          {OPTIONS_TRADING_CARD.intro}
        </p>

        <div className="options-types-grid">
          {OPTIONS_TRADING_CARD.types.map((type) => (
            <div key={type.name} className="option-type-card">
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
              <p className="learning-body-text option-profit-text">
                <strong>How it profits:</strong> {type.winCondition}
              </p>
            </div>
          ))}
        </div>

        <div className="strike-explanation">
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
