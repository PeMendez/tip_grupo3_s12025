import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import styles from './styles/header.module.css'; // Importar la hoja de estilos
import logo from "../assets/Logo1.svg"
import { FiMenu } from "react-icons/fi";
import { useNavigate } from 'react-router-dom'
import TextButton from "./TextButton.jsx";
import usePushNotifications from "../hooks/usePushNotifications.js";

const Header = ({ title }) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const toggleMenu = () => setIsMenuOpen(!isMenuOpen);
    const navigate = useNavigate()

    const {unsubscribe} = usePushNotifications()

    const logout = () => {
        localStorage.removeItem('token');
        setIsMenuOpen(false)
        navigate("/");
        unsubscribe()
    };

    return (
        <header className={styles.header}>
            {/* Menú de hamburguesa */}
            <button onClick={toggleMenu} className={styles.hamburgerButton}>
                <FiMenu size={24} />
            </button>

            {/* Texto dinámico */}
            <h1 className={styles.headerTitle}>{title}</h1>

            {/* Logo */}
            <img src={logo} alt="App Logo" className= {styles.headerLogo}/> {/* Sobre esta imagen no toma los estilos. */}


            {/* Fondo para cerrar el menú */}
            {isMenuOpen && <div className={styles.menuOverlay} onClick={toggleMenu}></div>}

            {/* Menú desplegable */}
            {isMenuOpen && (
                <div className={styles.menu}>
                    <ul className={styles.menuList} onClick={()=>setIsMenuOpen(false)}>
                        <li className={styles.menuItem}>
                            <Link to="/home" className={styles.menuLink}>Mi Hogar</Link>
                        </li>
                        <li className={styles.menuItem}>
                            <Link to="/rules" className={styles.menuLink}>Reglas</Link>
                        </li>
                        <li className={styles.menuItem}>
                            <TextButton handleClick={logout} text="Cerrar Sesión" />
                        </li>
                    </ul>
                </div>
            )}
        </header>
    );
};

export default Header;
