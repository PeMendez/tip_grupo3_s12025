import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import './index.css';

const registerServiceWorker = async () => {
    if ('serviceWorker' in navigator) {
        try {
            const registration = await navigator.serviceWorker.register('/service-worker.js');
            console.log('Service Worker registrado con éxito:', registration);

            window.addEventListener('load', async () => {
                const permission = await Notification.requestPermission();
                if (permission === 'granted') {
                    console.log('Permiso para notificaciones concedido');
                }
            });
        } catch (error) {
            console.error('Error al registrar el Service Worker:', error);
        }
    }
};

const container = document.getElementById('root');
const root = createRoot(container);
root.render(
    <StrictMode>
        <App />
    </StrictMode>
);

registerServiceWorker();