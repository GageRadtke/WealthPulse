import apiClient from '../../../api/client';

export const getAssets = () =>
    apiClient.get("/assets");

export const addAsset = (asset) =>
    apiClient.post("/assets", asset);

export const deleteAsset = (id) =>
    apiClient.delete(`/assets/${id}`);

export const addOrUpdateAsset = (asset) =>
    apiClient.put("/assets/update-quantity", asset);

export const refreshAssetPrices = () =>
    apiClient.post("/assets/refresh-prices");

export const refreshStockFundamentals = () =>
    apiClient.post("/assets/refresh-fundamentals");

export const updateMetalPurity = (id, purityKarat) =>
    apiClient.put(`/assets/${id}/purity`, { purityKarat });

export const uploadProfilePicture = async (formData) => {
  const response = await apiClient.post('/users/profile-picture', formData, {
    headers: { 'Content-Type': undefined },
  });
  return response.data;
};
