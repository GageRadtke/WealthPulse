import { useState, useEffect, useCallback } from "react";
import {
  getAssets,
  deleteAsset as deleteAssetApi,
  addOrUpdateAsset,
  refreshAssetPrices,
  updateMetalPurity as updateMetalPurityApi,
} from "../api/assetApi";
import apiClient from "../../../api/client";
import { APP_MODE } from "../../../constants/appConstants";
import { applyCachedPrices, createCachedPriceMap } from "../../../shared/finance/marketPricing";

export function useAssets(appMode) {
  const [assets, setAssets] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Data loading -------------------------------------------------------------
  // Cache data supplements persisted assets when the latest market price was
  // refreshed independently by the backend.
  const fetchAssets = useCallback(async () => {
    if (appMode !== APP_MODE.LIVE) return;
    setIsLoading(true);
    try {
      const response = await getAssets();
      const cacheResponse = await apiClient.get("/cache/status").catch(() => ({ data: [] }));
      const cachedPrices = createCachedPriceMap(cacheResponse.data);
      setAssets(applyCachedPrices(response.data || [], cachedPrices));
    } catch (error) {
      console.error("Error fetching portfolio data database entries:", error);
    } finally {
      setIsLoading(false);
    }
  }, [appMode]);

  useEffect(() => {
    fetchAssets();
  }, [fetchAssets]);

  // User actions -------------------------------------------------------------
  const deleteAsset = useCallback(async (id) => {
    try {
      await deleteAssetApi(id);
      await fetchAssets();
    } catch (error) {
      console.error("Error deleting asset:", error);
    }
  }, [fetchAssets]);

  const saveAsset = useCallback(async (assetPayload) => {
    try {
      const response = await addOrUpdateAsset(assetPayload);
      await fetchAssets();
      return response.data;
    } catch (error) {
      console.error("Error writing new position to standard data tables:", error);
      throw error;
    }
  }, [fetchAssets]);

  const updateQuantity = useCallback(async (id, quantityDelta, purityKarat) => {
    try {
      const assetToUpdate = assets.find((a) => a.id === id);
      if (!assetToUpdate) return;
      const delta = Number(quantityDelta);
      const selectedPurity = Number(purityKarat);
      const hasPurityUpdate = [10, 14, 18, 22, 24, 9999, 999, 958, 950, 935, 925, 900, 835, 830, 800]
        .includes(selectedPurity);
      if ((!Number.isFinite(delta) || delta === 0) && !hasPurityUpdate) return;

      const updatedPayload = {
        id: assetToUpdate.id,
        type: assetToUpdate.type,
        ticker: assetToUpdate.ticker,
        name: assetToUpdate.name,
        quantity: Number.isFinite(delta) ? delta : 0,
        ...(hasPurityUpdate ? { purityKarat: selectedPurity } : {}),
      };
      await saveAsset(updatedPayload); 
    } catch (error) {
      console.error("Error updating asset quantity snapshot:", error);
    }
  }, [assets, saveAsset]);

  const clearAssets = useCallback(() => {
    setAssets([]);
  }, []);

  const refreshPrices = useCallback(async () => {
    try {
      await refreshAssetPrices();
      await fetchAssets();
    } catch (error) {
      console.error("Error refreshing market prices:", error);
      throw error;
    }
  }, [fetchAssets]);

  const updateMetalPurity = useCallback(async (id, purityKarat) => {
    try {
      const response = await updateMetalPurityApi(id, purityKarat);
      setAssets((currentAssets) => currentAssets.map((asset) => (
        asset.id === id ? response.data : asset
      )));
      await fetchAssets();
    } catch (error) {
      console.error("Error updating metal purity:", error);
      throw error;
    }
  }, [fetchAssets]);

  return {
    assets,
    isLoading,
    deleteAsset,
    updateQuantity,
    saveAsset,
    refreshPrices,
    updateMetalPurity,
    clearAssets,
  };
}
