import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './header.module.css'; // Importar la hoja de estilos
import logo from "../assets/NeoHub.png"
const Header = ({ title }) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const toggleMenu = () => setIsMenuOpen(!isMenuOpen);

    return (
        <header className="header">
            {/* Menú de hamburguesa */}
            <button onClick={toggleMenu} className="hamburger-button">
                <span className="material-icons">menu</span>
            </button>

            {/* Texto dinámico */}
            <h1 className="header-title">{title}</h1>

            {/* Logo */}
            <img src={logo} alt="App Logo" className="header-logo" />

            {/* Menú desplegable */}
            {isMenuOpen && (
                <div className="menu">
                    <ul className="menu-list">
                        <li className="menu-item">
                            <Link to="/home" className="menu-link">Mi Hogar</Link>
                        </li>
                        <li className="menu-item">
                            <Link to="/rooms" className="menu-link">Habitaciones</Link>
                        </li>
                        <li className="menu-item">
                            <Link to="/profile" className="menu-link">Mi Perfil</Link>
                        </li>
                        <li className="menu-item">
                            <button className="menu-link">Cerrar Sesión</button>
                        </li>
                    </ul>
                </div>
            )}
        </header>
    );
};

export default Header;
