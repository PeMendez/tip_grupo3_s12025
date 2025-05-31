import { useState, useEffect } from 'react';
import { getActions } from '../api/ruleService'; // Asegúrate que la ruta sea correcta
import { actionTranslations, getTranslation } from '../api/ruleMapping.js'; // Importa tus traducciones

const useDeviceActions = (deviceType, token) => {
    const [actions, setActions] = useState([]); // Guardará [{ value: 'API_VAL', label: 'User Label' }, ...]
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!deviceType || !token) {
            setActions([]);
            return;
        }

        const fetchAndMapActions = async () => {
            setLoading(true);
            setError(null);
            try {
                const actionsData = await getActions(deviceType, token); //
                const mappedActions = actionsData.map(action => ({
                    value: action,
                    label: getTranslation(action, actionTranslations)
                }));
                console.log('useDeviceActions - Mapped Actions:', JSON.stringify(mappedActions, null, 2)); // Log importante
                setActions(mappedActions);
            } catch (err) {
                console.error('Error fetching actions:', err); //
                setError(err);
                setActions([]); //
            }
            setLoading(false);
        };

        fetchAndMapActions();
    }, [deviceType, token]);

    return { actions, loading, error };
};

export default useDeviceActions;