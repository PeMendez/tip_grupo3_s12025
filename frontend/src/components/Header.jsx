import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import styles from './styles/header.module.css';
import logo from "../assets/Logo1.svg"
import { FiMenu } from "react-icons/fi";
import { useNavigate } from 'react-router-dom'
import TextButton from "./TextButton.jsx";
import usePushNotifications from "../hooks/usePushNotifications.js";
import {useAuth} from "../contexts/AuthContext.jsx";
import {logout} from "../api/authService.js"
import {useTitle} from "../contexts/TitleContext.jsx";

const Header = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const toggleMenu = () => setIsMenuOpen(!isMenuOpen);
    const navigate = useNavigate();
    const {setIsAuthenticated, role} = useAuth();
    const {unsubscribe} = usePushNotifications();

    const {headerTitle} = useTitle();

    const handleLogout = () => {
        logout();
        setIsAuthenticated(false);
        setIsMenuOpen(false);
        navigate("/");
        unsubscribe();
    };

    return (
        <header className={styles.header}>
            {/* Menú de hamburguesa */}
            <button onClick={toggleMenu} className={styles.hamburgerButton}>
                <FiMenu size={24} />
            </button>

            {/* Texto dinámico */}
            <h1 className={styles.headerTitle}>{headerTitle}</h1>

            {/* Logo */}
            <img src={logo} alt="App Logo" className={styles.headerLogo}/>

            {/* Fondo para cerrar el menú */}
            {isMenuOpen && <div className={styles.menuOverlay} onClick={toggleMenu}></div>}

            {/* Menú desplegable */}
            {isMenuOpen && (
                <div className={styles.menu}>
                    <ul className={styles.menuList} onClick={() => setIsMenuOpen(false)}>
                        {role ? (
                            <>
                                <li className={styles.menuItem}>
                                    <Link to="/home" className={styles.menuLink}>Mi Hogar</Link>
                                </li>
                                <li className={styles.menuItem}>
                                    <Link to="/profile" className={styles.menuLink}>Mi Perfil</Link>
                                </li>
                                <li className={styles.menuItem}>
                                    <Link to="/rules" className={styles.menuLink}>Reglas</Link>
                                </li>
                            </>
                        ) : null}
                        <li className={styles.menuItem}>
                            <TextButton handleClick={handleLogout} text="Cerrar Sesión"/>
                        </li>
                    </ul>
                </div>
            )}
        </header>
    );
};

export default Header;