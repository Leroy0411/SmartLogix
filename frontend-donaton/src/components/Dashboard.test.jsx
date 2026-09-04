import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import Dashboard from './Dashboard';
import { getDashboard } from '../services/donatonApi';

vi.mock('../services/donatonApi');

describe('Dashboard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('muestra el indicador de carga mientras llega la respuesta del BFF', () => {
        getDashboard.mockReturnValue(new Promise(() => {})); // nunca resuelve

        render(<Dashboard />);

        expect(screen.getByText(/Cargando dashboard/i)).toBeInTheDocument();
    });

    it('renderiza las estadísticas agregadas cuando el BFF responde', async () => {
        getDashboard.mockResolvedValue({
            totalProductos: 10,
            productosDisponibles: 4,
            productosReservados: 3,
            productosAgotados: 3,
            totalCentrosDistribucion: 2,
            centrosActivos: 2,
            centrosSaturados: 0,
            totalPedidos: 5,
            pedidosPendientes: 2,
            pedidosEnCamino: 2,
            pedidosEntregados: 1,
            alertas: [],
        });

        render(<Dashboard />);

        await waitFor(() => expect(screen.getByText('10')).toBeInTheDocument());
        expect(screen.getByText('Total productos')).toBeInTheDocument();
        expect(screen.getByText('Centros de Distribución')).toBeInTheDocument();
        expect(screen.getByText('Pedidos')).toBeInTheDocument();
    });

    it('muestra las alertas del sistema cuando existen centros saturados', async () => {
        getDashboard.mockResolvedValue({
            totalProductos: 0, productosDisponibles: 0, productosReservados: 0, productosAgotados: 0,
            totalCentrosDistribucion: 1, centrosActivos: 0, centrosSaturados: 1,
            totalPedidos: 0, pedidosPendientes: 0, pedidosEnCamino: 0, pedidosEntregados: 0,
            alertas: ['🔴 1 centros de distribución saturados'],
        });

        render(<Dashboard />);

        await waitFor(() => expect(screen.getByText(/centros de distribución saturados/i)).toBeInTheDocument());
        expect(screen.getByText(/Alertas del Sistema/i)).toBeInTheDocument();
    });

    it('muestra un mensaje de error si la petición al BFF falla', async () => {
        getDashboard.mockRejectedValue(new Error('BFF no disponible'));

        render(<Dashboard />);

        await waitFor(() => expect(screen.getByText(/Error: BFF no disponible/i)).toBeInTheDocument());
    });
});
