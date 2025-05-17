import { useState, useEffect } from 'react';
import './loginPopup.css';
import {
    getActions,
    getAttributes,
    getOperators
} from '../api/ruleService.js'
import { getAllDevices } from '../api/deviceService.js'; // Nuevo endpoint para obtener todos los dispositivos

const RuleFormPopupBis = ({ onClose, onCreate }) => {
    const [name, setName] = useState('');
    const [devices, setDevices] = useState([]); // Lista de dispositivos disponibles
    const [error, setError] = useState('');
    const [formErrors, setFormErrors] = useState({});

    const [conditionDevice, setConditionDevice] = useState(null);
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
                setError('No se pudieron obtener los dispositivos disponibles.', err);
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
                    setError('El dispositivo seleccionado no tiene atributos disponibles.');
                }
            } catch (err) {
                setError('No se pudieron obtener los atributos u operadores del dispositivo seleccionado.');
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
                    setError('El dispositivo seleccionado no tiene acciones disponibles.');
                }
            } catch (err) {
                setError('No se pudieron obtener las acciones del dispositivo seleccionado.');
            }
        };

        fetchActions();
    }, [actionDevice]);

    const handleSubmit = () => {
        const errors = {};

        if (!name.trim()) errors.name = 'El nombre es obligatorio';
        if (!cond.operator) errors.operator = 'Seleccioná un operador';
        if (!cond.value.trim()) errors.value = 'El valor no puede estar vacío';
        if (!act.actionType) errors.actionType = 'Seleccioná una acción';

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
            <div className="modal">
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
                    <select onChange={e => setConditionDevice(devices.find(d => d.id === Number(e.target.value)))}>
                        <option value="">Seleccionar</option>
                        {devices.map(device => (
                            <option key={device.id} value={device.id}>{device.name}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Atributo:</label>
                    <select value={cond.attribute} onChange={e => setCond({ ...cond, attribute: e.target.value })}>
                        <option value="">Seleccionar</option>
                        {atributos.map(attr => (
                            <option key={attr} value={attr}>{attr}</option>
                        ))}
                    </select>
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
                    <label>Dispositivo:</label>
                    <select onChange={e => setActionDevice(devices.find(d => d.id === Number(e.target.value)))}>
                        <option value="">Seleccionar</option>
                        {devices.map(device => (
                            <option key={device.id} value={device.id}>{device.name}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Tipo de acción:</label>
                    <select value={act.actionType} onChange={e => setAct({ ...act, actionType: e.target.value })}>
                        <option value="">Seleccionar</option>
                        {acciones.map(action => (
                            <option key={action} value={action}>{action}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Parámetros:</label>
                    <input
                        type="text"
                        value={act.parameters}
                        onChange={e => setAct({ ...act, parameters: e.target.value })}
                        disabled={!requiresParameters(act.actionType)}
                    />
                </div>

                <div className="modal-actions">
                    <button onClick={handleSubmit}>Crear</button>
                    <button onClick={onClose}>Cancelar</button>
                </div>
            </div>
        </div>
    );
};

export default RuleFormPopupBis;
