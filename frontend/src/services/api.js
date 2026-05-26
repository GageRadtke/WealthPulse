import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

export const getAssets = () => axios.get(`${API_BASE}/assets`);
export const addAsset = (asset) => axios.post(`${API_BASE}/assets`, asset);
export const updateAssetPrice = (id, price) => axios.patch(`${API_BASE}/assets/${id}/price`, { price });