import React from "react";
import Card from "../../../../shared/components/Card";

/**
 * LessonCardGrid
 *
 * Renders a responsive grid of lesson cards.
 * Each card supports a simple body paragraph or a richer
 * multi-section layout (paragraph, list, flow).
 *
 * @param {Object[]} cards - Array of card data objects from learningContent.js
 */
export default function LessonCardGrid({ cards = [] }) {
  if (!cards.length) return null;

  return (
    <div className="learning-card-layout-grid">
      {cards.map((card, idx) => (
        <Card
          key={card.title || idx}
          title={card.title}
          className={card.fullWidth ? "learning-wide-card" : ""}
        >
          {/* Simple body */}
          {card.body && (
            <p className="learning-body-text">{card.body}</p>
          )}

          {card.link && (
            <a
              className="learning-resource-link"
              href={card.link.href}
              target="_blank"
              rel="noopener noreferrer"
            >
              {card.link.label} <span aria-hidden="true">↗</span>
            </a>
          )}

          {/* Rich multi-section cards */}
          {card.sections && (
            <div className="learning-rich-card-content">
              {card.sections.map((section, sIdx) => {
              if (section.type === "paragraph") {
                return (
                  <p key={sIdx} className="learning-body-text">
                    {section.text}
                  </p>
                );
              }

              if (section.type === "list") {
                return (
                  <div key={sIdx} className="learning-card-section">
                    {section.heading && (
                      <h5>{section.heading}</h5>
                    )}
                    <ul className="learning-card-list">
                      {section.items.map((item, iIdx) => (
                        <li key={iIdx}>
                          <strong>{item.label}:</strong> {item.desc}
                        </li>
                      ))}
                    </ul>
                  </div>
                );
              }

              if (section.type === "flow") {
                return (
                  <div key={sIdx} className="learning-card-section">
                    {section.heading && (
                      <h5>{section.heading}</h5>
                    )}
                    <div className="learning-flow-box">
                      <p>{section.root}</p>
                      <ul>
                        {section.items.map((item, iIdx) => (
                          <li key={iIdx}>{item}</li>
                        ))}
                      </ul>
                    </div>
                  </div>
                );
              }

              return null;
              })}
            </div>
          )}
        </Card>
      ))}
    </div>
  );
}
