import React from 'react';

const AssetList = ({ assets }) => {
  return (
    <div>
      <h2>Your Assets</h2>
      {assets.length === 0 ? (
        <p>No assets found. Add some!</p>
      ) : (
        <ul>
          {assets.map((asset) => (
            <li key={asset.id}>
              {asset.name} - ${asset.purchasePrice}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default AssetList;