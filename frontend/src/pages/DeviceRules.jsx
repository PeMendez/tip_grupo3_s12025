import { useEffect, useState } from 'react';
import {getRuleForDevice, createRule, deleteRule} from '../api/ruleService.js';
import {useParams} from "react-router-dom";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import {getDevice} from "../api/deviceService.js";
import {FiEdit, FiPlus} from "react-icons/fi";
import './rules.css'
import RuleFormPopupBis from "../components/RuleComponentBis.jsx";

const DeviceRules =  () => {

    const { id } = useParams();
    const token = localStorage.getItem('token');
    const [rules, setRules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showPopup, setShowPopup] = useState(false);
    const [device, setDevice] = useState(null)
    const [editMode, setEditMode] = useState(false);
    const [showDeletePopup, setShowDeletePopup] = useState(false);
    const [ruleToDelete, setRuleToDelete] = useState(null);
    const [expandedRuleId, setExpandedRuleId] = useState(null);


    const fetchRules = async () => {
        try {
            const data = await getRuleForDevice(id, token);
            const device = await getDevice(id, token);
            console.log(data)
            setRules(data);
            setDevice(device)
        } catch (err) {
            setError(err + ' No se pudieron cargar las reglas.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (id && token) {
            fetchRules();
        }
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
            console.error("Error al eliminar dispositivo", err);
        }
    };

    const toggleRuleExpand = (ruleId) => {
        setExpandedRuleId(prevId => (prevId === ruleId ? null : ruleId));
    };


    if (loading) return <p>Cargando reglas...</p>;
    if (error) return <p>{error}</p>;


    if (editMode) {
        return (
            <div className="main-container">
                <div className="header-wrapper">
                    <div className="header">
                        <BackOrCloseButton type="arrow" onClick={() => setEditMode(false)} />
                        <h2>Editar Reglas</h2>
                    </div>
                </div>
                <div className="room-grid">
                    {rules.map((rule) =>  (
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

                {showDeletePopup && (
                    <div className="modal-backdrop" onClick={() => setShowDeletePopup(false)}>
                        <div className="modal" onClick={(e) => e.stopPropagation()}>
                            <p>¿Eliminar "{ruleToDelete?.name}"?</p>
                            <div className="modal-actions">
                                <button onClick={handleConfirmDelete}>Confirmar</button>
                                <button onClick={() => setShowDeletePopup(false)}>Cancelar</button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        );

    }

    return (
        <div className="main-container">
            <div className="header-wrapper">
                <div className="header">
                    <BackOrCloseButton />
                    <h2>Reglas del dispositivo</h2>
                </div>
            </div>
            {rules.length === 0 ? (
                <>
                    <p>No hay reglas asociadas a este dispositivo.</p>
                    <button onClick={() => setShowPopup(true)}>Agregar Regla</button>
                </>
            ) : (
                <>
                    <div className="edit-container">
                        <div className="edit-button">
                            <button onClick={() => setEditMode(true)}>
                                <FiEdit size={24}/>
                            </button>
                        </div>
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
                                                    {cond.attribute} {cond.operator} {cond.value}
                                                </li>
                                            ))}
                                        </ul>
                                        <p><strong>Acciones:</strong></p>
                                        <ul>
                                            {rule.actions?.map((act, index) => (
                                                <li key={index}>
                                                    {act.actionType} ({act.parameters})
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </div>
                        ))}
                        <div className="add-device-icon">
                            <button onClick={() => setShowPopup(true)}>
                                <FiPlus size={24} className="icon"/>
                            </button>
                        </div>
                    </div>
                </>
            )}
            {showPopup && (
                <RuleFormPopupBis
                    onClose={() => setShowPopup(false)}
                    onCreate={handleCreateRule}
                    device={device}
                />
            )}
        </div>
            );
};

export default DeviceRules;