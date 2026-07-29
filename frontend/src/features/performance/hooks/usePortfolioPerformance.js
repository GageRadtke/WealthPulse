import { useEffect, useState } from "react";
import { getPortfolioPerformance } from "../api/performanceApi";

export default function usePortfolioPerformance(period, benchmark, refreshKey) {
  const [state, setState] = useState({ data: null, loading: true, error: "" });

  useEffect(() => {
    let active = true;
    setState((previous) => ({ ...previous, loading: true, error: "" }));
    getPortfolioPerformance(period, benchmark)
      .then(({ data }) => {
        if (active) setState({ data, loading: false, error: "" });
      })
      .catch(() => {
        if (active) {
          setState({
            data: null,
            loading: false,
            error: "Performance analytics are temporarily unavailable.",
          });
        }
      });
    return () => { active = false; };
  }, [period, benchmark, refreshKey]);

  return state;
}
