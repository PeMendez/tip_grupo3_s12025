import {test} from "vitest";
import {render, screen, fireEvent, waitFor} from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import RuleFormPopup from './RuleFormPopup';

describe('RuleFormPopup Component', () => {
    it('muestra errores al intentar enviar el formulario sin nombre de regla', () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();
        // Arrange
        // Renderizar el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate} />);

        // Act
        // Hacer clic en el botón "Crear"
        const createButton = screen.getByTestId("crear");
        fireEvent.click(createButton);

        //Assert
        // Verificar que se muestra el mensaje de error
        expect(screen.getByText('El nombre es obligatorio')).toBeInTheDocument();
        //expect(screen.getByText('Seleccioná un dispositivo para la condición')).toBeInTheDocument();
        //expect(screen.getByText('Seleccioná un dispositivo para la acción')).toBeInTheDocument();

        // Verificar que no se llamó a onCreate porque el formulario es inválido
        expect(onCreate).not.toHaveBeenCalled();
    });

    it('muestra errores si se completa el nombre solamente', () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();

        // Renderizar el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate} />);

        // Simular que el usuario escribe un nombre en el campo de entrada
        const nameInput = screen.getByTestId("nombreRegla");
        fireEvent.change(nameInput, { target: { value: 'Regla 1' } });

        // Asegurarse de que el valor ha cambiado
        expect(nameInput.value).toBe('Regla 1');

        // Hacer clic en el botón "Crear"
        const createButton = screen.getByTestId('crear');
        fireEvent.click(createButton);

        // Verificar que se muestran los mensajes de error restantes
        expect(screen.getByText('Seleccioná un dispositivo para la condición')).toBeInTheDocument();
        //expect(screen.getByText('Seleccioná un dispositivo para la acción')).toBeInTheDocument();

        // Verificar que no se llamó a onCreate porque aún hay errores
        expect(onCreate).not.toHaveBeenCalled();
    });

    it('muestra errores si se completan el nombre y el dispositivo de la condición', async () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();

        vi.mock('../api/deviceService.js', () => ({
            getAllDevices: vi.fn(() =>
                Promise.resolve([
                    { id: 1, name: 'Sensor de Temperatura' },
                    { id: 2, name: 'Lámpara Inteligente' },
                ])
            ),
        }));

        // Renderizar el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate}/>);

        await waitFor(
            () => {
                expect(screen.getByTestId('condDevice')).toBeInTheDocument();
            }
        )

        // Simular que el usuario escribe un nombre en el campo de entrada
        const nameInput = screen.getByTestId("nombreRegla");
        fireEvent.change(nameInput, {target: {value: 'Regla 1'}});

        // Asegurarse de que el valor ha cambiado
        expect(nameInput.value).toBe('Regla 1');

        // Simular que el usuario selecciona un dispositivo para la condición
        const deviceSelect = screen.getByTestId("condDevice"); // Ajusta el selector si el label es diferente

        fireEvent.change(deviceSelect, {target: {value: '1'}});
        // Asegurarse de que el valor del dispositivo ha cambiado
        expect(deviceSelect.value).toBe('1');

        // Hacer clic en el botón "Crear"
        const createButton = screen.getByTestId('crear');
        fireEvent.click(createButton);

        // Verificar que se muestran los mensajes de error restantes
        expect(screen.getByText('Seleccioná un atributo')).toBeInTheDocument();

        // Verificar que no se llamó a onCreate porque aún hay errores
        expect(onCreate).not.toHaveBeenCalled();
    });

    it('muestra errores si se completan nombre, dispositivo, atributo de la condicion', async () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();

        vi.mock('../api/deviceService.js', () => ({
            getAllDevices: vi.fn(() =>
                Promise.resolve([
                    { id: 1, name: 'Sensor de Temperatura', type: 'temperature_sensor' },
                    { id: 2, name: 'Lámpara Inteligente', type: 'dimmer' },
                ])
            ),
        }));

        vi.mock('../hooks/useDeviceAttributes', () => ({
            __esModule: true,
            default: vi.fn((deviceType, token) => {
                console.log('Mock useDeviceAttributes called with:', deviceType);
                return {
                    attributes:
                        [{ value: 'TEMPERATURE', label: 'Temperatura' }],
                    loading: false,
                };
            }),
        }));

        // Renderizar el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate}/>);

        //Esperar que se carguen los dispositivos
        await waitFor(() => {
            expect(screen.getByTestId('condDevice')).toBeInTheDocument();
        });

        // Simular que el usuario escribe un nombre en el campo de entrada
        const nameInput = screen.getByTestId("nombreRegla");
        fireEvent.change(nameInput, {target: {value: 'Regla 1'}});

        // Asegurarse de que el valor ha cambiado
        expect(nameInput.value).toBe('Regla 1');

        // Simular que el usuario selecciona un dispositivo para la condición
        const deviceSelect = screen.getByTestId("condDevice"); // Ajusta el selector si el label es diferente
        fireEvent.change(deviceSelect, {target: {value: '1'}});

        // Asegurarse de que el valor del dispositivo ha cambiado
        expect(deviceSelect.value).toBe('1');

        //Esperar a que los atributos se carguen
        await waitFor(()=>{
            expect(screen.getByTestId("attributeSelect"));
        });

        // Simular la selección de un atributo
        const attributeSelect = screen.getByTestId('attributeSelect');
        fireEvent.change(attributeSelect, { target: { value: 'TEMPERATURE' } }); // Selecciona "Temperatura"

        // Verificar que el atributo seleccionado es correcto
        expect(attributeSelect.value).toBe('TEMPERATURE');

        // Hacer clic en el botón "Crear"
        const createButton = screen.getByTestId('crear');
        fireEvent.click(createButton);

        // Verificar que se muestran los mensajes de error
        expect(screen.getByText('Seleccioná un operador')).toBeInTheDocument();

        // Verificar que no se llamó a onCreate porque aún hay errores
        expect(onCreate).not.toHaveBeenCalled();
    });

    it('regla creada correctamente con sensor temp y dimmer', async () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();

        vi.mock('../api/deviceService.js', () => ({
            getAllDevices: vi.fn(() =>
                Promise.resolve([
                    { id: 1, name: 'Sensor de Temperatura', type: 'temperature_sensor' },
                    { id: 2, name: 'Lámpara Inteligente', type: 'dimmer' },
                ])
            ),
        }));

        vi.mock('../hooks/useDeviceAttributes', () => ({
            __esModule: true,
            default: vi.fn((deviceType, token) => {
                console.log('Mock useDeviceAttributes called with:', deviceType);
                let attributes = [{ value: 'TEMPERATURE', label: 'Temperatura' }];
                return {
                    attributes: attributes,
                    loading: false,
                };
            }),
        }));

        vi.mock('../hooks/useAttributeOperators', () => ({
            __esModule: true,
            default: vi.fn((attribute, token) => {
                console.log('Mock useAttributeOperators called with:', attribute);

                let operators = [];
                if (attribute === 'TEMPERATURE') {
                    operators = [
                        { value: '>', label: 'Mayor que' },
                        { value: '<', label: 'Menor que' },
                        { value: '=', label: 'Igual a' },
                    ];
                }
                return {
                    operators,
                    loading: false,
                };
            }),
        }));

        vi.mock('../hooks/useDeviceActions', () => ({
            __esModule: true,
            default: vi.fn((deviceType, token) => {
                console.log('Mock useDeviceActions called with:', deviceType);
                let actions = [{ value: 'SET_BRIGHTNESS', label: 'Ajustar Brillo' },];
                return {
                    actions,
                    loading: false,
                };
            }),
        }));


        // Renderizar el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate}/>);

        //Esperar que se carguen los dispositivos
        await waitFor(() => {
            expect(screen.getByTestId('condDevice')).toBeInTheDocument();
        });

        // Simular que el usuario escribe un nombre en el campo de entrada
        const nameInput = screen.getByTestId("nombreRegla");
        fireEvent.change(nameInput, {target: {value: 'Regla 1'}});

        // Asegurarse de que el valor ha cambiado
        expect(nameInput.value).toBe('Regla 1');

        // Simular que el usuario selecciona un dispositivo para la condición
        const deviceSelect = screen.getByTestId("condDevice"); // Ajusta el selector si el label es diferente
        fireEvent.change(deviceSelect, {target: {value: '1'}});

        // Asegurarse de que el valor del dispositivo ha cambiado
        expect(deviceSelect.value).toBe('1');

        //Esperar a que los atributos se carguen
        await waitFor(()=>{
            expect(screen.getByTestId("attributeSelect"));
        });

        // Simular la selección de un atributo
        const attributeSelect = screen.getByTestId('attributeSelect');
        fireEvent.change(attributeSelect, { target: { value: 'TEMPERATURE' } }); // Selecciona "Temperatura"

        // Verificar que el atributo seleccionado es correcto
        expect(attributeSelect.value).toBe('TEMPERATURE');

        //Seleccion del operador:
        await waitFor(() => {
            expect(screen.getByTestId('operSelect')).toBeInTheDocument();
        });

        const operatorSelect = screen.getByTestId('operSelect');
        fireEvent.change(operatorSelect, { target: { value: '>' } });

        expect(operatorSelect.value).toBe('>');

        const condParameter = screen.getByTestId('condValue');
        fireEvent.change(condParameter, {target: {value: '30'}});
        expect(condParameter.value).toBe('30');

        //Seleccion del dispositivo de accion
        await waitFor(() => {
            expect(screen.getByTestId('actDeviceSelect')).toBeInTheDocument();
        });

        const actionDeviceSelect = screen.getByTestId('actDeviceSelect');
        fireEvent.change(actionDeviceSelect, { target: { value: '2' } });

        expect(actionDeviceSelect.value).toBe('2');

        //Seleccion del tipo de accion
        await waitFor(() => {
            expect(screen.getByTestId('actDeviceSelect')).toBeInTheDocument();
        });

        const actionSelect = screen.getByTestId('actionSelect');
        fireEvent.change(actionSelect, { target: { value: 'SET_BRIGHTNESS' } });

        expect(actionSelect.value).toBe('SET_BRIGHTNESS');

        //Setear el parametro de la accion de setear brillo

        const parameterInput = screen.getByTestId('actParameter');
        fireEvent.change(parameterInput, {target: {value: '25'}});
        expect(parameterInput.value).toBe('25');

        // Hacer clic en el botón "Crear"
        const createButton = screen.getByTestId('crear');
        fireEvent.click(createButton);

        // Verificar que se llamó a onCreate porque no hay errores
        expect(onCreate).toHaveBeenCalled();
    });

    it("regla creada correctamente con sensor de ventana y smart outlet", async() =>{
        const onClose = vi.fn();
        const onCreate = vi.fn();

        vi.mock('../api/deviceService.js', () => ({
            getAllDevices: vi.fn(() =>
                Promise.resolve([
                    { id: 1, name: 'Sensor de Ventana', type: 'opening_sensor' },
                    { id: 2, name: 'Enchufe Inteligente', type: 'smart_outlet' },
                ])
            ),
        }));

        vi.mock('../hooks/useDeviceAttributes', () => ({
            __esModule: true,
            default: vi.fn((deviceType, token) => {
                console.log('Mock useDeviceAttributes called with:', deviceType);
                let attributes = [{ value: 'IS_OPEN', label: 'Está' }];
                return {
                    attributes: attributes,
                    loading: false,
                };
            }),
        }));

        vi.mock('../hooks/useAttributeOperators', () => ({
            __esModule: true,
            default: vi.fn((attribute, token) => {
                console.log('Mock useAttributeOperators called with:', attribute);

                let operators = [];
                if (attribute === 'IS_OPEN') {
                    operators = [
                        { value: '=', label: 'Igual a' },
                    ];
                }
                return {
                    operators,
                    loading: false,
                };
            }),
        }));

        vi.mock('../hooks/useDeviceActions', () => ({
            __esModule: true,
            default: vi.fn((deviceType, token) => {
                console.log('Mock useDeviceActions called with:', deviceType);
                let actions = [
                    { value: 'TURN_ON', label: 'Encender' },
                    { value: 'TURN_OFF', label: 'Apagar' },
                ];
                return {
                    actions,
                    loading: false,
                };
            }),
        }));


        // Renderizar el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate}/>);

        //Esperar que se carguen los dispositivos
        await waitFor(() => {
            expect(screen.getByTestId('condDevice')).toBeInTheDocument();
        });

        // Simular que el usuario escribe un nombre en el campo de entrada
        const nameInput = screen.getByTestId("nombreRegla");
        fireEvent.change(nameInput, {target: {value: 'Regla 2'}});

        // Asegurarse de que el valor ha cambiado
        expect(nameInput.value).toBe('Regla 2');

        // Simular que el usuario selecciona un dispositivo para la condición
        const deviceSelect = screen.getByTestId("condDevice"); // Ajusta el selector si el label es diferente
        fireEvent.change(deviceSelect, {target: {value: '1'}});

        // Asegurarse de que el valor del dispositivo ha cambiado
        expect(deviceSelect.value).toBe('1');

        //Esperar a que los atributos se carguen
        await waitFor(()=>{
            expect(screen.getByTestId("attributeSelect"));
        });

        // Simular la selección de un atributo
        const attributeSelect = screen.getByTestId('attributeSelect');
        fireEvent.change(attributeSelect, { target: { value: 'IS_OPEN' } }); // Selecciona "Temperatura"

        // Verificar que el atributo seleccionado es correcto
        expect(attributeSelect.value).toBe('IS_OPEN');

        //Seleccion del operador:
        await waitFor(() => {
            expect(screen.getByTestId('operSelect')).toBeInTheDocument();
        });

        const operatorSelect = screen.getByTestId('operSelect');
        fireEvent.change(operatorSelect, { target: { value: '=' } });

        expect(operatorSelect.value).toBe('=');

        const condParameter = screen.getByTestId('condValueBool');
        fireEvent.change(condParameter, {target: {value: 'true'}});
        expect(condParameter.value).toBe('true');

        //Seleccion del dispositivo de accion
        await waitFor(() => {
            expect(screen.getByTestId('actDeviceSelect')).toBeInTheDocument();
        });

        const actionDeviceSelect = screen.getByTestId('actDeviceSelect');
        fireEvent.change(actionDeviceSelect, { target: { value: '2' } });

        expect(actionDeviceSelect.value).toBe('2');

        //Seleccion del tipo de accion
        await waitFor(() => {
            expect(screen.getByTestId('actDeviceSelect')).toBeInTheDocument();
        });

        const actionSelect = screen.getByTestId('actionSelect');
        fireEvent.change(actionSelect, { target: { value: 'TURN_ON' } });

        expect(actionSelect.value).toBe('TURN_ON');

        // Hacer clic en el botón "Crear"
        const createButton = screen.getByTestId('crear');
        fireEvent.click(createButton);

        // Verificar que se llamó a onCreate porque no hay errores
        expect(onCreate).toHaveBeenCalled();
    });

});


test('estructura de un test', () => {
    //Arrange
    render(<RuleFormPopup />);
    //Act
    //Assert
});
