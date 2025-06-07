import { useEffect, useState } from 'react';
import {useNavigate, useParams} from "react-router-dom";
import { getAllRules, getRuleForDevice, createRule, deleteRule } from '../api/ruleService.js';
import {getConfiguredDeviceCount, getDevice} from '../api/deviceService.js';
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import './styles/rules.css';
import TextButton from "../components/TextButton.jsx";
import RoundButton from "../components/RoundButton.jsx";
import RuleFormPopup from "../components/RuleFormPopup.jsx";
import {useTitle} from "../contexts/TitleContext.jsx";
import '../components/grid/styles/gridView.css'

import {
    attributeTranslations,
    operatorTranslations,
    actionTranslations,
    getTranslation,
    openingSensorBooleanValueTranslations,
    outletBooleanValueTranslations,
} from '../api/ruleMapping.js'; // Asegúrate que la ruta sea correcta

const RulesManager = ({ isDeviceContext = false }) => {
    const { id } = useParams(); // Si viene desde ruta con :id
    const token = localStorage.getItem('token');
    const navigate = useNavigate();

    const [deviceCount, setDeviceCount] = useState(0);
    const [rules, setRules] = useState([]);
    const [device, setDevice] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showPopup, setShowPopup] = useState(false);
    const [editMode, setEditMode] = useState(false);
    const [showDeletePopup, setShowDeletePopup] = useState(false);
    const [ruleToDelete, setRuleToDelete] = useState(null);
    const [expandedRuleId, setExpandedRuleId] = useState(null);

    const {setHeaderTitle} = useTitle();



    const fetchRules = async () => {
        setLoading(true);
        try {
            if (isDeviceContext && id) {
                const [rulesData, deviceData] = await Promise.all([
                    getRuleForDevice(id, token),
                    getDevice(id, token)
                ]);
                setRules(rulesData);
                setDevice(deviceData);
            } else {
                const rulesData = await getAllRules(token);
                setRules(rulesData);
            }
        } catch (err) {
            setError("No se pudieron cargar las reglas.", err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const fetchDeviceCount = async () => {
            try {
                const count = await getConfiguredDeviceCount(token);
                setDeviceCount(count);
            } catch (error) {
                console.error('Error al cargar el conteo de dispositivos configurados:', error);
            }
        };
        fetchDeviceCount();
    }, []);


    useEffect(() => {
        if (token) fetchRules();
    }, [id, token, isDeviceContext]);

    //Efecto para el title.
    useEffect(() => {
        const baseTitle = isDeviceContext ? (device ? `Reglas de ${device.name}` : 'Reglas del dispositivo') : 'Reglas'; //
        if (editMode) { //
            setHeaderTitle("Editar reglas"); //
        } else {
            setHeaderTitle(baseTitle);
        }
    }, [editMode, isDeviceContext, device, setHeaderTitle]);

    const handleCreateRule = async (newRule) => {
        try {
            await createRule(newRule, token);
            fetchRules();
        } catch (err) {
            console.error('Error al crear regla:', err);
        }
    };

    const handleConfirmDelete = async () => {
        if (!ruleToDelete) return;
        try {
            await deleteRule(ruleToDelete.id, token);
            fetchRules();
            setShowDeletePopup(false);
            setRuleToDelete(null);
        } catch (err) {
            console.error("Error al eliminar regla", err);
        }
    };

    const toggleRuleExpand = (ruleId) => {
        setExpandedRuleId(prevId => (prevId === ruleId ? null : ruleId));
    };

    // Helper para renderizar el valor de la condición de forma amigable
    const renderConditionValue = (attribute, value) => {
        attribute = attribute.toUpperCase();
        // Primero, intenta traducir valores booleanos si el atributo es IS_ON
        if (attribute === 'IS_ON') { // Asume que 'IS_ON' es el identificador crudo
            return getTranslation(value.toString(), outletBooleanValueTranslations, value); // Convierte a string por si acaso es booleano
        }
        if (attribute === 'IS_OPEN') {
            return getTranslation(value.toString(), openingSensorBooleanValueTranslations, value); // Lo mismo para sensor puerta
        }
        // Podría añadir más lógica aquí para otros atributos si es necesario
        if (attribute === 'TEMPERATURE') return `${value}°C`;
        if (attribute === 'BRIGHTNESS') return `${value}%`;
        return value; // Devuelve el valor como está si no hay traducción especial
    };

    if (loading) return <div className="loading-message">Cargando reglas...</div>;
    // No mostrar mensaje de error y de "no hay reglas" al mismo tiempo.
    if (error && rules.length === 0) return <p className="error-message">{error}</p>;

    const emptyMessage = isDeviceContext ? 'No hay reglas asociadas a este dispositivo.' : 'No hay reglas.';

    return (
        <div className="main-container">
            <BackOrCloseButton />

            {editMode ? (
                <>
                    <div className="room-grid">
                        {rules.map((rule) => (
                            <div
                                key={rule.id}
                                className="room-editable-container"
                                onClick={() => {
                                    setRuleToDelete(rule);
                                    setShowDeletePopup(true);
                                }}
                            >
                                <div className={`room-button ${editMode ? ' edit-mode' : ''}`}>
                                    <span>{rule.name}</span>
                                    <div className="delete-icon-full">🗑️</div>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            ) : (
                <>
                    {rules.length === 0 ?
                    (
                        <>
                            <p>{emptyMessage}</p>
                            {deviceCount>=2 ?
                                (<div className="add-device-icon">
                                    <TextButton handleClick={()=> setShowPopup(true)} text="Agregar Regla"/>
                                </div>) :
                                (<div>
                                    <p>No tienes suficientes dispositivos configurados para agregar reglas.</p>
                                    <TextButton handleClick={() => navigate('/home')} text="A mis habitaciones..."/>
                                </div>)
                            }
                        </>
                    ) : (
                        <>
                            <div className="edit-container">
                                <RoundButton type="edit" onClick={() => setEditMode(true)} />
                            </div>
                            <div className="rule-grid">
                                {rules.map((rule) => (
                                    <div
                                        key={rule.id}
                                        className={`rule-button ${expandedRuleId === rule.id ? 'expanded' : ''}`}
                                        onClick={() => toggleRuleExpand(rule.id)}
                                    >
                                        <h3>{rule.name}</h3>
                                        {expandedRuleId === rule.id && (
                                            <div className="rule-details">
                                                <p><strong>Condiciones:</strong></p>
                                                <ul>
                                                    {rule.conditions?.map((cond, index) => (
                                                        <li key={index}>
                                                            {cond.deviceName}{' '}
                                                            {getTranslation(cond.attribute, attributeTranslations)}{' '}
                                                            {getTranslation(cond.operator, operatorTranslations)}{' '}
                                                            {renderConditionValue(cond.attribute, cond.value)}
                                                        </li>
                                                    ))}
                                                </ul>
                                                <p><strong>Acciones:</strong></p>
                                                <ul>
                                                    {rule.actions?.map((act, index) => (
                                                        <li key={index}>
                                                            {getTranslation(act.actionType, actionTranslations)}{' '}
                                                            {act.parameters} {/* Los parámetros usualmente no se traducen o dependen del tipo de acción */}
                                                            {act.deviceName}{' '}
                                                        </li>
                                                    ))}
                                                </ul>
                                            </div>
                                        )}
                                    </div>
                                ))}
                                {deviceCount>2 ?
                                    (<div className="add-device-icon">
                                        <TextButton handleClick={()=> setShowPopup(true)} text="Agregar..."/>
                                    </div>) :
                                    (<div>
                                        <p>Necesitas al menos 2 dispositivos configurados para agregar reglas.</p>
                                        <TextButton handleClick={() => navigate('/home')} text="A mis habitaciones..."/>
                                    </div>)
                                }

                            </div>
                        </>
                    )}
                </>
            )}

            {showPopup && (
                <RuleFormPopup
                    onClose={() => setShowPopup(false)}
                    onCreate={handleCreateRule}
                    device={isDeviceContext ? device : null}
                />
            )}

            {showDeletePopup && (
                <div className="modal-backdrop" onClick={() => setShowDeletePopup(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <p>¿Estás seguro que querés eliminar la regla "{ruleToDelete?.name}"?</p>
                        <div className="modal-actions">
                            <TextButton handleClick={handleConfirmDelete} text="Confirmar"/>
                            <TextButton handleClick={ () => setShowDeletePopup(false) } text="Cancelar"/>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RulesManager;
