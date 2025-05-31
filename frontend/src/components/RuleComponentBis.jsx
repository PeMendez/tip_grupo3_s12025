import { useState, useEffect } from 'react';
import {
    getActions,
    getAttributes,
    getOperators
} from '../api/ruleService.js'
import { getAllDevices } from '../api/deviceService.js'; // Nuevo endpoint para obtener todos los dispositivos
import './styles/rule.css'
import TextButton from "./TextButton.jsx";

const RuleFormPopupBis = ({ onClose, onCreate, device }) => {
    const [name, setName] = useState('');
    const [devices, setDevices] = useState([]);
    const [formErrors, setFormErrors] = useState({});

    const [conditionDevice, setConditionDevice] = useState(device || null);
    const [actionDevice, setActionDevice] = useState(null);

    const [atributos, setAtributos] = useState([]);
    const [operadores, setOperadores] = useState([]);
    const [acciones, setAcciones] = useState([]);

    const [cond, setCond] = useState({
        deviceId: '',
        attribute: '',
        operator: '',
        value: ''
    });

    const [act, setAct] = useState({
        deviceId: '',
        actionType: '',
        parameters: ''
    });

    useEffect(() => {
        if (device) {
            setConditionDevice(device);
        }
    }, [device]);

    const requiresParameters = (actionType) => {
        // Define qué acciones requieren parámetros
        const actionsWithParams = ['SET_BRIGHTNESS']; // Listar las que lo requieran, por ahora viaja asi
        return actionsWithParams.includes(actionType);
    };


    useEffect(() => {
        const fetchDevices = async () => {
            try {
                const token = localStorage.getItem('token');
                const devicesData = await getAllDevices(token); // Llama al nuevo endpoint
                setDevices(devicesData);
            } catch (err) {
                console.log('No se pudieron obtener los dispositivos disponibles.', err);
            }
        };

        fetchDevices();
    }, []);

    useEffect(() => {
        console.log("Cambio el condition device", conditionDevice);
        const fetchAttributesAndOperators = async () => {
            if (!conditionDevice) {
                setAtributos([]);
                setOperadores([]);
                return;
            }

            try {
                const token = localStorage.getItem('token');
                const atributosData = await getAttributes(conditionDevice.type, token);

                if (atributosData.length > 0) {
                    setAtributos(atributosData);
                    setCond(prev => ({
                        ...prev,
                        deviceId: conditionDevice.id,
                        attribute: atributosData[0]
                    }));

                    const operadoresData = await getOperators(atributosData[0], token);
                    setOperadores(operadoresData);
                } else {
                    setAtributos([]);
                    setOperadores([]);
                    console.log('El dispositivo seleccionado no tiene atributos disponibles.');
                }
            } catch (err) {
                console.log('No se pudieron obtener los atributos u operadores del dispositivo seleccionado.', err);
            }
        };

        fetchAttributesAndOperators();
    }, [conditionDevice]);

    useEffect(() => {
        console.log("cambio el action device", actionDevice);
        const fetchActions = async () => {
            if (!actionDevice) {
                setAcciones([]);
                return;
            }

            try {
                const token = localStorage.getItem('token');
                const accionesData = await getActions(actionDevice.type, token);

                if (accionesData.length > 0) {
                    setAcciones(accionesData);
                    setAct(prev => ({ ...prev, deviceId: actionDevice.id }));
                } else {
                    setAcciones([]);
                    console.log('El dispositivo seleccionado no tiene acciones disponibles.');
                }
            } catch (err) {
                console.log('No se pudieron obtener las acciones del dispositivo seleccionado.', err);
            }
        };

        fetchActions();
    }, [actionDevice]);

    const handleSubmit = () => {
        const errors = {};

        if (!name.trim()) errors.name = 'El nombre es obligatorio';
        else if (!cond.deviceId) errors.conditionDevice = 'Seleccioná un dispositivo para la condición';
        else if (!cond.attribute) errors.attribute = 'Seleccioná un atributo';
        else if (!cond.operator) errors.operator = 'Seleccioná un operador';
        else if (!cond.value.trim()) errors.value = 'El valor no puede estar vacío';
        else if (!act.deviceId) errors.actionDevice = 'Seleccioná un dispositivo para la acción';
        else if (!act.actionType) errors.actionType = 'Seleccioná una acción';

        if(!requiresParameters(act.actionType)){
            act.parameters = "";    // si no requiere parametros manda string vacio
        }

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
            <div className="modal-rule">
                <h3>Crear nueva regla</h3>

                <input
                    placeholder="Nombre de la regla"
                    value={name}
                    onChange={e => setName(e.target.value)}
                />
                {formErrors.name && <span className="error">{formErrors.name}</span>}

                <h4>Condición</h4>
                <div>
                    <label>Dispositivo:</label>
                    <select
                        value={conditionDevice?.id || ''}
                        onChange={e => setConditionDevice(devices.find(d => d.id === Number(e.target.value)))}
                    >
                        <option value="">Seleccionar</option>
                        {devices.map(device => (
                            <option key={device.id} value={device.id}>{device.name}</option>
                        ))}
                    </select>
                    {formErrors.conditionDevice && <span className="error">{formErrors.conditionDevice}</span>}
                </div>
                <div>
                    <label>Atributo:</label>
                    <select value={cond.attribute} onChange={e => setCond({...cond, attribute: e.target.value})}>
                        <option value="">Seleccionar</option>
                        {atributos.map(attr => (
                            <option key={attr} value={attr}>{attr}</option>
                        ))}
                    </select>
                    {formErrors.attribute && <span className="error">{formErrors.attribute}</span>}
                </div>
                <div>
                    <label>Operador:</label>
                    <select value={cond.operator} onChange={e => setCond({ ...cond, operator: e.target.value })}>
                        <option value="">Seleccionar</option>
                        {operadores.map(op => (
                            <option key={op} value={op}>{op}</option>
                        ))}
                    </select>
                    {formErrors.operator && <span className="error">{formErrors.operator}</span>}
                </div>
                <div>
                    <label>Valor:</label>
                    <input type="text" value={cond.value} onChange={e => setCond({ ...cond, value: e.target.value })} />
                    {formErrors.value && <span className="error">{formErrors.value}</span>}
                </div>

                <h4>Acción</h4>
                <div>
                    <label>Dispositivo:</label>
                    <select onChange={e => setActionDevice(devices.find(d => d.id === Number(e.target.value)))}>
                        <option value="">Seleccionar</option>
                        {devices.map(device => (
                            <option key={device.id} value={device.id}>{device.name}</option>
                        ))}
                    </select>
                    {formErrors.actionDevice && <span className="error">{formErrors.actionDevice}</span>}
                </div>
                <div>
                    <label>Tipo de acción:</label>
                    <select value={act.actionType} onChange={e => setAct({ ...act, actionType: e.target.value })}>
                        <option value="">Seleccionar</option>
                        {acciones.map(action => (
                            <option key={action} value={action}>{action}</option>
                        ))}
                    </select>
                    {formErrors.actionType && <span className="error">{formErrors.actionType}</span>}
                </div>
                <div>
                    <label>Parámetros:</label>
                    <input
                        type="text"
                        value={act.parameters}
                        onChange={e => setAct({ ...act, parameters: e.target.value })}
                        disabled={!requiresParameters(act.actionType)}
                    />
                    {formErrors.parameters && <span className="error">{formErrors.parameters}</span>}
                </div>

                <div className="modal-rule-actions">
                    <TextButton text={"Crear"} handleClick={handleSubmit}/>
                    <TextButton text={"Cancelar"} handleClick={onClose}/>
                </div>
            </div>
        </div>
    );
};

export default RuleFormPopupBis;
