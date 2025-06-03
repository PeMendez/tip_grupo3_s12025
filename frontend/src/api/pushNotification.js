// pushNotifications.js
export const subscribeToPushNotifications = async () => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
        console.warn('Push notifications no son soportadas');
        return null;
    }

    const VAPID_PUBLIC_KEY = 'BLL-7oUmgMqtohKsK-JwA4X2gzm0FW_ia5fWVj1zq9BhRTtYDrNsKLCuo066Ju6I4vEJgcljU-Z7S1RiAW1NHj8'

    try {
        const registration = await navigator.serviceWorker.ready;
        const existingSubscription = await registration.pushManager.getSubscription();

        if (existingSubscription) {
            return existingSubscription;
        }

        const subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY)
        });

        // Envía la suscripción a tu servidor
        await sendSubscriptionToServer(subscription);

        return subscription;
    } catch (error) {
        console.error('Error al suscribirse a push notifications:', error);
        return null;
    }
};

const sendSubscriptionToServer = async (subscription) => {
    // Implementa el envío a tu backend
    const response = await fetch('/api/push-subscribe', {
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