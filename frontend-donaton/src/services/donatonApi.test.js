import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import {
    getDashboard,
    getProductos,
    crearProducto,
    actualizarEstadoProducto,
    eliminarProducto,
    getCentros,
    crearCentro,
    getPedidos,
    despacharPedido,
    confirmarEntrega,
} from '../services/donatonApi';

vi.mock('axios');

describe('donatonApi (Facade de llamadas HTTP)', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('getDashboard llama al endpoint /bff/dashboard y retorna los datos', async () => {
        axios.get.mockResolvedValue({ data: { totalProductos: 3 } });

        const resultado = await getDashboard();

        expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('/bff/dashboard'));
        expect(resultado).toEqual({ totalProductos: 3 });
    });

    it('getProductos llama al endpoint de MS-Inventario', async () => {
        axios.get.mockResolvedValue({ data: [{ id: 1, categoria: 'ELECTRONICA' }] });

        const resultado = await getProductos();

        expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('/api/inventario'));
        expect(resultado).toHaveLength(1);
    });

    it('crearProducto realiza un POST con los datos enviados', async () => {
        const nuevo = { categoria: 'ALIMENTOS', proveedor: 'Maipú', stock: 10, bodegaId: 1 };
        axios.post.mockResolvedValue({ data: { id: 5, ...nuevo } });

        const resultado = await crearProducto(nuevo);

        expect(axios.post).toHaveBeenCalledWith(expect.stringContaining('/api/inventario'), nuevo);
        expect(resultado.id).toBe(5);
    });

    it('actualizarEstadoProducto realiza un PUT con el estado como query param', async () => {
        axios.put.mockResolvedValue({ data: { id: 1, estado: 'RESERVADO' } });

        await actualizarEstadoProducto(1, 'RESERVADO');

        expect(axios.put).toHaveBeenCalledWith(expect.stringContaining('/api/inventario/1/estado?estado=RESERVADO'));
    });

    it('eliminarProducto realiza un DELETE al endpoint correcto', async () => {
        axios.delete.mockResolvedValue({ data: { mensaje: 'ok' } });

        await eliminarProducto(7);

        expect(axios.delete).toHaveBeenCalledWith(expect.stringContaining('/api/inventario/7'));
    });

    it('getCentros llama al endpoint de centros de distribución de MS-Pedidos', async () => {
        axios.get.mockResolvedValue({ data: [] });

        await getCentros();

        expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('/api/pedidos/centros-distribucion'));
    });

    it('crearCentro realiza un POST con los datos del centro', async () => {
        const centro = { nombre: 'Centro X', capacidadMaxima: 50 };
        axios.post.mockResolvedValue({ data: { id: 1, ...centro } });

        await crearCentro(centro);

        expect(axios.post).toHaveBeenCalledWith(expect.stringContaining('/api/pedidos/centros-distribucion'), centro);
    });

    it('getPedidos llama al endpoint de pedidos de MS-Pedidos', async () => {
        axios.get.mockResolvedValue({ data: [] });

        await getPedidos();

        expect(axios.get).toHaveBeenCalledWith(expect.stringContaining('/api/pedidos'));
    });

    it('despacharPedido realiza un PUT al endpoint de despacho', async () => {
        axios.put.mockResolvedValue({ data: { estado: 'EN_CAMINO' } });

        await despacharPedido(2);

        expect(axios.put).toHaveBeenCalledWith(expect.stringContaining('/api/pedidos/2/despachar'));
    });

    it('confirmarEntrega realiza un PUT con observaciones como query param', async () => {
        axios.put.mockResolvedValue({ data: { estado: 'ENTREGADO' } });

        await confirmarEntrega(2, 'Todo OK');

        expect(axios.put).toHaveBeenCalledWith(expect.stringContaining('observaciones=Todo OK'));
    });
});
