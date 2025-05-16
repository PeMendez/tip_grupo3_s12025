import { useState, useEffect } from 'react';
import './loginPopup.css';
import {getActions, getAttributes, getOperators} from '../api/ruleService.js'

const RuleFormPopup = ({ onClose, onCreate, device }) => {
    const [name, setName] = useState('');
    const token = localStorage.getItem('token');
    const [atributo, setAtributo] = useState('');
    const [error, setError] = useState('');
    const [operadores, setOperadores] = useState([]);
    const [acciones, setAcciones] = useState([]);

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

    const fetchAttributes = async () => {
        try {
            const data = await getAttributes(device.type, token);
            setAtributo(data);
            setCond(prevState => ({...prevState,attribute: data.toString()}));
            const op = await getOperators(data.toString(), token);
            setOperadores(op || [])
            const acc = await getActions(device.type, token);
            setAcciones(acc);
        } catch (err) {
            setError(err + ' No se pudieron obtener los atributos.');
        }
    };

    useEffect(() => {
        fetchAttributes()
    }, []);



    const handleSubmit = () => {
        console.log(cond, act)
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
                    <input value={atributo.toString()} disabled />
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
                        {acciones.map(act => (
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
