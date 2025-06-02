import {
    FiSun, FiThermometer, FiShield, FiVideo, FiLock
} from 'react-icons/fi';
import { LuAirVent } from "react-icons/lu";
import { TiLightbulb } from "react-icons/ti";
import { MdOutlineBrightness4 } from "react-icons/md";

export const deviceTypeIcons = {
    smart_outlet: <TiLightbulb size={24} />,
    temperature_sensor: <FiThermometer size={24} />,
    opening_sensor: <FiShield size={24} />,
    AIR_CONDITIONER: <LuAirVent size={24} />,
    TV_CONTROL: <FiVideo size={24} />,
    smartOutlet: <FiLock size={24} />,
    dimmer: <MdOutlineBrightness4 size={24} />
};

export const getDeviceIcon = (type) => deviceTypeIcons[type] || <FiSun size={24} />;