import { useState, useEffect } from 'react';
import {getAllDevicesConfigured} from '../api/deviceService.js';
import './styles/rule.css';
import TextButton from "./TextButton.jsx";
import { useAuth } from "../contexts/AuthContext";

// Importa los nuevos hooks
import useDeviceAttributes from '../hooks/useDeviceAttributes';
import useAttributeOperators from '../hooks/useAttributeOperators';
import useDeviceActions from '../hooks/useDeviceActions';

const RuleFormPopup = ({ onClose, onCreate, device: initialDevice }) => {
    const [name, setName] = useState('');
    const [devices, setDevices] = useState([]);
    const [formErrors, setFormErrors] = useState({});

    // --- ESTADOS DE CONDICIÓN MODIFICADOS ---
    const [conditionType, setConditionType] = useState('DEVICE'); // NUEVO: Estado para el tipo de condición
    const [conditionDevice, setConditionDevice] = useState(initialDevice || null);
    const [actionDevice, setActionDevice] = useState(null);

    // Estado para la condición de tipo DEVICE
    const [cond, setCond] = useState({
        deviceId: initialDevice?.id || '',
        attribute: '',
        operator: '',
        value: ''
    });

    // NUEVO: Estado para la condición de tipo TIME
    const [timeCondition, setTimeCondition] = useState({ value: '' });

    const { role } = useAuth();
    const token = localStorage.getItem('token');

    // Usar los custom hooks
    const { attributes: conditionAttributes, loading: loadingAttributes } = useDeviceAttributes(conditionDevice?.type, token);
    const { operators: conditionOperators, loading: loadingOperators } = useAttributeOperators(cond.attribute, token);
    const { actions: deviceActions, loading: loadingActions } = useDeviceActions(actionDevice?.type, token);

    const [act, setAct] = useState({
        deviceId: '',
        actionType: '',
        parameters: ''
    });

    useEffect(() => {
        if (initialDevice) {
            setConditionDevice(initialDevice);
            setCond(prev => ({ ...prev, deviceId: initialDevice.id }));
            setConditionType('DEVICE'); // Aseguramos el tipo si viene un dispositivo inicial
        }
    }, [initialDevice]);

    // (El resto de los useEffect se mantienen igual)
    useEffect(() => {
        if (conditionAttributes.length > 0) {
            const currentAttributeIsValid = conditionAttributes.some(attr => attr.value === cond.attribute);
            if (!cond.attribute || !currentAttributeIsValid) {
                setCond(prev => ({
                    ...prev,
                    attribute: conditionAttributes[0].value,
                    operator: '',
                    value: ''
                }));
            }
        } else {
            setCond(prev => ({ ...prev, attribute: '', operator: '', value: ''}));
        }
    }, [conditionAttributes]);

    useEffect(() => {
        if (Array.isArray(deviceActions) && deviceActions.length > 0) {
            const currentActionIsValid = deviceActions.some(action => action.value === act.actionType);
            if (!act.actionType || !currentActionIsValid) {
                setAct(prev => ({
                    ...prev,
                    actionType: deviceActions[0].value,
                    parameters: ''
                }));
            }
        } else if (Array.isArray(deviceActions)){
            setAct(prev => ({ ...prev, actionType: '', parameters: ''}));
        }
    }, [deviceActions, act.actionType]);


    const requiresParameters = (actionType) => {
        const actionsWithParams = ['SET_BRIGHTNESS'];
        return actionsWithParams.includes(actionType);
    };

    useEffect(() => {
        const fetchDevices = async () => {
            try {
                const devicesData = await getAllDevicesConfigured(token, role.toString());
                setDevices(devicesData);
            } catch (err) {
                console.log('No se pudieron obtener los dispositivos disponibles.', err);
            }
        };
        fetchDevices();
    }, [token]);


    const handleSubmit = () => {
        // --- LÓGICA DE VALIDACIÓN MODIFICADA ---
        const errors = {};
        if (!name.trim()) errors.name = 'El nombre es obligatorio';

        // Validaciones específicas por tipo de condición
        if (conditionType === 'DEVICE') {
            if (!conditionDevice) errors.conditionDevice = 'Seleccioná un dispositivo para la condición';
            else if (!cond.attribute) errors.attribute = 'Seleccioná un atributo';
            else if (!cond.operator) errors.operator = 'Seleccioná un operador';
            else if (!cond.value.trim() && cond.attribute !== 'IS_ON' && cond.attribute !== 'IS_OPEN') errors.value = 'El valor no puede estar vacío';
        } else if (conditionType === 'TIME') {
            if (!timeCondition.value) errors.time = 'Por favor, ingresá una hora válida.';
        }

        // Validaciones de la acción (sin cambios)
        if (!actionDevice) errors.actionDevice = 'Seleccioná un dispositivo para la acción';
        else if (!act.actionType) errors.actionType = 'Seleccioná una acción';

        if(!requiresParameters(act.actionType)){
            act.parameters = "";
        } else if (requiresParameters(act.actionType) && !act.parameters.trim()) {
            errors.parameters = 'Los parámetros son obligatorios para esta acción';
        }

        if (Object.keys(errors).length > 0) {
            setFormErrors(errors);
            return;
        }
        setFormErrors({});

        // --- CONSTRUCCIÓN DE LA REGLA MODIFICADA ---
        let conditions;
        if (conditionType === 'DEVICE') {
            conditions = [{
                type: "DEVICE",
                deviceId: conditionDevice.id,
                attribute: cond.attribute,
                operator: cond.operator,
                value: cond.value
            }];
        } else { // TIME
            conditions = [{
                type: "TIME",
                deviceId: null,
                attribute: 0,
                // El operador para la hora suele ser de igualdad, así que lo podemos fijar.
                // Si necesitaras más (ej. "antes de", "después de"), se podría agregar otro select.
                operator: "EQUALS",
                value: timeCondition.value // formato "HH:mm"
            }];
        }

        const newRule = {
            name,
            conditions, // Usamos las condiciones construidas dinámicamente
            actions: [{
                deviceId: actionDevice.id,
                actionType: act.actionType,
                parameters: act.parameters
            }]
        };

        onCreate(newRule);
        onClose();
    };

    // NUEVO: Handler para cambiar el tipo de condición y limpiar los estados correspondientes
    const handleConditionTypeChange = (type) => {
        setConditionType(type);
        setFormErrors({}); // Limpiar errores al cambiar
        // Reseteamos el estado de la condición que no está seleccionada
        if (type === 'DEVICE') {
            setTimeCondition({ value: '' });
        } else {
            setConditionDevice(null);
            setCond({ deviceId: '', attribute: '', operator: '', value: '' });
        }
    }

    return (
        <div className="modal-backdrop">
            <div className="modal-rule">
                <h3>Crear nueva regla</h3>

                <input
                    placeholder="Nombre de la regla"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    data-testid = "nombreRegla"
                />
                {formErrors.name && <span className="error">{formErrors.name}</span>}

                <h4>Condición</h4>

                {/* NUEVO: Selector para el tipo de condición */}
                <div className="condition-type-selector">
                    <label>
                        <input
                            type="radio"
                            name="conditionType"
                            value="DEVICE"
                            checked={conditionType === 'DEVICE'}
                            onChange={() => handleConditionTypeChange('DEVICE')}
                        />
                        Dispositivo
                    </label>
                    <label>
                        <input
                            type="radio"
                            name="conditionType"
                            value="TIME"
                            checked={conditionType === 'TIME'}
                            onChange={() => handleConditionTypeChange('TIME')}
                        />
                        Hora
                    </label>
                </div>

                {/* MODIFICADO: Renderizado condicional del formulario de condición */}
                {conditionType === 'DEVICE' && (
                    <>
                        <div>
                            <label>Dispositivo:</label>
                            <select
                                value={conditionDevice?.id || ''}
                                onChange={e => {
                                    const selectedDev = devices.find(d => d.id === Number(e.target.value));
                                    setConditionDevice(selectedDev);
                                    setCond(prev => ({ ...prev, deviceId: selectedDev?.id || '', attribute: '', operator: '', value: '' }));
                                }}
                                data-testid = "condDevice"
                            >
                                <option value="">Seleccionar</option>
                                {devices.map(d => (
                                    <option key={d.id} value={d.id}>{d.name}</option>
                                ))}
                            </select>
                            {formErrors.conditionDevice && <span className="error">{formErrors.conditionDevice}</span>}
                        </div>
                        <div>
                            <label>Atributo:</label>
                            <select
                                value={cond.attribute}
                                onChange={e => setCond({...cond, attribute: e.target.value, operator: '', value: ''})}
                                disabled={loadingAttributes || !conditionDevice}
                                data-testid="attributeSelect"
                            >
                                <option value="">{loadingAttributes ? "Cargando..." : "Seleccionar"}</option>
                                {conditionAttributes.map(attr => (
                                    <option key={attr.value} value={attr.value}>{attr.label}</option>
                                ))}
                            </select>
                            {formErrors.attribute && <span className="error">{formErrors.attribute}</span>}
                        </div>

                        {cond.attribute === 'IS_ON' || cond.attribute === 'IS_OPEN' ? (
                            <div>
                                <label>Valor:</label>
                                <select
                                    value={cond.value}
                                    onChange={e => setCond({ ...cond, value: e.target.value })}
                                    disabled={!cond.attribute}
                                    data-testid="condValueBool"
                                >
                                    <option value="">Seleccionar Estado</option>
                                    <option value="true">{cond.attribute === 'IS_ON' ? "Encendido" : "Abierto"} </option>
                                    <option value="false">{cond.attribute === 'IS_ON' ? "Apagado" : "Cerrado"}</option>
                                </select>
                            </div>
                        ) : (
                            <div>
                                <label>Valor:</label>
                                <input
                                    type={cond.attribute === 'TEMPERATURE' || cond.attribute === 'BRIGHTNESS' ? 'number' : 'text'}
                                    value={cond.value}
                                    onChange={e => setCond({ ...cond, value: e.target.value })}
                                    disabled={!cond.attribute}
                                    data-testid="condValue"
                                />
                                {formErrors.value && <span className="error">{formErrors.value}</span>}
                            </div>
                        )}

                        <div>
                            <label>Operador:</label>
                            <select
                                value={cond.operator}
                                onChange={e => setCond({ ...cond, operator: e.target.value })}
                                disabled={loadingOperators || !cond.attribute }
                                data-testid="operSelect"
                            >
                                <option value="">{loadingOperators ? "Cargando..." : "Seleccionar"}</option>
                                {conditionOperators.map(op => (
                                    <option key={op.value} value={op.value}>{op.label}</option>
                                ))}
                            </select>
                            {formErrors.operator && <span className="error">{formErrors.operator}</span>}
                        </div>
                    </>
                )}

                {/* NUEVO: Formulario para la condición de HORA */}
                {conditionType === 'TIME' && (
                    <div>
                        <label>Hora (HH:mm):</label>
                        <input
                            type="time" // El input de tipo "time" ofrece un selector de hora nativo
                            value={timeCondition.value}
                            onChange={e => setTimeCondition({ value: e.target.value })}
                            data-testid="timeConditionValue"
                        />
                        {formErrors.time && <span className="error">{formErrors.time}</span>}
                    </div>
                )}


                <h4>Acción</h4>
                {/* La sección de Acción no necesita cambios */}
                <div>
                    <label>Dispositivo:</label>
                    <select onChange={e => {
                        const selectedDev = devices.find(d => d.id === Number(e.target.value));
                        setActionDevice(selectedDev);
                        setAct(prev => ({ ...prev, deviceId: selectedDev?.id || '', actionType: '', parameters: '' }));
                    }}
                            data-testid="actDeviceSelect"
                    >
                        <option value="">Seleccionar</option>
                        {devices.map(d => (
                            <option key={d.id} value={d.id}>{d.name}</option>
                        ))}
                    </select>
                    {formErrors.actionDevice && <span className="error">{formErrors.actionDevice}</span>}
                </div>
                <div>
                    <label>Tipo de acción:</label>
                    <select
                        value={act.actionType}
                        onChange={e => setAct({ ...act, actionType: e.target.value, parameters: '' })}
                        disabled={loadingActions || !actionDevice}
                        data-testid="actionSelect"
                    >
                        <option value="">{loadingActions ? "Cargando..." : "Seleccionar"}</option>
                        {(deviceActions || []).map(action => (
                            <option key={action.value} value={action.value}>{action.label}</option>
                        ))}
                    </select>
                    {formErrors.actionType && <span className="error">{formErrors.actionType}</span>}
                </div>
                <div>
                    <label>Parámetros:</label>
                    <input
                        type={act.actionType === 'SET_BRIGHTNESS' ? 'number' : 'text'}
                        value={act.parameters}
                        onChange={e => setAct({ ...act, parameters: e.target.value })}
                        disabled={!requiresParameters(act.actionType)}
                        placeholder={act.actionType === 'SET_BRIGHTNESS' ? '0-100' : ''}
                        data-testid="actParameter"
                    />
                    {formErrors.parameters && <span className="error">{formErrors.parameters}</span>}
                </div>

                <div className="modal-rule-actions">
                    <TextButton text={"Crear"} handleClick={handleSubmit} data-testid="crear"/>
                    <TextButton text={"Cancelar"} handleClick={onClose} data-testid="cancelar"/>
                </div>
            </div>
        </div>
    );
};

export default RuleFormPopup;