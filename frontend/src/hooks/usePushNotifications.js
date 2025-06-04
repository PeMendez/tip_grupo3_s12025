import { useEffect, useState } from "react";
import { subscribeToPushNotifications } from "../api/pushNotification.js";

const usePushNotifications = () => {
    const [isSubscribed, setIsSubscribed] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const handlePushSubscription = async () => {
            const token = localStorage.getItem("token");
            if (token) {
                try {
                    const subscription = await subscribeToPushNotifications();
                    if (subscription) {
                        setIsSubscribed(true);
                        console.log("Suscripción a notificaciones push exitosa:", subscription);
                    } else {
                        throw new Error("No se pudo completar la suscripción.");
                    }
                } catch (err) {
                    console.error("Error al suscribirse a notificaciones push:", err);
                    setError(err.message);
                }
            }
        };

        handlePushSubscription();
    }, []);

    const unsubscribe = async () => {
        try {
            const registration = await navigator.serviceWorker.ready;
            const subscription = await registration.pushManager.getSubscription();
            if (subscription) {
                const success = await subscription.unsubscribe();
                if (success) {
                    setIsSubscribed(false);
                    console.log("Desuscripción exitosa.");
                }
            }
        } catch (err) {
            console.error("Error al desuscribirse:", err);
            setError(err.message);
        }
    };

    return { isSubscribed, error, unsubscribe }; // Retorna el estado de la suscripción y los posibles errores
};

export default usePushNotifications;
