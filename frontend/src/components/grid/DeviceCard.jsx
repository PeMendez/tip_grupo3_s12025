import { useNavigate } from "react-router-dom";
import { getDeviceIcon } from "./utils.jsx";
import SmartOutletControl from "../DeviceControls/SmartOutletControl";
import DimmerControl from "../DeviceControls/DimmerControl";
import TemperatureSensorControl from "../deviceControls/TemperatureSensorControl.jsx";
import OpeningSensorControl from "../deviceControls/OpeningSensorControl.jsx";
import './styles/deviceCard.css'

const DeviceCard = ({ device, toggleLight, setBrightness, editMode, onClick, addMode }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        if(editMode || addMode) {
            onClick(device);
        } else {
            navigate(`/device/${device.id}`, { state: { device } });

        }
    };

    const renderDeviceControl = () => {
        switch(device.type) {
            case "smart_outlet":
                return <SmartOutletControl device={device} toggleLight={toggleLight} />;
            case "dimmer":
                return <DimmerControl device={device} setBrightness={setBrightness} />;
            case "temperature_sensor":
                return <TemperatureSensorControl device={device} />;
            case "opening_sensor":
                return <OpeningSensorControl device={device} />;
            default:
                return null;
        }
    };

    return (
        <div
            className={`${device.type === 'opening_sensor' && device.status ? 'alarm-active' : ''}`}
            onClick={handleClick}
        >
            <div className={`device-icon ${device.type === 'opening_sensor' && device.status ? 'alarm-active' : ''}`}>
                {getDeviceIcon(device.type)}
            </div>
            <div className="marquee-container">
                <div className="marquee">
                    <span>{device.name}</span>
                </div>
            </div>

            {renderDeviceControl()}

            {device.type === 'opening_sensor' && device.status && (
                <span className="alarm-status">ACTIVA</span>
            )}
        </div>
    );
};

export default DeviceCard;