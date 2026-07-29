import React from "react";

export default function StrategyTable({ rows }) {
  return (
    <div className="learning-table-scroll">
      <table className="learning-strategy-table">
        <thead>
          <tr>
            <th>Metric</th>
            <th>Day Trading</th>
            <th>Long-Term Investing</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.metric}>
              <td>
                <strong>{row.metric}</strong>
              </td>
              <td className={row.dayClassName}>{row.dayTrading}</td>
              <td className={row.longClassName}>{row.longTerm}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
