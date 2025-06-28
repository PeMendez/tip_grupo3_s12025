export const attributeTranslations = {
    TEMPERATURE: 'Temperatura',
    IS_ON: 'Estado',
    IS_OPEN: 'Está',
    BRIGHTNESS: 'Brillo',
    TIME: 'Hora'
    // Agrega aquí todos los atributos que necesites traducir
};

export const operatorTranslations = {
    GREATER_THAN: 'Mayor que',
    LESS_THAN: 'Menor que',
    EQUALS: '=',
    // Agrega aquí todos los operadores
};

export const actionTranslations = {
    TURN_ON: 'Encender',
    TURN_OFF: 'Apagar',
    SET_BRIGHTNESS: 'Ajustar Brillo a ',
    // Agrega aquí todas las acciones
};

export const outletBooleanValueTranslations = {
    'TRUE': 'Encendido',
    'FALSE': 'Apagado',
};

export const openingSensorBooleanValueTranslations = {
    'TRUE': 'Abierto',
    'FALSE': 'Cerrado',
};

// Función genérica para obtener la traducción o el valor original
export const getTranslation = (value, translationMap) => {
    return translationMap[value.toUpperCase()] || value;
};