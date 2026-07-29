export class FuturesTradingCourse {
  constructor() {
    this.metadata = {
      title: "Futures Trading 101",
      version: "2.0.0",
      lastUpdated: "2026-06-21",
      category: "Financial Education",
    };

    this._definitions = {
      futuresContract: {
        title: "Futures Contract",
        definition:
          "A legally binding agreement to buy or sell a specific asset at a predetermined price on a specified date in the future.",
        keyTraits: [
          "Standardized contract specifications",
          "Centralized exchange execution",
          "Binding obligations",
        ],
      },
      derivativesContext: {
        title: "Derivatives Context",
        definition:
          "An overarching financial contract deriving its value from an underlying asset. All futures are derivatives; not all derivatives are futures.",
        types: ["Options", "Swaps", "Forwards", "Futures"],
      },
      versusDayTrading: {
        title: "Futures vs. Day Trading",
        definition:
          "Futures is the asset class (the instrument). Day trading is an execution strategy (closing all positions within the same session). You can day trade futures, but you don't have to.",
        pdtExemption:
          "Futures are exempt from the standard stock market $25,000 Pattern Day Trader rule.",
      },
    };

    this._marketMechanics = {
      hours: {
        schedule: "24 hours a day, 6 days a week",
        details:
          "Opens Sunday evening (~6:00 PM EST) through Friday afternoon, featuring a brief daily afternoon maintenance break.",
      },
      limits: {
        mechanism: "Limit Up / Limit Down",
        purpose:
          "Exchange-mandated price bands designed to curb extreme market volatility and sudden panic sell-offs.",
      },
    };

    this._roadmap = [
      {
        step: 1,
        title: "Select a Specialized Broker",
        action:
          "Choose platforms built for futures execution (e.g., NinjaTrader, Tradovate, Interactive Brokers).",
      },
      {
        step: 2,
        title: "Pass Suitability Checks",
        action:
          "Verify financial risk tolerance and net worth with your broker due to integrated leverage.",
      },
      {
        step: 3,
        title: "Master Margins",
        action:
          "Understand the massive variance between low Intraday Margin (day trading) and higher Maintenance Margin (overnight holding).",
      },
      {
        step: 4,
        title: "Scale with Micro Contracts",
        action:
          "Practice live using Micro E-mini contracts, which are 1/10th the size of standard contracts.",
      },
    ];

    this._risks = [
      {
        type: "Leverage Risk",
        warning:
          "Low margin requirements multiply both profits and losses; adverse movements can exceed account balances.",
      },
      {
        type: "Expiration & Delivery",
        warning:
          "Contracts have rigid lifespans. Positions must be rolled or cash-settled before expiration to avoid delivery complications.",
      },
    ];

    this._videoLessons = [
      {
        title:
          "The Only Futures Trading Video You Will Ever Need (Beginner to Expert)",
        src: "https://www.youtube-nocookie.com/embed/Eebx6eGMc_A",
      },
    ];
  }

  // --- GETTERS ---

  /** @returns {Object} Course title, version, and category metadata. */
  getCourseMetadata() {
    return this.metadata;
  }

  /** @returns {Object} All core vocabulary and structural definitions. */
  getDefinitions() {
    return this._definitions;
  }

  /** @returns {Object} Market trading hours and circuit breaker mechanics. */
  getMarketMechanics() {
    return this._marketMechanics;
  }

  /** @returns {Array} Structured four-step onboarding roadmap. */
  getRoadmap() {
    return this._roadmap;
  }

  /** @returns {Array} Systemic risk data points. */
  getRisks() {
    return this._risks;
  }

  /** @returns {Array} Curated video lessons for the futures module. */
  getVideoLessons() {
    return this._videoLessons;
  }

  // --- UTILITY METHODS ---

  /**
   * Safely fetches a singular definition by key.
   * @param {string} key - e.g., 'futuresContract' or 'versusDayTrading'
   * @returns {Object|null}
   */
  getDefinitionByKey(key) {
    return this._definitions[key] ?? null;
  }

  /**
   * Filters the risk array for entries matching a keyword.
   * @param {string} keyword
   * @returns {Array}
   */
  searchRisks(keyword) {
    const lowerKey = keyword.toLowerCase();
    return this._risks.filter(
      (risk) =>
        risk.type.toLowerCase().includes(lowerKey) ||
        risk.warning.toLowerCase().includes(lowerKey),
    );
  }
}

export const courseInstance = new FuturesTradingCourse();
