import { useEffect, useState } from 'react';
import { getRuleForDevice, createRule } from '../api/ruleService.js';
import {useParams} from "react-router-dom";
import BackOrCloseButton from "../components/BackOrCloseButton.jsx";
import RuleFormPopup from "../components/RuleComponent.jsx";
import {getDevice} from "../api/deviceService.js";

const DeviceRules =  () => {

    const { id } = useParams();
    const token = localStorage.getItem('token');
    const [rules, setRules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showPopup, setShowPopup] = useState(false);
    const [device, setDevice] = useState(null)

    const fetchRules = async () => {
        try {
            const data = await getRuleForDevice(id, token);
            const device = await getDevice(id, token);
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

    if (loading) return <p>Cargando reglas...</p>;
    if (error) return <p>{error}</p>;

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
                    {rules.map((rule) => (
                        <div key={rule.id} className="rule-card">
                            <h3>{rule.name}</h3>
                            <p><strong>Condiciones:</strong></p>
                            <ul>
                                {rule.conditions.map((cond, index) => (
                                    <li key={index}>
                                        {cond.attribute} {cond.operator} {cond.value}
                                    </li>
                                ))}
                            </ul>
                            <p><strong>Acciones:</strong></p>
                            <ul>
                                {rule.actions.map((act, index) => (
                                    <li key={index}>
                                        {act.actionType} ({act.parameters})
                                    </li>
                                ))}
                            </ul>
                        </div>
                    ))}
                </>
            )}
            {showPopup && (
                <RuleFormPopup
                    onClose={() => setShowPopup(false)}
                    onCreate={handleCreateRule}
                    device={device}
                />
            )}
        </div>
    );
};


export default DeviceRules;