import { useEffect } from 'react';
import './styles/Toast.css';
import { FiAlertCircle, FiCheckCircle, FiInfo } from "react-icons/fi";

const Toast = ({ message,type = 'info', duration = 3000, onClose }) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    const getIcon = () => {
        switch(type) {
            case 'success':
                return <FiCheckCircle className="toast-icon" />;
            case 'error':
                return <FiAlertCircle className="toast-icon" />;
            case 'info':
            default:
                return <FiInfo className="toast-icon" />;
        }
    };



    return (
        <div className={`toast toast-${type}`}>
            <div className="toast-content">
                {getIcon()}
                <span>{message}</span>
            </div>
        </div>
    );
};

export default Toast;