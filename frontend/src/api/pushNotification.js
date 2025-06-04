// pushNotifications.js
const API_URL = import.meta.env.VITE_API_URL + "/push-subscribe"

export const subscribeToPushNotifications = async () => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
        console.warn('Push notifications no son soportadas');
        return null;
    }
    const VAPID_PUBLIC_KEY = import.meta.env.VITE_VAPID_PUBLIC_KEY

    try {
        const registration = await navigator.serviceWorker.ready;
        const existingSubscription = await registration.pushManager.getSubscription();
        console.log("Chequeo registracion...");

        if (existingSubscription) {
            console.log("Ya existe suscripcion. Me voy", existingSubscription);
            return existingSubscription;
        }

        console.log('Hay que suscribirse...');

        const subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY)
        });

        console.log('Suscripción actual:', subscription);

        // Envía la suscripción a tu servidor
        await sendSubscriptionToServer(subscription);

        console.log("Suscripcion enviada al server.");

        return subscription;
    } catch (error) {
        console.error('Error al suscribirse a push notifications:', error);
        return null;
    }
};

const sendSubscriptionToServer = async (subscription) => {
    // Implementa el envío a tu backend
    const response = await fetch(API_URL, {
        method: 'POST',
        body: JSON.stringify(subscription),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    });

    if (!response.ok) {
        throw new Error('Error al enviar suscripción al servidor');
    }
};

function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
        .replace(/-/g, '+')
        .replace(/_/g, '/');
    const rawData = window.atob(base64);
    return Uint8Array.from([...rawData].map((char) => char.charCodeAt(0)));
}