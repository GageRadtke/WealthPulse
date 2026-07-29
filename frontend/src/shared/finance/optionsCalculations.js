export const calculateCallValue = (stockPrice, strikePrice) => {
  return Math.max(0, (Number(stockPrice) || 0) - (Number(strikePrice) || 0));
};

export const calculatePutValue = (stockPrice, strikePrice) => {
  return Math.max(0, (Number(strikePrice) || 0) - (Number(stockPrice) || 0));
};

export class OptionContract {
  constructor(stockPrice, strikePrice) {
    this.stockPrice = Number(stockPrice) || 0;
    this.strikePrice = Number(strikePrice) || 0;
  }

  get callValue() {
    return calculateCallValue(this.stockPrice, this.strikePrice);
  }

  get putValue() {
    return calculatePutValue(this.stockPrice, this.strikePrice);
  }
}
