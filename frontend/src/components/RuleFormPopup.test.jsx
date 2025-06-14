import {test} from "vitest";
import {render, screen, fireEvent, waitFor} from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import RuleFormPopup from './RuleFormPopup';

// Mockea los módulos una sola vez al principio del archivo de test.
// Vitest elevará (hoist) estos mocks.
vi.mock('../api/deviceService.js');
vi.mock('../hooks/useDeviceAttributes.js');
vi.mock('../hooks/useAttributeOperators.js');
vi.mock('../hooks/useDeviceActions.js');
vi.mock('../contexts/AuthContext', () => ({
    __esModule: true,
    useAuth: vi.fn(() => ({ role: 'ADMIN' })),
}));

// Importa los módulos mockeados DESPUÉS de llamar a vi.mock
import { getAllDevicesConfigured } from '../api/deviceService.js';
import useDeviceAttributes from '../hooks/useDeviceAttributes.js';
import useAttributeOperators from "../hooks/useAttributeOperators.js";
import useDeviceActions from "../hooks/useDeviceActions.js";

describe('RuleFormPopup Component', () => {
    it('muestra errores al intentar enviar el formulario sin nombre de regla', () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();

        getAllDevicesConfigured.mockResolvedValue([]);

        useDeviceAttributes.mockReturnValue({
            attributes: [],
            loading: false,
        });

        useAttributeOperators.mockReturnValue({
            operators: [],
            loading: false,
        });

        useDeviceActions.mockReturnValue({
            actions: [],
            loading: false,
        })
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

        getAllDevicesConfigured.mockResolvedValue([]);

        useDeviceAttributes.mockReturnValue({
            attributes: [],
            loading: false,
        });

        useAttributeOperators.mockReturnValue({
            operators: [],
            loading: false,
        });

        useDeviceActions.mockReturnValue({
            actions: [],
            loading: false,
        })

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

        getAllDevicesConfigured.mockResolvedValue([
            { id: 1, name: 'Sensor de Temperatura' },
            { id: 2, name: 'Lámpara Inteligente' },
        ]);

        useDeviceAttributes.mockReturnValue({
            attributes: [],
            loading: false,
        });

        useAttributeOperators.mockReturnValue({
            operators: [],
            loading: false,
        });

        useDeviceActions.mockReturnValue({
            actions: [],
            loading: false,
        })

        vi.mock("../api/deviceService.js", () => ({
            getAllDevicesConfigured: vi.fn(() =>
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

    it('muestra errores si se completan nombre, dispositivo y atributo de la condicion', async () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();

        // Configura el comportamiento de los mocks para ESTE test en específico.
        getAllDevicesConfigured.mockResolvedValue([
            { id: 1, name: 'Sensor de Temperatura', type: 'TEMPERATURE_SENSOR' },
            { id: 2, name: 'Lámpara Inteligente', type: 'DIMMER' },
        ]);

        useDeviceAttributes.mockReturnValue({
            attributes: [{ value: 'TEMPERATURE', label: 'Temperatura' }],
            loading: false,
        });

        // Renderiza el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate} />);

        // Espera a que los dispositivos se carguen
        await waitFor(() => {
            expect(screen.getByTestId('condDevice')).toBeInTheDocument();
        });

        // Simula la interacción del usuario
        const nameInput = screen.getByTestId("nombreRegla");
        fireEvent.change(nameInput, { target: { value: 'Regla 1' } });

        const deviceSelect = screen.getByTestId("condDevice");
        fireEvent.change(deviceSelect, { target: { value: '1' } });

        // Espera a que los atributos aparezcan después de seleccionar el dispositivo
        await waitFor(() => {
            expect(screen.getByTestId("attributeSelect")).toBeInTheDocument();
        });

        // Selecciona el atributo
        const attributeSelect = screen.getByTestId('attributeSelect');
        fireEvent.change(attributeSelect, { target: { value: 'TEMPERATURE' } });

        // Verifica que el valor es el correcto
        expect(attributeSelect.value).toBe('TEMPERATURE');

        // Intenta crear la regla
        const createButton = screen.getByTestId('crear');
        fireEvent.click(createButton);

        // Verifica el mensaje de error esperado
        expect(await screen.findByText('Seleccioná un operador')).toBeInTheDocument();
        expect(onCreate).not.toHaveBeenCalled();
    });

    it('regla creada correctamente con sensor temp y dimmer', async () => {
        console.log("Arrange");
        const onClose = vi.fn();
        const onCreate = vi.fn();

        getAllDevicesConfigured.mockResolvedValue([
            { id: 1, name: 'Sensor de Temperatura', type: 'TEMPERATURE_SENSOR' },
            { id: 2, name: 'Lámpara Inteligente', type: 'DIMMER' },
        ]);

        useDeviceAttributes.mockReturnValue({
            attributes: [{ value: 'TEMPERATURE', label: 'Temperatura' }],
            loading: false,
        });

        useAttributeOperators.mockReturnValue({
            operators: [
                { value: '>', label: 'Mayor que' },
                { value: '<', label: 'Menor que' },
                { value: '=', label: 'Igual a' },
            ],
            loading: false,
        });

        useDeviceActions.mockReturnValue({
            actions: [{ value: 'SET_BRIGHTNESS', label: 'Ajustar Brillo' },],
            loading: false,
        })

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

        getAllDevicesConfigured.mockResolvedValue([
            { id: 1, name: 'Sensor de Ventana', type: 'opening_sensor' },
            { id: 2, name: 'Enchufe Inteligente', type: 'smart_outlet' },
        ]);

        useDeviceAttributes.mockReturnValue({
            attributes: [{ value: 'IS_OPEN', label: 'Está' }],
            loading: false,
        });

        useAttributeOperators.mockReturnValue({
            operators: [
                { value: '=', label: 'Igual a' },
            ],
            loading: false,
        });

        useDeviceActions.mockReturnValue({
            actions: [
                { value: 'TURN_ON', label: 'Encender' },
                { value: 'TURN_OFF', label: 'Apagar' },
            ],
            loading: false,
        })

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
