import React, { useState } from 'react';
import { addAsset } from '../services/api';

const AssetForm = ({ onAssetAdded }) => {
  const [name, setName] = useState('');
  const [price, setPrice] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    await addAsset({ name, purchasePrice: price });
    onAssetAdded(); // Trigger list refresh
  };

  return (
    <form onSubmit={handleSubmit}>
      <input placeholder="Asset Name" onChange={(e) => setName(e.target.value)} />
      <input placeholder="Price" onChange={(e) => setPrice(e.target.value)} />
      <button type="submit">Add Asset</button>
    </form>
  );
};
export default AssetForm;