import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import cocinaImg from '../assets/cocina.jpg'
import dormitorioImg from '../assets/dormitorio.jpg'
import livingImg from '../assets/living.jpg'
import garajeImg from '../assets/garaje.jpg'
import lavaderoImg from '../assets/lavadero.jpg'
import banoImg from '../assets/Baño.jpg'
import salaImg from '../assets/salaDeJuegos.jpg'
import { FiEdit, FiPlus } from 'react-icons/fi'
import './mainPage.css'

function MainPage() {
    const [rooms, setRooms] = useState([])
    const [editMode, setEditMode] = useState(false)
    const navigate = useNavigate()

    const roomTypes = [
        { name: 'Cocina', img: cocinaImg },
        { name: 'Dormitorio', img: dormitorioImg },
        { name: 'Living', img: livingImg },
        { name: 'Garaje', img: garajeImg },
        { name: 'Lavadero', img: lavaderoImg },
        { name: 'Baño', img: banoImg },
        { name: 'Sala de juegos', img: salaImg },
    ]

    return (
        <div className="main-container">
            <div className="header">
                <h2>Mi Hogar</h2>
            </div>

            {!editMode && rooms.length > 0 && (
                <>
                    <div className="room-grid">
                        {rooms.map((room, index) => (
                            <div key={index} className="room-button">
                                <img src={room.img} alt={room.name} />
                                <span>{room.name}</span>
                            </div>
                        ))}
                    </div>
                    <div className="edit-button">
                        <button onClick={() => setEditMode(true)}>
                            <FiEdit /> Editar hogar
                        </button>
                    </div>
                </>
            )}

            {!editMode && rooms.length === 0 && (
                <div className="no-rooms">
                    <p>Aún no tenés habitaciones...</p>
                    <button onClick={() => setEditMode(true)}>
                        <FiEdit /> Editar hogar
                    </button>
                </div>
            )}

            {editMode && (
                <div className="room-grid">
                    {roomTypes.map((room, index) => (
                        <button key={index} onClick={() => navigate(`/room/${room.name}`)} className="room-button">
                            <img src={room.img} alt={room.name} />
                            <span>{room.name}</span>
                        </button>
                    ))}
                    <div className="add-room-icon">
                        <button onClick={() => alert("Agregar habitación personalizada")} className="room-button">
                            <FiPlus size={24} className="icon" />
                        </button>
                    </div>
                </div>
            )}
        </div>
    )
}

export default MainPage
