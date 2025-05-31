import { useState, useEffect } from 'react';
import { getOperators } from '../api/ruleService'; // Asegúrate que la ruta sea correcta
import { operatorTranslations, getTranslation } from '../api/ruleMapping.js'; // Importa tus traducciones

const useAttributeOperators = (deviceType, token) => {
    const [operators, setOperators] = useState([]); // Guardará [{ value: 'API_VAL', label: 'User Label' }, ...]
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!deviceType || !token) {
            setOperators([]);
            return;
        }

        const fetchAndMapOperators = async () => {
            setLoading(true);
            setError(null);
            try {
                const operatorsData = await getOperators(deviceType, token); //
                const mappedOperators = operatorsData.map(oper => ({
                    value: oper,
                    label: getTranslation(oper, operatorTranslations)
                }));
                setOperators(mappedOperators);
            } catch (err) {
                console.error('Error fetching operators:', err); //
                setError(err);
                setOperators([]); //
            }
            setLoading(false);
        };

        fetchAndMapOperators();
    }, [deviceType, token]);

    return { operators: operators, loading, error };
};

export default useAttributeOperators;