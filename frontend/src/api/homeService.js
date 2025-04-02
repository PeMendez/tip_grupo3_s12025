export const controlLight = async (isOn) => {

    const apiUrl = "http://localhost:8080"
    try {
        const response = await fetch(apiUrl+'/api/mqtt/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                topic: 'LEDctrl',
                message: isOn ? 'toggle' : 'toggle'
            })
        });

        if (!response.ok) {
            throw new Error('Error en la respuesta del servidor');
        }
        return await response.text();
    } catch (error) {
        console.error('Error al controlar luces:', error);
        throw error;
    }
};