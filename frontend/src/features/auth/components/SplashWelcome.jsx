import React from "react";

export default function SplashWelcome() {
  return (
    <header className="splash-intro-section">
      <h1 className="splash-title">Welcome to Wealth Pulse </h1>
      <p className="splash-lead">
        Your sandbox environment for tracking assets, modeling net worth
        changes, and demystifying investment basics. We provide the analytics,
        utilities, and references needed to take command of financial structures
        safely.
      </p>

      <div className="splash-editorial-grid">
        <div className="editorial-card">
          <h4>Who Is This App For?</h4>
          <p>
            Designed explicitly for self-directed individuals, macro hobbyists,
            and everyday savers looking to build a structured logbook of equity
            classes, alternative real assets, and bullion holdings.
          </p>
        </div>
        <div className="editorial-card">
          <h4>Why Use It?</h4>
          <p>
            By keeping your portfolio entries organized and checking metrics
            against real calculations—like our public scrap bullion
            convertor—you gain clear clarity over asset weight variables without
            spreadsheet friction.
          </p>
        </div>
      </div>

      <div className="compliance-disclaimer-box">
        <h5>⚠️ Educational Tool Disclaimer & Legal Notices</h5>
        <p>
          This application functions strictly as an interactive mathematical
          educational resource. Material generated inside tracking feeds or
          sample modules **does not constitute legal, tax, or financial
          advice**.
        </p>
        <p>
          Any algorithmic projection tools or future prediction models that
          forecast prospective net worth evaluate parameters **based solely on
          legacy historical asset trend milestones**. Past metrics provide zero
          performance assurances. This software cannot promise actual investment
          gains or insulate profiles against financial losses.
        </p>
        <p className="disclaimer-strong">
          Please partner and consult with a licensed, credentialed fiduciary
          advisor to thoroughly understand your personal risk layout before
          modifying active strategies.
        </p>
      </div>
    </header>
  );
}
