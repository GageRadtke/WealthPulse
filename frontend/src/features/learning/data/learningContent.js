export const LEARNING_TABS = [
  { id: "equities", label: "Stocks & ETFs" },
  { id: "stacking", label: "Precious Metals Stacking" },
  { id: "futures", label: "Bonus: Futures Contracts" },
];

export const EQUITIES_CARDS = [
  {
    title: "What Is Investing?",
    body: "Investing is the process of putting money into assets with the hope that their value will increase over time, such as stocks, bonds, or real estate. Saving is for immediate needs in a secure place with minimal risk and growth potential. Trading attempts to profit from short-term price movement and can be risky and time-consuming. Investing puts money into long-term assets, taking on some risk in return for possible growth over years or decades.",
  },
  {
    title: "What Is a Stock?",
    body: "A stock represents fractional equity ownership in an individual corporation. When you purchase a share, you claim fractional stakes in that company's financial growth. An ETF is an aggregated basket of various underlying investments managed as a single tradable product.",
  },
  {
    title: "Stocks vs. Funds",
    body: "Buying an individual stock isolates risk to one company's success or failure. An ETF dilutes that asset volatility by pooling dozens or hundreds of entities concurrently. Both trade on live tickers continuously throughout open market hours.",
  },
  {
    title: "Derivatives",
    body: "An option gives its buyer the right, but not the obligation, to buy or sell an underlying asset at a specified strike price by its expiration. A standard U.S. equity option usually represents 100 shares: calls provide the right to buy, while puts provide the right to sell. Corporate actions such as stock splits, mergers, and special distributions can produce adjusted contracts with different deliverables, so investors should verify each contract's specifications before trading.",
  },
  {
    title: "What Is a Bond?",
    body: "A bond is a loan you make to a government, municipality, or company. In return, the borrower generally pays you interest at set intervals and repays the bond's principal when it reaches maturity. Bonds are often less volatile than stocks, but they still carry risks such as inflation, changing interest rates, and the possibility that the borrower cannot repay the debt.",
  },
  {
    title: "Where to Learn How to Invest Like a Pro",
    body: "Build your investing knowledge one topic at a time. Investopedia's learning library covers investing basics, stocks, bonds, ETFs, portfolio management, and market analysis, making it a useful place to continue learning and researching unfamiliar terms.",
    link: {
      label: "Explore Investopedia's Investing Guide",
      href: "https://www.investopedia.com/investing-4427685",
    },
  },
  {
    title: "What Are Target-Date Funds?",
    fullWidth: true,
    sections: [
      {
        type: "paragraph",
        text: "A target-date fund is a diversified mutual fund or ETF designed for a long-term goal such as retirement. Its name usually includes a year, such as 2060, but the date is only a starting point: investors should also compare the fund's strategy, fees, risk level, and fit with their other savings.",
      },
      {
        type: "list",
        heading: "How the glide path works",
        items: [
          {
            label: "When you are young",
            desc: "The fund focuses heavily on growth, investing mostly in stocks to maximize long-term gains.",
          },
          {
            label: "As you age",
            desc: "The fund's adviser gradually adjusts allocations among underlying stock, bond, and other funds according to a planned glide path. The mix generally becomes more conservative, but the timing, holdings, and risk level vary by fund and losses remain possible.",
          },
        ],
      },
      {
        type: "paragraph",
        text: "You can learn more about how these options work by reviewing the SEC Investor Guide on Target-Date Funds.",
      },
      {
        type: "flow",
        heading: "The typical 401(k) investment mix",
        root: "Your 401(k) Contributions",
        items: [
          "Mutual Funds / ETFs - stocks for growth",
          "Target-Date Funds - automatic age-based mix",
          "Fixed Income / Stable Value - lower-volatility choices",
        ],
      },
      {
        type: "list",
        heading: "Primary building blocks",
        items: [
          {
            label: "Mutual Funds & ETFs",
            desc: "Instead of buying individual stocks like Apple or Amazon, your 401(k) pools your money to buy index funds that hold hundreds of companies at once.",
          },
          {
            label: "Bonds & Fixed-Income Funds",
            desc: "These funds hold government and corporate debt to provide steady interest and cushion your portfolio when the stock market is volatile.",
          },
          {
            label: "Stable Value / Cash Funds",
            desc: "These are conservative plan options intended to preserve capital and provide modest returns, but they are not risk-free. Depending on the product, risks can include inflation reducing purchasing power, issuer or wrap-provider credit problems, withdrawal restrictions, liquidity limits, fees, and plan events that affect access to book value. Review the plan's specific terms before investing.",
          },
        ],
      },
      {
        type: "paragraph",
        text: "Some retirement plans use a target-date fund as the default investment for participants who have not made their own selection. The fund handles allocation and rebalancing according to its stated strategy, but it does not guarantee sufficient retirement income or eliminate the need to review its fees, glide path, risk, and continued suitability.",
      },
    ],
  },
];

export const TRADING_INSTRUCTIONS = [
  {
    step: "1",
    title: "The Infrastructure: Online Brokerages & Clearing Markets",
    body: "When you place a trade, your retail brokerage acts as the gateway to public clearing markets (e.g., exchanges like the NYSE or Nasdaq). To start, research top-rated online brokers, create an account, and complete the identity verification process. You can read up on the top platforms in the Investopedia Best Brokers for Beginners Guide.",
  },
  {
    step: "2",
    title: "Funding the Account",
    body: "Trades are settled in cash. You must link your local checking account (via an Automated Clearing House or wire transfer) to deposit funds. Once your linked account is verified, initiate an electronic funds transfer. Depending on the brokerage, you may be granted 'instant buying power' while the cash transfer clears over 1 to 3 business days."
  },
  {
    step: "3",
    title: "Asset Identification: Ticker Symbols",
    body: "Every publicly traded company is assigned a unique series of letters called a ticker symbol (e.g., AAPL for Apple, MSFT for Microsoft) used to identify the equity you want to trade.",
  },
  {
    step: "4",
    title: "Execution Mechanics: Order Types",
    mechanism: "Choosing how your broker buys or sells the stock determines your price control vs. execution speed.",
    actionItem: null,
    orders: [
      {
        name: "Market Order",
        how: "Instructs the broker to buy or sell immediately at the best available current market price.",
        prosCons: "Provides greater execution certainty than a limit order, but neither immediate execution nor a specific price is guaranteed. Quotes can change while an order is routed, and fast-moving or thinly traded markets can produce substantial slippage or partial fills.",
        when: "Highly liquid large-cap stocks where speed matters more than a $0.05 difference in share price."
      },
      {
        name: "Limit Order",
        how: "Instructs the broker to execute the trade only if the asset hits a specific price target (or better). A buy limit order executes at your target price or lower; a sell limit order executes at your target price or higher.",
        prosCons: "Guarantees price control, but does not guarantee execution. If the stock never reaches your target, your order sits unfulfilled.",
        when: "Volatile stocks, or when you are trying to buy a dip and want strict control over your entry price."
      }
    ]
  }
];

export const EQUITIES_VIDEO_LESSONS = [
  {
    title: "Stocks & ETFs Video Guide",
    src: "https://www.youtube-nocookie.com/embed/98qfFzqDKR8",
  },
  {
    title: "Stocks & ETFs Lesson",
    src: "https://www.youtube-nocookie.com/embed/DPsUntwGIAg",
  },
  {
    title: "Index Funds vs. Mutual Funds vs Hedge Funds vs ETFs Explained",
    src: "https://www.youtube-nocookie.com/embed/qbaZHZkadfg",
  },
  {
    title: "Options Trading for Beginners: Total Guide with Examples!",
    src: "https://www.youtube-nocookie.com/embed/hcalZ_sRtRY",
  },
];



export const STRATEGY_ROWS = [
  {
    metric: "Time Horizon",
    dayTrading: "Minutes to hours; zero overnight exposure.",
    longTerm: "Months, years, or decades.",
  },
  {
    metric: "Primary Driver",
    dayTrading: "Short-term price volatility and technical charts.",
    longTerm: "Corporate fundamentals and macroeconomic growth.",
  },
  {
    metric: "Success Rate",
    dayTrading: "Many day traders suffer severe losses, particularly when using leverage, and consistent profitability is difficult.",
    longTerm: "Diversified long-term investing has more time to recover from volatility and benefit from compounding, but returns are never guaranteed.",
    dayClassName: "learning-danger-text",
    longClassName: "learning-success-text",
  },
];

export const RED_FLAGS = [
  {
    label: "High Expense Ratios",
    desc: "Hidden operational management fees that quietly erode long-term ETF returns.",
  },
  {
    label: "Leverage & Margin",
    desc: "Borrowing funds from a broker to trade. This amplifies minor losses into catastrophic debts.",
  },
  {
    label: "Emotional FOMO",
    desc: "Panic buying at performance peaks or panic selling during standard market corrections.",
  },
];

export const GLOSSARY_TERMS = [
  {
    label: "Stock",
    desc: "A security that represents a fractional ownership share in a corporation.",
  },
  {
    label: "Share",
    desc: "A single unit of ownership in a company.",
  },
  {
    label: "Equities",
    desc: "Another term for stocks, representing equal parts of ownership in a company.",
  },
  {
    label: "Initial Public Offering (IPO)",
    desc: "The first time a private company sells its shares of stock to the general public.",
  },
  {
    label: "Common Stock",
    desc: "A type of stock that represents equity ownership and typically carries voting rights at shareholder meetings.",
  },
  {
    label: "Preferred Stock",
    desc: "A class of ownership that generally has priority over common stock for dividends and liquidation proceeds but usually has limited or no voting rights. Dividends are not guaranteed and may be suspended; missed cumulative dividends normally accrue, while missed noncumulative dividends generally do not.",
  },
  {
    label: "Dividend",
    desc: "A portion of a company's profit distributed to its shareholders, usually paid out in cash quarterly.",
  },
  {
    label: "Capital Appreciation",
    desc: "An increase in the market price of a stock or asset over time.",
  },
  {
    label: "Blue-Chip Stock",
    desc: "Shares in a massive, financially stable, and well-known corporation with a history of reliable performance.",
  },
  {
    label: "Growth Stock",
    desc: "A share in a company that is expected to grow earnings at an above-average rate compared to the rest of the market.",
  },
  {
    label: "Value Stock",
    desc: "A stock that appears to be trading at a low price relative to its fundamental financial health, such as earnings or sales.",
  },
  {
    label: "Dividend Yield",
    desc: "The financial ratio calculated as the annual dividend paid out divided by the stock's current price.",
  },
  {
    label: "Price-to-Earnings (P/E) Ratio",
    desc: "A valuation metric used to compare a company's current share price to its per-share earnings.",
  },
  {
    label: "Earnings Per Share (EPS)",
    desc: "A company's profit allocated to each outstanding share of common stock.",
  },
  {
    label: "Market Capitalization",
    desc: "The total dollar market value of a company's outstanding shares.",
  },
  {
    label: "Outstanding Shares",
    desc: "The total number of shares currently held by all shareholders, including insiders and institutional investors.",
  },
  {
    label: "Stock Split",
    desc: "When a company divides its existing shares into multiple new shares to lower the price per share and increase liquidity.",
  },
  {
    label: "Sector",
    desc: "A specific area or industry of the economy, such as technology, healthcare, or energy.",
  },
  {
    label: "Bond",
    desc: "A fixed-income instrument that represents a loan made by an investor to a borrower, such as a corporation or government.",
  },
  {
    label: "Fixed Income",
    desc: "An investment strategy or asset class that pays out a set return or interest payments, such as bonds.",
  },
  {
    label: "Issuer",
    desc: "The legal entity, such as a corporation, municipality, or government, that sells bonds to raise capital.",
  },
  {
    label: "Principal",
    desc: "The original amount of money lent to the bond issuer, which is returned at the bond's maturity date.",
  },
  {
    label: "Maturity Date",
    desc: "The specific date on which the bond issuer must repay the principal balance to the bondholder.",
  },
  {
    label: "Coupon Rate",
    desc: "The annual interest rate paid by the bond issuer to the investor, expressed as a percentage of the bond's face value.",
  },
  {
    label: "Yield to Maturity (YTM)",
    desc: "The total anticipated return on a bond if the bond is held until its maturity date.",
  },
  {
    label: "Treasury Bond (T-Bond)",
    desc: "A long-term U.S. government debt security with a maturity of 10 to 30 years backed by the federal government.",
  },
  {
    label: "Treasury Bill (T-Bill)",
    desc: "A short-term U.S. government debt security with a maturity date ranging from a few days to 52 weeks.",
  },
  {
    label: "Municipal Bond",
    desc: "A debt security issued by a state, county, or municipality to fund local public projects, often featuring tax-free interest.",
  },
  {
    label: "Corporate Bond",
    desc: "Debt instruments issued by public or private companies to fund capital expansion, operations, or acquisitions.",
  },
  {
    label: "Junk Bond",
    desc: "A high-yield corporate bond with a low credit rating that carries a higher risk of default but pays a higher interest rate.",
  },
  {
    label: "Default",
    desc: "The failure of a bond issuer to pay the interest or return the principal when it comes due.",
  },
  {
    label: "Credit Rating",
    desc: "An independent assessment of a bond issuer's creditworthiness and financial ability to fulfill its payment obligations.",
  },
  {
    label: "Bond Ladder",
    desc: "A portfolio of fixed-income bonds scheduled to mature at staggered future dates to reduce interest rate risk.",
  },
  {
    label: "Exchange-Traded Fund (ETF)",
    desc: "A basket of securities, such as stocks, bonds, or commodities, that trades on an exchange throughout the day like a stock.",
  },
  {
    label: "Basket of Securities",
    desc: "A grouped collection of diverse individual investments packaged together inside a fund.",
  },
  {
    label: "Index Fund",
    desc: "A portfolio structured as a mutual fund or ETF and built to track the components of a specific financial market index.",
  },
  {
    label: "Index",
    desc: "A statistical measure tracking the performance of a selected basket of assets or companies representing a market segment.",
  },
  {
    label: "Passive Management",
    desc: "An investing strategy that tracks a market index rather than paying a manager to pick assets.",
  },
  {
    label: "Active Management",
    desc: "A fund management style where a professional manager makes intentional buy and sell decisions to beat market returns.",
  },
  {
    label: "Expense Ratio",
    desc: "The percentage of your total investment deducted annually by fund managers to cover administrative and operating costs.",
  },
  {
    label: "Liquidity",
    desc: "The ease with which an asset or security can be bought or sold quickly without causing a major price swing.",
  },
  {
    label: "Diversification",
    desc: "A risk management strategy of spreading your capital across various assets, industries, and locations to minimize risk.",
  },
  {
    label: "Net Asset Value (NAV)",
    desc: "The total value of a fund's assets minus its liabilities, divided by the number of outstanding shares.",
  },
  {
    label: "Bond ETF",
    desc: "An exchange-traded fund that invests exclusively in a basket of fixed-income bonds.",
  },
  {
    label: "Sector ETF",
    desc: "An ETF that holds a basket of stocks tied to a specific industry, such as financials or tech.",
  },
  {
    label: "Commodity ETF",
    desc: "An investment fund that tracks the performance of a physical commodity, such as gold or oil.",
  },
  {
    label: "Leveraged ETF",
    desc: "An aggressive fund that uses financial derivatives and debt to generate returns that are a multiple of the underlying index.",
  },
  {
    label: "Inverse ETF",
    desc: "A fund designed to deliver the opposite return of an index, allowing investors to profit when the market falls.",
  },
  {
    label: "TIPS ETF",
    desc: "A fund holding Treasury Inflation-Protected Securities designed to protect investments against inflation.",
  },
  {
    label: "Dividend ETF",
    desc: "An exchange-traded fund composed of companies that distribute steady, attractive dividend yields.",
  },
];

export const OPTIONS_TRADING_CARD = {
  title: "⚡ Options Trading: Calls, Puts & Strikes",
  intro: "Options trading is completely different from buying normal stock. Instead of buying the actual shares, you are buying a contract that gives you the right to buy or sell a stock at a specific price before an expiration date.",
  types: [
    {
      name: "🟢 Call Option (The 'Buy' Reservation)",
      view: "Bullish (You think the stock price will go UP)",
      right: "Gives you the right to BUY a stock at a fixed price.",
      analogy: "Putting down a non-refundable deposit to lock in the price of a house before it hits the market.",
      winCondition: "If the stock rockets past your fixed price, your contract lets you buy it cheap or sell the contract for a massive profit."
    },
    {
      name: "🔴 Put Option (The 'Sell' Insurance)",
      view: "Bearish (You think the stock price will go DOWN)",
      right: "Gives you the right to SELL a stock at a fixed price.",
      analogy: "Buying car insurance. You pay a small fee (premium) so if your asset crashes, you get a fixed payout.",
      winCondition: "If the stock crashes way below your fixed price, your contract letting you sell it at the higher price becomes highly valuable."
    }
  ],
  strikeExplanation: {
    title: "🎯 What is a Strike Price?",
    body: "The Strike Price is the locked-in price at which you have the right to buy or sell the stock, no matter how crazy the actual market price gets. If the stock doesn't move past your strike price before the contract's Expiration Date, the option expires completely worthless, and you lose what you paid for it (the Premium)."
  },
  warning: "⚠️ Critical Warning for Beginners: Options are complex, leveraged instruments. Buyers can lose the entire premium, while some uncovered option-selling strategies can create losses greater than the premium received and, in certain cases, theoretically unlimited losses. Time decay, volatility, liquidity, assignment, and expiration all affect outcomes. Learn the contract terms and consider practicing with paper trading before risking real capital."
};
