import React, { useState, useEffect } from "react";
import "./App.css";
import { getAssets } from "./services/api"; // Ensure this file exists
import AssetForm from "./components/AssetForm";
import AssetList from "./components/AssetList";

function App() {
  const [assets, setAssets] = useState([]);

  const fetchAssets = async () => {
    try {
      const response = await getAssets();
      setAssets(response.data);
    } catch (error) {
      console.error("Error fetching assets:", error);
    }
  };

  useEffect(() => {
    fetchAssets();
  }, []);

  return (
    <div className="App">
      <h1>WealthPulse Asset Manager</h1>

      {/* Your modular components replace the boilerplate */}
      <AssetForm onAssetAdded={fetchAssets} />
      <AssetList assets={assets} onUpdate={fetchAssets} />
    </div>
  );
}

export default App;
