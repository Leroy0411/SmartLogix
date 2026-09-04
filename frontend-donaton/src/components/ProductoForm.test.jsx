import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ProductoForm from './ProductoForm';
import { useProductos } from '../hooks/useProductos';

vi.mock('../hooks/useProductos');

describe('ProductoForm', () => {
    const crearMock = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
        useProductos.mockReturnValue({ crear: crearMock });
    });

    function llenarFormulario() {
        fireEvent.change(screen.getByPlaceholderText('Ej: Distribuidora Central'), { target: { value: 'Distribuidora Central' } });
        const inputs = document.querySelectorAll('input');
        fireEvent.change(inputs[1], { target: { value: '15' } }); // stock
        fireEvent.change(inputs[2], { target: { value: '2' } });  // bodegaId
    }

    it('renderiza todos los campos del formulario', () => {
        render(<ProductoForm />);

        expect(screen.getByText('📥 Registrar Producto')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Ej: Distribuidora Central')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Registrar Producto/i })).toBeInTheDocument();
    });

    it('al enviar datos válidos llama a crear() con el payload correcto', async () => {
        crearMock.mockResolvedValue({ id: 1 });
        render(<ProductoForm />);

        llenarFormulario();
        fireEvent.click(screen.getByRole('button', { name: /Registrar Producto/i }));

        await waitFor(() => expect(crearMock).toHaveBeenCalledTimes(1));
        const payload = crearMock.mock.calls[0][0];
        expect(payload.categoria).toBe('ELECTRONICA');
        expect(payload.proveedor).toBe('Distribuidora Central');
        expect(payload.stock).toBe(15);
        expect(payload.bodegaId).toBe(2);
    });

    it('muestra mensaje de éxito cuando el producto se registra correctamente', async () => {
        crearMock.mockResolvedValue({ id: 1 });
        render(<ProductoForm />);

        llenarFormulario();
        fireEvent.click(screen.getByRole('button', { name: /Registrar Producto/i }));

        await waitFor(() =>
            expect(screen.getByText(/Producto registrado exitosamente/i)).toBeInTheDocument()
        );
    });

    it('muestra mensaje de error cuando la API rechaza la solicitud', async () => {
        crearMock.mockRejectedValue({ response: { data: { error: 'Categoría no soportada' } } });
        render(<ProductoForm />);

        llenarFormulario();
        fireEvent.click(screen.getByRole('button', { name: /Registrar Producto/i }));

        await waitFor(() =>
            expect(screen.getByText(/Categoría no soportada/i)).toBeInTheDocument()
        );
    });

    it('permite cambiar la categoría del producto mediante el select', () => {
        render(<ProductoForm />);

        const select = screen.getByDisplayValue('ELECTRONICA');
        fireEvent.change(select, { target: { value: 'HOGAR' } });

        expect(select.value).toBe('HOGAR');
    });
});
