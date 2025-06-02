const DimmerControl = ({ device, setBrightness }) => {
    return (
        <div className="slider-container" onClick={(e) => e.stopPropagation()}>
            <label className="slider-label">
                <input
                    type="range"
                    min="0"
                    max="100"
                    value={device.brightness || 0}
                    onChange={(e) => setBrightness(device, e.target.value)}
                />
                <span className="slider-value">{device.brightness || 0}%</span>
            </label>
        </div>
    );
};

export default DimmerControl;