import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useProductos } from './useProductos';
import * as api from '../services/donatonApi';

vi.mock('../services/donatonApi');

describe('useProductos (Custom Hook)', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('carga los productos automáticamente al montar', async () => {
        api.getProductos.mockResolvedValue([{ id: 1, categoria: 'ELECTRONICA' }]);

        const { result } = renderHook(() => useProductos());

        expect(result.current.cargando).toBe(true);

        await waitFor(() => expect(result.current.cargando).toBe(false));

        expect(result.current.productos).toHaveLength(1);
        expect(result.current.error).toBeNull();
    });

    it('registra un error si falla la carga inicial', async () => {
        api.getProductos.mockRejectedValue(new Error('Falla de red'));

        const { result } = renderHook(() => useProductos());

        await waitFor(() => expect(result.current.cargando).toBe(false));

        expect(result.current.error).toBe('Falla de red');
    });

    it('crear() agrega el nuevo producto al estado local', async () => {
        api.getProductos.mockResolvedValue([]);
        api.crearProducto.mockResolvedValue({ id: 9, categoria: 'HOGAR' });

        const { result } = renderHook(() => useProductos());
        await waitFor(() => expect(result.current.cargando).toBe(false));

        await act(async () => {
            await result.current.crear({ categoria: 'HOGAR' });
        });

        expect(result.current.productos).toHaveLength(1);
        expect(result.current.productos[0].id).toBe(9);
    });

    it('actualizarEstado() refleja el nuevo estado en el item correspondiente', async () => {
        api.getProductos.mockResolvedValue([{ id: 1, categoria: 'ELECTRONICA', estado: 'DISPONIBLE' }]);
        api.actualizarEstadoProducto.mockResolvedValue({ id: 1, estado: 'RESERVADO' });

        const { result } = renderHook(() => useProductos());
        await waitFor(() => expect(result.current.cargando).toBe(false));

        await act(async () => {
            await result.current.actualizarEstado(1, 'RESERVADO');
        });

        expect(result.current.productos[0].estado).toBe('RESERVADO');
    });

    it('eliminar() remueve el producto del estado local', async () => {
        api.getProductos.mockResolvedValue([{ id: 1, categoria: 'ELECTRONICA' }]);
        api.eliminarProducto.mockResolvedValue({});

        const { result } = renderHook(() => useProductos());
        await waitFor(() => expect(result.current.cargando).toBe(false));

        await act(async () => {
            await result.current.eliminar(1);
        });

        expect(result.current.productos).toHaveLength(0);
    });
});
