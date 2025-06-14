import React, { createContext, useContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
    const token = localStorage.getItem("token");
    const [isAuthenticated, setIsAuthenticated] = useState(!!token);
    const [user, setUser] = useState(null);
    const [role, setRole] = useState(null);


    useEffect(() => {
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setUser(decoded);
            } catch (error) {
                console.error("Token inválido", error);
                setIsAuthenticated(false);
                setUser(null);
            }
        }
    }, [token]);

    return (
        <AuthContext.Provider value={{ isAuthenticated, setIsAuthenticated, user, role, setRole }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
