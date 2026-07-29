import React, { useState } from "react";
import { API_BASE } from "../../../constants/appConstants";
import { uploadProfilePicture } from "../../assets/api/assetApi";

export default function ProfileUploader({
  username = "default",
  initialProfilePic = null,
}) {
  const imageUsername = encodeURIComponent(username || "default");
  const defaultEndpoint = `${API_BASE}/api/users/profile-picture/${imageUsername}`;
  const [previewUrl, setPreviewUrl] = useState(
    initialProfilePic || defaultEndpoint,
  );
  const [localPreviewUrl, setLocalPreviewUrl] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [imageError, setImageError] = useState(false);
  const currentPreviewUrl = !imageError && (localPreviewUrl || previewUrl);

  const handleProfilePicChange = async (e) => {
    const input = e.target;
    const file = input.files?.[0];
    if (!file) return;

    const localUrl = URL.createObjectURL(file);
    setLocalPreviewUrl(localUrl);
    setImageError(false);
    const formData = new FormData();
    formData.append("file", file);
    formData.append("username", username);

    try {
      setIsUploading(true);
      const data = await uploadProfilePicture(formData);
      setPreviewUrl(`${API_BASE}${data.url}?updated=${Date.now()}`);
    } catch (error) {
      console.error(
        "Error saving image binary:",
        error.response?.data || error,
      );
      alert(
        error.response?.data?.error ||
        "Failed to sync profile picture modifications to backend servers.",
      );
    } finally {
      setIsUploading(false);
      input.value = "";
    }
  };

  return (
    <label className={`profile-pic-box ${isUploading ? "uploading" : ""}`}>
      {currentPreviewUrl ? (
        <img
          src={currentPreviewUrl}
          alt="User Profile"
          className="profile-img-preview"
          onError={() => setImageError(true)}
        />
      ) : (
        <span>Click to Upload 300x300 Pic</span>
      )}
      <input
        type="file"
        accept="image/*"
        onChange={handleProfilePicChange}
        className="visually-hidden"
      />
    </label>
  );
}
