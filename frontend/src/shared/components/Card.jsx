import React from "react";

export default function Card({ title, children, className = "" }) {
  return (
    <div className={`editorial-card ${className}`.trim()}>
      {title && <h4>{title}</h4>}
      {children}
    </div>
  );
}
