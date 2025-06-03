import { useEffect, useState } from 'react';
import {useNavigate, useParams} from "react-router-dom";
import { getAllRules, getRuleForDevice, createRule, deleteRule } from '../api/ruleService.js';
import {getConfiguredDeviceCount, getDevice} from '../api/deviceService.js';
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import './styles/rules.css';
import RuleFormPopupBis from "../components/RuleComponentBis.jsx";
import TextButton from "../components/TextButton.jsx";
import RoundButton from "../components/RoundButton.jsx";
import RuleFormPopup from "../components/RuleFormPopup.jsx";

const RulesManager = ({setHeaderTitle, isDeviceContext = false }) => {
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

    const fetchRules = async () => {
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
    }, [id, token]);

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

    if (loading) return <p>Cargando reglas...</p>;
    if (error) return <p>{error}</p>;

    const title = isDeviceContext ? `Reglas del dispositivo` : 'Reglas';
    const emptyMessage = isDeviceContext ? 'No hay reglas asociadas a este dispositivo.' : 'No hay reglas.';

    if(editMode){
        setHeaderTitle("Editar reglas")
    } else setHeaderTitle(title)

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
                                <div className="room-button">
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
                                                            {cond.deviceName} {cond.attribute} {cond.operator} {cond.value}
                                                        </li>
                                                    ))}
                                                </ul>
                                                <p><strong>Acciones:</strong></p>
                                                <ul>
                                                    {rule.actions?.map((act, index) => (
                                                        <li key={index}>
                                                            {act.deviceName} {act.actionType} {act.parameters}
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
                                        <p>No tienes suficientes dispositivos configurados para agregar reglas.</p>
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
                        <p>¿Eliminar "{ruleToDelete?.name}"?</p>
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
