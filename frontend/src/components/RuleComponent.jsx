import { useState, useEffect } from 'react';
import './loginPopup.css';
import {
    getActions,
    getAttributes,
    getDevicesForAction,
    getDevicestypeForAction,
    getOperators
} from '../api/ruleService.js'

const RuleFormPopup = ({ onClose, onCreate, device }) => {
    const [name, setName] = useState('');
    const token = localStorage.getItem('token');
    const [atributo, setAtributo] = useState('');
    const [error, setError] = useState('');
    const [operadores, setOperadores] = useState([]);
    const [acciones, setAcciones] = useState([]);
    const [formErrors, setFormErrors] = useState({});
    const [param, setParam] = useState([]);


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
        const fetchInitialData = async () => {
            try {
                const data = await getAttributes(device.type, token);
                setAtributo(data);
                setCond(prev => ({ ...prev, attribute: data.toString() }));

                const op = await getOperators(data.toString(), token);
                setOperadores(op || []);

                const acc = await getActions(device.type, token);
                setAcciones(acc);
            } catch (err) {
                setError(err + ' No se pudieron obtener los atributos.');
            }
        };
        fetchInitialData();
    }, []);

    useEffect(() => {
        const fetchParams = async () => {
            if (!act.actionType) {
                setParam([]);
                return;
            }
            try {
                const types = await getDevicestypeForAction(act.actionType, token);
                console.log(types)
                const result = await getDevicesForAction(types, token);
                setParam(result || []);
            } catch (err) {
                setError('Error al obtener parámetros para la acción', err);
            }
        };
        fetchParams();
    }, [act.actionType]);



    const handleSubmit = () => {
        const errors = {};

        if (!name.trim()) errors.name = 'El nombre es obligatorio';
        if (!cond.operator) errors.operator = 'Seleccioná un operador';
        if (!cond.value.trim()) errors.value = 'El valor no puede estar vacío';
        if (!act.actionType) errors.actionType = 'Seleccioná una acción';
        if (!act.parameters.trim()) errors.parameters = 'Ingresá parámetros';

        if (Object.keys(errors).length > 0) {
            setFormErrors(errors);
            return;
        }

        const newRule = {
            name,
            conditions: [cond],
            actions: [act]
        };

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
                {formErrors.name && <span className="error">{formErrors.name}</span>}

                <h4>Condición</h4>
                <div>
                    <label>Atributo</label>
                    <input value={atributo.toString()} disabled/>
                </div>
                <div>
                    <label>Operador:</label>
                    <select value={cond.operator} onChange={e => setCond({...cond, operator: e.target.value})}>
                        <option value="">Seleccionar</option>
                        {operadores.map(op => (
                            <option key={op} value={op}>{op}</option>
                        ))}
                    </select>
                    {formErrors.operator && <span className="error">{formErrors.operator}</span>}
                </div>
                <div>
                    <label>Valor:</label>
                    <input type="text" value={cond.value} onChange={e => setCond({...cond, value: e.target.value})}/>
                    {formErrors.value && <span className="error">{formErrors.value}</span>}
                </div>

                <h4>Acción</h4>
                <div>
                    <label>Tipo de acción:</label>
                    <select value={act.actionType} onChange={e => setAct({...act, actionType: e.target.value})}>
                        <option value="">Seleccionar</option>
                        {acciones.map(act => (
                            <option key={act} value={act}>{act}</option>
                        ))}
                    </select>
                    {formErrors.actionType && <span className="error">{formErrors.actionType}</span>}
                </div>
                {/*<div>
                    <label>Parámetros:</label>
                    <input type="text" value={act.parameters}
                           onChange={e => setAct({...act, parameters: e.target.value})}/>
                    {formErrors.parameters && <span className="error">{formErrors.parameters}</span>}
                </div>*/}
                <div>
                    <label>Parámetros:</label>
                    <select value={act.parameters}
                            onChange={e => setAct({...act, parameters: e.target.value})}
                            disabled={!act.actionType}>
                        <option value="">Seleccionar</option>
                        {param.map(par => (
                            <option key={par.id} value={par.name}>{par.name}</option>
                        ))}
                    </select>
                    {formErrors.parameters && <span className="error">{formErrors.parameters}</span>}
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
