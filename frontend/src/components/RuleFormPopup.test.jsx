import {test} from "vitest";
import { render, screen, fireEvent, act } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';

const devicesMock1 = [
    { id: 1, name: 'Sensor de Temperatura' },
    { id: 2, name: 'Lámpara Inteligente' },
];

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

    it('muestra errores si faltan campos obligatorios, incluso si se completan el nombre y el dispositivo de la condición', () => {
        const onClose = vi.fn();
        const onCreate = vi.fn();

        // Mockear dispositivos para que estén disponibles
        vi.mock('../api/deviceService.js', () => ({
            getAllDevices: () => Promise.resolve(devicesMock1),
        }));

        // Renderizar el componente
        render(<RuleFormPopup onClose={onClose} onCreate={onCreate}/>);

        // Simular que el usuario escribe un nombre en el campo de entrada
        const nameInput = screen.getByTestId("nombreRegla");
        fireEvent.change(nameInput, {target: {value: 'Regla 1'}});

        // Asegurarse de que el valor ha cambiado
        expect(nameInput.value).toBe('Regla 1');

        // Simular que el usuario selecciona un dispositivo para la condición
        const deviceSelect = screen.getByTestId("condDevice"); // Ajusta el selector si el label es diferente

        fireEvent.change(deviceSelect, {target: {value: '2'}});
        // Asegurarse de que el valor del dispositivo ha cambiado
        expect(deviceSelect.value).toBe('1');

        // Hacer clic en el botón "Crear"
        const createButton = screen.getByTestId('crear');
        fireEvent.click(createButton);

        // Verificar que se muestran los mensajes de error restantes
        expect(screen.getByText('Seleccioná un dispositivo para la acción')).toBeInTheDocument();

        // Verificar que no se llamó a onCreate porque aún hay errores
        expect(onCreate).not.toHaveBeenCalled();
    });
});


test('estructura de un test', () => {
    //Arrange
    render(<RuleFormPopup />);
    //Act
    //Assert
});
