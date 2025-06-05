import { useState } from "react";

const DimmerControl = ({ device, setBrightness }) => {
    const [tempBrightness, setTempBrightness] = useState(device.brightness || 0);

    const handleSliderChange = (e) => {
        setTempBrightness(e.target.value);
    };

    const handleSliderRelease = () => {
        setBrightness(device, tempBrightness);
    };

    return (
        <div className="slider-container" onClick={(e) => e.stopPropagation()}>
            <label className="slider-label">
                <input
                    type="range"
                    min="0"
                    max="100"
                    value={tempBrightness}
                    onChange={handleSliderChange}
                    onMouseUp={handleSliderRelease}
                    onTouchEnd={handleSliderRelease}
                />
                <span className="slider-value">{tempBrightness}%</span>
            </label>
        </div>
    );
};

export default DimmerControl;
