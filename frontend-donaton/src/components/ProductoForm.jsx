// src/components/ProductoForm.jsx
// Formulario para registrar nuevos productos en inventario.
// Usa el hook useProductos (separación de responsabilidades).

import React, { useState } from 'react';
import { useProductos } from '../hooks/useProductos';

const CATEGORIAS = ['ELECTRONICA', 'ALIMENTOS', 'HOGAR', 'ROPA'];

const estilos = {
    form:    { background: '#fff', border: '1px solid #ddd', borderRadius: '8px', padding: '24px', maxWidth: '500px', boxShadow: '0 2px 6px rgba(0,0,0,.08)' },
    titulo:  { color: '#1a3c5e', marginBottom: '16px' },
    grupo:   { marginBottom: '14px' },
    label:   { display: 'block', color: '#555', marginBottom: '4px', fontSize: '0.9rem' },
    input:   { width: '100%', padding: '8px 10px', border: '1px solid #ccc', borderRadius: '4px', boxSizing: 'border-box' },
    boton:   { background: '#e67e22', color: '#fff', border: 'none', padding: '10px 24px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' },
    exito:   { background: '#d4edda', color: '#155724', padding: '10px', borderRadius: '4px', marginTop: '12px' },
    error:   { background: '#f8d7da', color: '#721c24', padding: '10px', borderRadius: '4px', marginTop: '12px' },
};

export default function ProductoForm() {
    const { crear } = useProductos();
    const [form, setForm] = useState({ categoria: 'ELECTRONICA', proveedor: '', stock: '', bodegaId: '', descripcion: '' });
    const [mensaje, setMensaje] = useState(null);
    const [tipoMensaje, setTipoMensaje] = useState('exito');

    const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const enviar = async (e) => {
        e.preventDefault();
        try {
            await crear({ ...form, stock: Number(form.stock), bodegaId: Number(form.bodegaId) });
            setMensaje('✅ Producto registrado exitosamente');
            setTipoMensaje('exito');
            setForm({ categoria: 'ELECTRONICA', proveedor: '', stock: '', bodegaId: '', descripcion: '' });
        } catch (err) {
            setMensaje('❌ Error: ' + (err.response?.data?.error || err.message));
            setTipoMensaje('error');
        }
    };

    return (
        <form style={estilos.form} onSubmit={enviar}>
            <h2 style={estilos.titulo}>📥 Registrar Producto</h2>

            <div style={estilos.grupo}>
                <label style={estilos.label}>Categoría</label>
                <select name="categoria" value={form.categoria} onChange={cambiar} style={estilos.input}>
                    {CATEGORIAS.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
            </div>

            <div style={estilos.grupo}>
                <label style={estilos.label}>Proveedor</label>
                <input name="proveedor" value={form.proveedor} onChange={cambiar} required style={estilos.input} placeholder="Ej: Distribuidora Central" />
            </div>

            <div style={estilos.grupo}>
                <label style={estilos.label}>Stock</label>
                <input name="stock" type="number" min="1" value={form.stock} onChange={cambiar} required style={estilos.input} />
            </div>

            <div style={estilos.grupo}>
                <label style={estilos.label}>ID Bodega</label>
                <input name="bodegaId" type="number" min="1" value={form.bodegaId} onChange={cambiar} required style={estilos.input} />
            </div>

            <div style={estilos.grupo}>
                <label style={estilos.label}>Descripción</label>
                <textarea name="descripcion" value={form.descripcion} onChange={cambiar} style={{ ...estilos.input, minHeight: '70px' }} />
            </div>

            <button type="submit" style={estilos.boton}>Registrar Producto</button>

            {mensaje && <div style={estilos[tipoMensaje]}>{mensaje}</div>}
        </form>
    );
}
