import { useState, useEffect } from 'react';
import './loginPopup.css';

const RuleFormPopup = ({ onClose, onCreate, device }) => {
    const [name, setName] = useState('');

    const operadores = ['>', '<', '==', '!=', '>=', '<='];
    const accionesDisponibles = ['Encender', 'Apagar', 'Notificar', 'Ajustar'];

    const [cond, setCond] = useState({
        deviceId: device?.id || '',
        attribute: '',
        operator: '',
        value: ''
    });

    const [act, setAct] = useState({
        deviceId: device?.id || '',
        actionType: '',
        parameters: ''
    });

    useEffect(() => {
        if (device) {
            const excludeKeys = ['id', 'name', 'type', 'topic', 'roomId', 'status'];
            const posiblesAtributos = Object.keys(device).filter(
                key => !excludeKeys.includes(key) && device[key] !== null && typeof device[key] !== 'object'
            );

            const atributoDetectado = posiblesAtributos.length > 0 ? posiblesAtributos[0] : '';

            setCond(prev => ({
                ...prev,
                deviceId: device.id,
                attribute: atributoDetectado
            }));

            setAct(prev => ({
                ...prev,
                deviceId: device.id,
                actionType: prev.actionType,
                parameters: prev.parameters
            }));
        }
    }, [device]);

    const handleSubmit = () => {
        const newRule = {
            name,
            conditions: [cond],
            actions: [act]
        };
        console.log(JSON.stringify(newRule))
        onCreate(newRule);
        onClose();
    };

    return (
        <div className="modal-backdrop">
            <div className="modal">
                <h3>Crear nueva regla para <strong>{device?.name}</strong></h3>

                <input
                    placeholder="Nombre de la regla"
                    value={name}
                    onChange={e => setName(e.target.value)}
                />

                <h4>Condición</h4>
                <div>
                    <label>Atributo</label>
                    <input value={cond.attribute} disabled />
                </div>
                <div>
                    <label>Operador:</label>
                    <select value={cond.operator} onChange={e => setCond({ ...cond, operator: e.target.value })}>
                        <option value="">Seleccionar</option>
                        {operadores.map(op => (
                            <option key={op} value={op}>{op}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Valor:</label>
                    <input type="text" value={cond.value} onChange={e => setCond({ ...cond, value: e.target.value })} />
                </div>

                <h4>Acción</h4>
                <div>
                    <label>Tipo de acción:</label>
                    <select value={act.actionType} onChange={e => setAct({ ...act, actionType: e.target.value })}>
                        <option value="">Seleccionar</option>
                        {accionesDisponibles.map(act => (
                            <option key={act} value={act}>{act}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Parámetros:</label>
                    <input type="text" value={act.parameters} onChange={e => setAct({ ...act, parameters: e.target.value })} />
                </div>

                <div className="modal-actions">
                    <button onClick={handleSubmit}>Crear</button>
                    <button onClick={onClose}>Cancelar</button>
                </div>
            </div>
        </div>
    );
};

export default RuleFormPopup;
