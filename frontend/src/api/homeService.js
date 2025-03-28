export const controlLight = async (isOn) => {
    try {
        const response = await fetch('/api/mqtt/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                topic: 'luces',
                message: isOn ? 'encender' : 'apagar'
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