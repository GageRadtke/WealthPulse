export default function QuantityEditor({
  assetId,
  quantity,
  onQuantityChange,
  onUpdate,
}) {

  const handleApply = () => {

    const targetQuantity = parseFloat(quantity);

    if (isNaN(targetQuantity)) {
      return;
    }

    onUpdate(assetId, targetQuantity);

    onQuantityChange(assetId, "");
  };

  return (
    <div className="update-qty-actions">

      <input
        type="number"
        placeholder="Adjust (+/-)"
        className="qty-input-box"
        value={quantity}
        onChange={(event) =>
          onQuantityChange(assetId, event.target.value)
        }
      />

      <button
        className="btn-apply-qty"
        onClick={handleApply}
      >
        Apply
      </button>

    </div>
  );
}
