// Único cliente HTTP: agrega X-Sesion-Id, traduce problem+json a ApiError y maneja el 401 en un solo lugar.
const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8081'

let sesionId = sessionStorage.getItem('sesionId')
let alPerder = () => {}

export function guardarSesion(id) {
  sesionId = id
  if (id) sessionStorage.setItem('sesionId', id)
  else sessionStorage.removeItem('sesionId')
}
export const haySesionGuardada = () => Boolean(sesionId)
export const alPerderSesion = (fn) => { alPerder = fn }

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

export async function api(path, { method = 'GET', body } = {}) {
  const headers = {}
  if (sesionId) headers['X-Sesion-Id'] = sesionId
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let res
  try {
    res = await fetch(BASE + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) })
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Verificá que el backend esté en ejecución.')
  }
  if (res.status === 204) return null
  const data = await res.json().catch(() => null)
  if (res.ok) return data

  if (res.status === 401 && !(path === '/api/sesiones' && method === 'POST')) {
    guardarSesion(null)
    alPerder()
  }
  const detalle = res.status >= 500
    ? 'Ocurrió un error inesperado en el servidor. Intentá de nuevo en unos minutos.'
    : data?.detail ?? `El servidor respondió con el código ${res.status}.`
  throw new ApiError(res.status, detalle)
}

/** Arma el query string omitiendo los valores vacíos. */
export function query(params) {
  const s = new URLSearchParams(Object.entries(params).filter(([, v]) => v !== '' && v != null)).toString()
  return s ? `?${s}` : ''
}
