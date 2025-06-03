import { useState, useEffect } from 'react';
import { getAllDevices } from '../api/deviceService.js'; //
import './styles/rule.css'; //
import TextButton from "./TextButton.jsx"; //

// Importa los nuevos hooks
import useDeviceAttributes from '../hooks/useDeviceAttributes';
import useAttributeOperators from '../hooks/useAttributeOperators';
import useDeviceActions from '../hooks/useDeviceActions';

const RuleFormPopup = ({ onClose, onCreate, device: initialDevice }) => { // Renombrado 'device' prop para claridad
    const [name, setName] = useState(''); //
    const [devices, setDevices] = useState([]); //
    const [formErrors, setFormErrors] = useState({}); //

    const [conditionDevice, setConditionDevice] = useState(initialDevice || null); //
    const [actionDevice, setActionDevice] = useState(null); //

    const token = localStorage.getItem('token');

    const [cond, setCond] = useState({ //
        deviceId: initialDevice?.id || '', //
        attribute: '',
        operator: '',
        value: '' //
    });
    // Usar los custom hooks
    const { attributes: conditionAttributes, loading: loadingAttributes } = useDeviceAttributes(conditionDevice?.type, token);
    // Suponiendo que cond.attribute guarda el 'value' del atributo seleccionado
    const { operators: conditionOperators, loading: loadingOperators } = useAttributeOperators(cond.attribute, token);
    const { actions: deviceActions, loading: loadingActions } = useDeviceActions(actionDevice?.type, token);

    const [act, setAct] = useState({ //
        deviceId: '',
        actionType: '',
        parameters: '' //
    });

    useEffect(() => { //
        if (initialDevice) { //
            setConditionDevice(initialDevice); //
            setCond(prev => ({ ...prev, deviceId: initialDevice.id })); //
        }
    }, [initialDevice]); //

    // Efecto para resetear atributo/operador si cambian los atributos disponibles (ej. al cambiar de dispositivo)
    useEffect(() => {
        if (conditionAttributes.length > 0) {
            // Opcional: auto-seleccionar el primer atributo si no hay uno ya o si el actual no está en la nueva lista
            const currentAttributeIsValid = conditionAttributes.some(attr => attr.value === cond.attribute);
            if (!cond.attribute || !currentAttributeIsValid) {
                setCond(prev => ({
                    ...prev,
                    attribute: conditionAttributes[0].value, // Selecciona el 'value'
                    operator: '', // Resetea operador también
                    value: ''     // Resetea valor
                }));
            }
        } else {
            setCond(prev => ({ ...prev, attribute: '', operator: '', value: ''}));
        }
    }, [conditionAttributes]); // No incluyas cond.attribute aquí para evitar bucles si lo auto-seleccionas

    // Similar para acciones si cambias actionDevice
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


    const requiresParameters = (actionType) => { //
        const actionsWithParams = ['SET_BRIGHTNESS']; //
        return actionsWithParams.includes(actionType); //
    };

    useEffect(() => { //
        const fetchDevices = async () => { //
            try { //
                const devicesData = await getAllDevices(token); //
                setDevices(devicesData); //
            } catch (err) { //
                console.log('No se pudieron obtener los dispositivos disponibles.', err); //
            }
        };
        fetchDevices(); //
    }, [token]); //


    const handleSubmit = () => { //
        // ... (tu lógica de validación no cambia mucho, pero asegúrate que los errores se manejen bien)
        const errors = {}; //

        if (!name.trim()) errors.name = 'El nombre es obligatorio'; //
        if (!conditionDevice) errors.conditionDevice = 'Seleccioná un dispositivo para la condición'; // Adaptado
        else if (!cond.attribute) errors.attribute = 'Seleccioná un atributo'; //
        else if (!cond.operator) errors.operator = 'Seleccioná un operador'; //
        else if (!cond.value.trim() && cond.attribute !== 'IS_ON') errors.value = 'El valor no puede estar vacío'; // Ajuste para IS_ON si usas dropdown
        else if (!actionDevice) errors.actionDevice = 'Seleccioná un dispositivo para la acción'; // Adaptado
        else if (!act.actionType) errors.actionType = 'Seleccioná una acción'; //


        if(!requiresParameters(act.actionType)){ //
            act.parameters = "";    // si no requiere parametros manda string vacio //
        } else if (requiresParameters(act.actionType) && !act.parameters.trim()) {
            errors.parameters = 'Los parámetros son obligatorios para esta acción';
        }


        if (Object.keys(errors).length > 0) { //
            setFormErrors(errors); //
            return; //
        }
        setFormErrors({}); // Limpia errores si to-do está bien

        const newRule = { //
            name, //
            conditions: [{ //
                deviceId: conditionDevice.id, //
                attribute: cond.attribute, //
                operator: cond.operator, //
                value: cond.value //
            }],
            actions: [{ //
                deviceId: actionDevice.id, //
                actionType: act.actionType, //
                parameters: act.parameters //
            }]
        };

        onCreate(newRule); //
        onClose(); //
    };

    console.log('Datos para el desplegable de Acciones:', JSON.stringify(deviceActions, null, 2));
    console.log('Estado de carga para Acciones:', loadingActions);
    console.log('Dispositivo de Acción seleccionado:', actionDevice ? actionDevice.name : 'Ninguno');


    // Render
    return (
        <div className="modal-backdrop"> {/* */}
            <div className="modal-rule"> {/* */}
                <h3>Crear nueva regla</h3> {/* */}

                <input //
                    placeholder="Nombre de la regla" //
                    value={name} //
                    onChange={e => setName(e.target.value)} //
                />
                {formErrors.name && <span className="error">{formErrors.name}</span>} {/* */}

                <h4>Condición</h4> {/* */}
                <div>
                    <label>Dispositivo:</label> {/* */}
                    <select //
                        value={conditionDevice?.id || ''} //
                        onChange={e => {
                            const selectedDev = devices.find(d => d.id === Number(e.target.value)); //
                            setConditionDevice(selectedDev); //
                            setCond(prev => ({ ...prev, deviceId: selectedDev?.id || '', attribute: '', operator: '', value: '' })); // Resetea al cambiar dispositivo
                        }}
                    >
                        <option value="">Seleccionar</option> {/* */}
                        {devices.map(d => ( //
                            <option key={d.id} value={d.id}>{d.name}</option> //
                        ))}
                    </select>
                    {formErrors.conditionDevice && <span className="error">{formErrors.conditionDevice}</span>} {/* */}
                </div>
                <div>
                    <label>Atributo:</label> {/* */}
                    <select
                        value={cond.attribute}
                        onChange={e => setCond({...cond, attribute: e.target.value, operator: '', value: ''})} // Resetea operador y valor
                        disabled={loadingAttributes || !conditionDevice}
                    >
                        <option value="">{loadingAttributes ? "Cargando..." : "Seleccionar"}</option> {/* */}
                        {conditionAttributes.map(attr => ( // `conditionAttributes` ahora es [{value, label}, ...]
                            <option key={attr.value} value={attr.value}>{attr.label}</option> //
                        ))}
                    </select>
                    {formErrors.attribute && <span className="error">{formErrors.attribute}</span>} {/* */}
                </div>

                {/* Caso especial para el valor del atributo IS_ON */}
                {cond.attribute === 'IS_ON' ? (
                    <div>
                        <label>Valor:</label>
                        <select
                            value={cond.value}
                            onChange={e => setCond({ ...cond, value: e.target.value })}
                            disabled={!cond.attribute}
                        >
                            <option value="">Seleccionar Estado</option>
                            <option value="true">Encendido</option>
                            <option value="false">Apagado</option>
                        </select>
                    </div>
                ) : (
                    <div>
                        <label>Valor:</label> {/* */}
                        <input //
                            type={cond.attribute === 'TEMPERATURE' || cond.attribute === 'BRIGHTNESS' ? 'number' : 'text'} // Ajusta el tipo de input
                            value={cond.value} //
                            onChange={e => setCond({ ...cond, value: e.target.value })} //
                            disabled={!cond.attribute}
                        />
                        {formErrors.value && <span className="error">{formErrors.value}</span>} {/* */}
                    </div>
                )}

                <div>
                    <label>Operador:</label> {/* */}
                    <select
                        value={cond.operator}
                        onChange={e => setCond({ ...cond, operator: e.target.value })} //
                        disabled={loadingOperators || !cond.attribute }
                    >
                        <option value="">{loadingOperators ? "Cargando..." : "Seleccionar"}</option> {/* */}
                        {conditionOperators.map(op => ( // `conditionOperators` ahora es [{value, label}, ...]
                            <option key={op.value} value={op.value}>{op.label}</option> //
                        ))}
                    </select>
                    {formErrors.operator && <span className="error">{formErrors.operator}</span>} {/* */}
                </div>


                <h4>Acción</h4> {/* */}
                <div>
                    <label>Dispositivo:</label> {/* */}
                    <select onChange={e => { //
                        const selectedDev = devices.find(d => d.id === Number(e.target.value)); //
                        setActionDevice(selectedDev); //
                        setAct(prev => ({ ...prev, deviceId: selectedDev?.id || '', actionType: '', parameters: '' })); // Resetea
                    }}>
                        <option value="">Seleccionar</option> {/* */}
                        {devices.map(d => ( //
                            <option key={d.id} value={d.id}>{d.name}</option> //
                        ))}
                    </select>
                    {formErrors.actionDevice && <span className="error">{formErrors.actionDevice}</span>} {/* */}
                </div>
                <div>
                    <label>Tipo de acción:</label> {/* */}
                    <select
                        value={act.actionType}
                        onChange={e => setAct({ ...act, actionType: e.target.value, parameters: '' })} // Resetea parámetros
                        disabled={loadingActions || !actionDevice}
                    >
                        <option value="">{loadingActions ? "Cargando..." : "Seleccionar"}</option> {/* */}
                        {(deviceActions || []).map(action => ( // `deviceActions` ahora es [{value, label}, ...]
                            <option key={action.value} value={action.value}>{action.label}</option> //
                        ))}
                    </select>
                    {formErrors.actionType && <span className="error">{formErrors.actionType}</span>} {/* */}
                </div>
                <div>
                    <label>Parámetros:</label> {/* */}
                    <input //
                        type={act.actionType === 'SET_BRIGHTNESS' ? 'number' : 'text'}
                        value={act.parameters} //
                        onChange={e => setAct({ ...act, parameters: e.target.value })} //
                        disabled={!requiresParameters(act.actionType)} //
                        placeholder={act.actionType === 'SET_BRIGHTNESS' ? '0-100' : ''}
                    />
                    {formErrors.parameters && <span className="error">{formErrors.parameters}</span>} {/* */}
                </div>

                <div className="modal-rule-actions"> {/* */}
                    <TextButton text={"Crear"} handleClick={handleSubmit}/> {/* */}
                    <TextButton text={"Cancelar"} handleClick={onClose}/> {/* */}
                </div>
            </div>
        </div>
    );
};

export default RuleFormPopup;