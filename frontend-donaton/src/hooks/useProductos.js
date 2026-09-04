// src/hooks/useProductos.js
//
// Hook personalizado que encapsula la lógica de estado y fetching
// de productos de inventario. Aplica separación de responsabilidades:
// los componentes no gestionan estado asíncrono directamente.

import { useState, useEffect, useCallback } from 'react';
import { getProductos, crearProducto, actualizarEstadoProducto, eliminarProducto } from '../services/donatonApi';

export function useProductos() {
    const [productos, setProductos] = useState([]);
    const [cargando,  setCargando]  = useState(false);
    const [error,     setError]     = useState(null);

    const cargar = useCallback(async () => {
        setCargando(true);
        setError(null);
        try {
            const data = await getProductos();
            setProductos(data);
        } catch (err) {
            setError(err.message || 'Error al cargar productos');
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => { cargar(); }, [cargar]);

    const crear = async (datos) => {
        const nuevo = await crearProducto(datos);
        setProductos(prev => [...prev, nuevo]);
        return nuevo;
    };

    const actualizarEstado = async (id, estado) => {
        const actualizado = await actualizarEstadoProducto(id, estado);
        setProductos(prev =>
            prev.map(p => p.id === id ? { ...p, estado } : p)
        );
        return actualizado;
    };

    const eliminar = async (id) => {
        await eliminarProducto(id);
        setProductos(prev => prev.filter(p => p.id !== id));
    };

    return { productos, cargando, error, cargar, crear, actualizarEstado, eliminar };
}
