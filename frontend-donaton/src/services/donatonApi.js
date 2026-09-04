import axios from 'axios'

const BFF_URL       = import.meta.env.VITE_BFF_URL       || 'http://localhost:8080'
const MS_INVENTARIO = import.meta.env.VITE_MS_INVENTARIO || 'http://localhost:8081'
const MS_PEDIDOS    = import.meta.env.VITE_MS_PEDIDOS    || 'http://localhost:8082'

export const getDashboard = () =>
    axios.get(`${BFF_URL}/bff/dashboard`).then(r => r.data)

export const getProductos = () =>
    axios.get(`${MS_INVENTARIO}/api/inventario`).then(r => r.data)

export const crearProducto = (datos) =>
    axios.post(`${MS_INVENTARIO}/api/inventario`, datos).then(r => r.data)

export const actualizarEstadoProducto = (id, estado) =>
    axios.put(`${MS_INVENTARIO}/api/inventario/${id}/estado?estado=${estado}`).then(r => r.data)

export const eliminarProducto = (id) =>
    axios.delete(`${MS_INVENTARIO}/api/inventario/${id}`).then(r => r.data)

export const getCentros = () =>
    axios.get(`${MS_PEDIDOS}/api/pedidos/centros-distribucion`).then(r => r.data)

export const crearCentro = (datos) =>
    axios.post(`${MS_PEDIDOS}/api/pedidos/centros-distribucion`, datos).then(r => r.data)

export const getPedidos = () =>
    axios.get(`${MS_PEDIDOS}/api/pedidos`).then(r => r.data)

export const despacharPedido = (id) =>
    axios.put(`${MS_PEDIDOS}/api/pedidos/${id}/despachar`).then(r => r.data)

export const confirmarEntrega = (id, obs = '') =>
    axios.put(`${MS_PEDIDOS}/api/pedidos/${id}/entregar?observaciones=${obs}`).then(r => r.data)
