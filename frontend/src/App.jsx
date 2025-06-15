import './App.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import MainPage from './pages/MainPage.jsx'
import RoomDetail from "./pages/RoomDetail.jsx";
import RulesManager from "./pages/RulesManager.jsx";
import Profile from "./pages/Profile.jsx";
import Header from "./components/Header.jsx";
import {AuthProvider} from "./contexts/AuthContext.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import {TitleProvider, useTitle} from "./contexts/TitleContext.jsx";
import DeviceConfig from "./pages/DeviceConfig.jsx";
import NoHomePage from "./pages/NoHomePage.jsx";


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
                                    <MainPage/>
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/room/:id"
                            element={
                                <ProtectedRoute>
                                    <RoomDetail/>
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
                        <Route
                            path="/profile"
                            element={
                                <ProtectedRoute>
                                    <Profile/>
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/device/:id"
                            element={
                                <ProtectedRoute>
                                    <DeviceConfig/>
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/noHome"
                            element={
                                <ProtectedRoute>
                                    <NoHomePage/>
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