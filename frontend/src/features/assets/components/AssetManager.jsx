import AssetForm from "./forms/AssetForm";
import BulkUploadForm from "./BulkUploadForm";

export default function AssetManager({ onAssetAdded }) {
  return (
    <div className="asset-manager-section">
      <div className="forms-container-row">
        <div className="form-card">
          <AssetForm onAssetAdded={onAssetAdded} />
        </div>
        <div className="form-card bulk-card">
          <BulkUploadForm onAssetAdded={onAssetAdded} />
        </div>
      </div>
    </div>
  );
}
