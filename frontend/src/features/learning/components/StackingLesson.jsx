import React, { useState } from "react";
import DiversifierTool from "../../dashboard/components/DiversifierTool";
import Card from "../../../shared/components/Card";
import VideoLessonGrid from "./lessonBlocks/VideoLessonGrid";
import {
  COUNTERFEIT_WATCHLIST,
  METALS_SCAMS,
  STACKING_GOALS,
  STACKING_VIDEO_LESSONS,
} from "../data/stackingContent";

export default function StackingLesson() {
  const [selectedGoal, setSelectedGoal] = useState("beginner");
  const recommendation = STACKING_GOALS[selectedGoal];

  return (
    <div className="learning-fade-in-transition">
      <h2 className="learning-section-title">
        Physical Precious Metals Stacking
      </h2>

      <p className="learning-body-text stacking-intro">
        Stacking means gradually acquiring physical gold or silver as a store of
        value, collection, or preparedness asset. Learn the product, premium,
        testing method, storage plan, and likely resale market before buying.
        Precious metals can fluctuate in price and should not replace an
        emergency fund or a diversified financial plan.
      </p>

      <VideoLessonGrid videos={STACKING_VIDEO_LESSONS} />

      <section className="stacking-counterfeit-layout">
        <div className="learning-tips-container-card stacking-watchlist-card">
          <h3>Top 50 Faked Silver and Gold Coins / Bullion</h3>
          <p className="learning-body-text">
            This unranked watchlist covers 50 products and product types that
            buyers should inspect carefully. Inclusion does not mean every item
            is suspicious; popular products attract copies because buyers
            recognize them.
          </p>
          <div className="stacking-watchlist-columns">
            <div>
              <h4>Gold watchlist</h4>
              <ol className="learning-tips-list">
                {COUNTERFEIT_WATCHLIST.gold.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ol>
            </div>
            <div>
              <h4>Silver watchlist</h4>
              <ol className="learning-tips-list" start="26">
                {COUNTERFEIT_WATCHLIST.silver.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ol>
            </div>
          </div>
        </div>

        <aside className="learning-tips-container-card learning-risk-card stacking-test-warning">
          <h3>⚠️ Always Test Your Metals</h3>
          <p className="learning-body-text">
            Never rely on appearance, packaging, or a seller&apos;s word alone.
            No single home test proves authenticity.
          </p>
          <ul className="learning-tips-list">
            <li>Buy from an established dealer and keep the receipt.</li>
            <li>Compare exact weight and dimensions with mint specifications.</li>
            <li>Use a strong magnet and slide test; precious metals are not magnetic.</li>
            <li>Check sound or resonance only as one supporting test.</li>
            <li>Use a conductivity verifier for higher-confidence screening.</li>
            <li>Have valuable or questionable pieces professionally tested with XRF, ultrasound, or another appropriate method.</li>
            <li>Do not damage collectible coins with acid, filing, or destructive testing.</li>
          </ul>
        </aside>
      </section>

      <section className="learning-tips-container-card">
        <h3>Common Precious-Metals Scams</h3>
        <div className="learning-card-layout-grid stacking-scam-grid">
          {METALS_SCAMS.map((scam) => (
            <Card key={scam.title} title={scam.title}>
              <p className="learning-body-text">{scam.body}</p>
            </Card>
          ))}
        </div>
        <p className="learning-body-text">
          Pause when a seller creates urgency. Verify the business independently,
          calculate the premium over melt value, read the return and buyback
          policies, and use a payment method with buyer protection.
        </p>
      </section>

      <section className="learning-tips-container-card stacking-mindset-card">
        <h3>Choose Your Stacking Mindset</h3>
        <p className="learning-body-text">
          Select the goal that best describes you. This educational suggestion
          is a starting point—not personalized financial advice.
        </p>
        <label className="stacking-goal-label" htmlFor="stacking-goal">
          My main goal
          <select
            id="stacking-goal"
            className="stacking-goal-select"
            value={selectedGoal}
            onChange={(event) => setSelectedGoal(event.target.value)}
          >
            {Object.entries(STACKING_GOALS).map(([value, goal]) => (
              <option key={value} value={value}>{goal.label}</option>
            ))}
          </select>
        </label>
        <div className="stacking-recommendation" aria-live="polite">
          <h4>{recommendation.focus}</h4>
          <p><strong>What to focus on:</strong> {recommendation.allocation}</p>
          <p><strong>Why it fits:</strong> {recommendation.why}</p>
          <p><strong>Watch out for:</strong> {recommendation.avoid}</p>
        </div>
      </section>

      <section className="learning-tips-container-card stacking-dca-card">
        <h3>Dollar-Cost Averaging for Precious Metals</h3>
        <p className="learning-body-text">
          Dollar-cost averaging means investing a fixed amount on a regular
          schedule—such as every payday or once a month—regardless of whether
          gold or silver prices are rising or falling. Some purchases will occur
          at higher prices and others at lower prices, which spreads your entry
          cost across time instead of depending on one perfectly timed purchase.
        </p>

        <div className="learning-card-layout-grid stacking-dca-grid">
          <Card title="Why Stackers Use It">
            <ul className="learning-tips-list">
              <li>Creates a consistent stacking habit.</li>
              <li>Reduces the pressure to predict short-term price movements.</li>
              <li>Helps control emotional buying during market excitement.</li>
              <li>Gradually builds ounces while keeping purchases within a budget.</li>
              <li>Makes it easier to measure your average cost per ounce.</li>
            </ul>
          </Card>

          <Card title="A Simple Example">
            <p className="learning-body-text">
              If you budget $100 each month, buy only what that amount reasonably
              covers after premiums, tax, and shipping. Record the total amount
              paid and the actual precious-metal weight received. Your true
              average cost is:
            </p>
            <p className="stacking-dca-formula">
              Total money spent ÷ total ounces owned
            </p>
          </Card>
        </div>

        <div className="stacking-dca-advice">
          <h4>Tips for a New Stacker</h4>
          <ul className="learning-tips-list">
            <li>
              <strong>Set a sustainable schedule:</strong> Choose an amount that
              will not interfere with bills, emergency savings, or debt payments.
            </li>
            <li>
              <strong>Track the all-in cost:</strong> Include premiums, shipping,
              tax, card fees, and other charges—not only the spot price.
            </li>
            <li>
              <strong>Compare premium per ounce:</strong> A cheaper-looking item
              can cost more per ounce because of its size or premium.
            </li>
            <li>
              <strong>Combine small orders when practical:</strong> Saving for a
              larger purchase may reduce shipping and premiums, but only buy from
              a reputable dealer and stay within your budget.
            </li>
            <li>
              <strong>Keep separate records:</strong> Track gold and silver
              independently because they have different prices, premiums, and
              resale markets.
            </li>
            <li>
              <strong>Plan your exit before buying:</strong> Learn what local
              dealers or online buyers pay and favor products they recognize.
            </li>
            <li>
              <strong>Do not chase dips:</strong> A price decline can continue.
              Follow your schedule unless your financial situation changes.
            </li>
            <li>
              <strong>Review periodically:</strong> Reassess your budget,
              storage, insurance, and overall allocation every few months.
            </li>
          </ul>
        </div>

        <p className="learning-body-text stacking-dca-caution">
          Dollar-cost averaging can reduce timing risk, but it cannot guarantee
          a profit or protect against falling metal prices. Physical bullion
          premiums and resale spreads also mean your break-even price may be
          higher than the quoted spot price.
        </p>
      </section>

      <DiversifierTool contextType="stacking" />
    </div>
  );
}
