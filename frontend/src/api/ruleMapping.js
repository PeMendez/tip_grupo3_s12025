export const attributeTranslations = {
    TEMPERATURE: 'Temperatura',
    IS_ON: 'Estado (Encendido/Apagado)',
    IS_OPEN: 'Estado (Abierto/Cerrado)',
    BRIGHTNESS: 'Brillo',
    // Agrega aquí todos los atributos que necesites traducir
};

export const operatorTranslations = {
    GREATER_THAN: 'Mayor que',
    LESS_THAN: 'Menor que',
    EQUALS: 'Igual a',
    // Agrega aquí todos los operadores
};

export const actionTranslations = {
    TURN_ON: 'Encender',
    TURN_OFF: 'Apagar',
    SET_BRIGHTNESS: 'Ajustar Brillo',
    // Agrega aquí todas las acciones
};

// Función genérica para obtener la traducción o el valor original
export const getTranslation = (value, translationMap) => {
    return translationMap[value] || value;
};