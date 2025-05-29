import { useEffect } from 'react';
import './styles/Toast.css';
import {FiAlertCircle} from "react-icons/fi";

const Toast = ({ message, duration = 3000, onClose }) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    return (
        <div className="toast">
            <div className="toast-content">
                <FiAlertCircle className="toast-icon" />
                <span>{message}</span>
            </div>
        </div>
    );
};

export default Toast;