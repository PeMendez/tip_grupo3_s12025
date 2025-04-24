import { useState } from 'react'
import cocinaImg from '../assets/cocina.jpg'
import dormitorioImg from '../assets/dormitorio.jpg'
import livingImg from '../assets/living.jpg'
import garajeImg from '../assets/garaje.jpg'
import lavaderoImg from '../assets/lavadero.jpg'
import banoImg from '../assets/Baño.jpg'
import salaImg from '../assets/salaDeJuegos.jpg'
import { FiEdit } from 'react-icons/fi';
import './mainPage.css'


function MainPage() {
    const [rooms, setRooms] = useState([])

    const addRoom = () => {
        const roomName = prompt('Nombre de la habitación:')
        if (roomName) {
            setRooms([...rooms, { name: roomName, devices: [] }])
        }
    }

    const roomTypes = [
        { name: 'Cocina', img: cocinaImg },
        { name: 'Dormitorio', img: dormitorioImg },
        { name: 'Living', img: livingImg },
        { name: 'Garaje', img: garajeImg },
        { name: 'Lavadero', img: lavaderoImg },
        { name: 'Baño', img: banoImg },
        { name: 'Sala de juegos', img: salaImg },
    ]


    const handleRoomTypeClick = (roomType) => {
        const roomName = prompt(`Agregar ${roomType.name} a tu hogar`)
        if (roomName) {
            setRooms([...rooms, { name: roomName, devices: [] }])
        }
    }

    return (
        <div className="main-container">
            <div className="header">
                <h2>Mi Hogar</h2>
            </div>
            {rooms.length === 0 ? (
                <div className="botones">

                    <button onClick={addRoom}>Agregar habitación</button>
                    <button onClick={() => alert("Editar hogar")}>
                        <FiEdit/> Editar hogar
                    </button>
                <div/>
                    <div className="room-grid">
                        {roomTypes.map((room, index) => (
                            <button key={index} onClick={() => handleRoomTypeClick(room)} className="room-button">
                                <img src={room.img} alt={room.name} />
                                <span>{room.name}</span>
                            </button>
                        ))}
                    </div>
                </div>
            ) : (
                <div>
                    {rooms.map((room, index) => (
                        <div key={index}>
                            <h3>{room.name}</h3>
                            <p>{room.devices.length} dispositivos</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}

export default MainPage
