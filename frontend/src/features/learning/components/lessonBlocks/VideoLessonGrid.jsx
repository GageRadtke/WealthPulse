import React from "react";
import Card from "../../../../shared/components/Card";

export default function VideoLessonGrid({ videos }) {
  return (
    <div className="learning-card-layout-grid">
      {videos.map((video) => (
        <Card key={video.src} title={video.title}>
          <div className="learning-video-frame">
            <iframe
              src={video.src}
              title={video.title}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              referrerPolicy="strict-origin-when-cross-origin"
              allowFullScreen
            />
          </div>
        </Card>
      ))}
    </div>
  );
}
