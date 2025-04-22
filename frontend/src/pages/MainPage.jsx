import { useState } from 'react'

function MainPage() {
    const [rooms, setRooms] = useState([])

    const addRoom = () => {
        const roomName = prompt('Nombre de la habitación:')
        if (roomName) {
            setRooms([...rooms, { name: roomName, devices: [] }])
        }
    }

    return (
        <div>
            <h2>Mi Hogar</h2>
            <button onClick={addRoom}>Agregar habitación</button>
            <div>
                {rooms.map((room, index) => (
                    <div key={index}>
                        <h3>{room.name}</h3>
                        <p>{room.devices.length} dispositivos</p>
                    </div>//
                ))}
            </div>
        </div>
    )
}

export default MainPage