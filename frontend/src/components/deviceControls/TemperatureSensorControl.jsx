const TemperatureSensorControl = ({ device }) => {
    return (
        <div className="device-info">
            <small>{device.temperature}°C</small>
        </div>
    );
};

export default TemperatureSensorControl;