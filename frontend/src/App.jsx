import './App.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import MainPage2 from './pages/MainPage2.jsx'
import RoomDetail2 from "./pages/RoomDetail2.jsx";
import RulesManager from "./pages/RulesManager.jsx";
import Header from "./components/Header.jsx";
import {AuthProvider} from "./contexts/AuthContext.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import {TitleProvider, useTitle} from "./contexts/TitleContext.jsx";


const AppContent = () => {
    const {headerTitle} = useTitle();

    return (
        <div className="app-container">
            <AuthProvider>
                <Router>
                    <Header title={headerTitle} />
                    <Routes>
                        {/* Página de login siempre accesible */}
                        <Route path="/" element={<LoginPage/>} />

                        {/* Rutas protegidas */}
                        <Route
                            path="/home"
                            element={
                                <ProtectedRoute>
                                    <MainPage2/>
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/room/:id"
                            element={
                                <ProtectedRoute>
                                    <RoomDetail2/>
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/rules"
                            element={
                                <ProtectedRoute>
                                    <RulesManager/>
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/rule/:id"
                            element={
                                <ProtectedRoute>
                                    <RulesManager isDeviceContext={true}/>
                                </ProtectedRoute>
                            }
                        />
                    </Routes>
                </Router>
            </AuthProvider>
        </div>
    );
};

const App = () => {
    return(
        <TitleProvider>
            <AppContent/>
        </TitleProvider>
    );
};

export default App