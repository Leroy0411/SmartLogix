// src/App.jsx
import React, { useState } from 'react';
import Dashboard from './components/Dashboard';
import ProductoForm from './components/ProductoForm';

const navEstilo = {
    background: '#1a3c5e', color: '#fff', padding: '14px 24px',
    display: 'flex', alignItems: 'center', gap: '24px'
};
const btnEstilo = (activo) => ({
    background: activo ? '#e67e22' : 'transparent',
    color: '#fff', border: '1px solid #e67e22',
    padding: '6px 16px', borderRadius: '4px', cursor: 'pointer'
});

export default function App() {
    const [vista, setVista] = useState('dashboard');

    return (
        <div>
            <nav style={navEstilo}>
                <span style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>🚚 SmartLogix</span>
                <button style={btnEstilo(vista === 'dashboard')} onClick={() => setVista('dashboard')}>Dashboard</button>
                <button style={btnEstilo(vista === 'producto')}  onClick={() => setVista('producto')}>Registrar Producto</button>
            </nav>

            <main style={{ padding: '24px' }}>
                {vista === 'dashboard' && <Dashboard />}
                {vista === 'producto'  && <ProductoForm />}
            </main>
        </div>
    );
}
