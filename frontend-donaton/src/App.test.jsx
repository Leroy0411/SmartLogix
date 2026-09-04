import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from './App';

// Evitamos llamadas reales a la red mockeando los módulos que las realizan.
vi.mock('./services/donatonApi', () => ({
    getDashboard: vi.fn(() => new Promise(() => {})),
    getProductos: vi.fn(() => Promise.resolve([])),
    crearProducto: vi.fn(),
    actualizarEstadoProducto: vi.fn(),
    eliminarProducto: vi.fn(),
}));

describe('App', () => {
    it('muestra el Dashboard por defecto al iniciar', () => {
        render(<App />);
        expect(screen.getByText('🚚 SmartLogix')).toBeInTheDocument();
        expect(screen.getByText(/Cargando dashboard/i)).toBeInTheDocument();
    });

    it('cambia a la vista de Registrar Producto al hacer clic en el botón de navegación', async () => {
        render(<App />);

        fireEvent.click(screen.getByText('Registrar Producto'));

        await waitFor(() =>
            expect(screen.getByText('📥 Registrar Producto')).toBeInTheDocument()
        );
    });
});
