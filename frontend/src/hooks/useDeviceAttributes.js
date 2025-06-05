import { useState, useEffect } from 'react';
import { getAttributes } from '../api/ruleService'; // Asegúrate que la ruta sea correcta
import { attributeTranslations, getTranslation } from '../api/ruleMapping.js'; // Importa tus traducciones

const useDeviceAttributes = (deviceType, token) => {
    const [attributes, setAttributes] = useState([]); // Guardará [{ value: 'API_VAL', label: 'User Label' }, ...]
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!deviceType || !token) {
            setAttributes([]);
            return;
        }

        const fetchAndMapAttributes = async () => {
            setLoading(true);
            setError(null);
            try {
                const attrsData = await getAttributes(deviceType, token); //
                const mappedAttributes = attrsData.map(attr => ({
                    value: attr,
                    label: getTranslation(attr, attributeTranslations)
                }));
                setAttributes(mappedAttributes);
            } catch (err) {
                console.error('Error fetching attributes:', err); //
                setError(err);
                setAttributes([]); //
            }
            setLoading(false);
        };

        fetchAndMapAttributes();
    }, [deviceType, token]);

    return { attributes, loading, error };
};

export default useDeviceAttributes;