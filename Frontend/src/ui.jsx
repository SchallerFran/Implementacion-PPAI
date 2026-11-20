import { useCallback, useEffect, useRef, useState } from 'react'
import { AlertTriangle, CheckCircle2, Info, X } from 'lucide-react'
import { api } from './api'

// ---------- formato (es-AR, sin conversión de zona) ----------
export function fecha(iso) {
  if (!iso) return '—'
  const [d, t] = iso.split('T')
  const [a, m, dia] = d.split('-')
  return t ? `${dia}/${m}/${a} ${t.slice(0, 5)}` : `${dia}/${m}/${a}`
}
export const numero = (n) => new Intl.NumberFormat('es-AR').format(n)

// ---------- estados ----------
const ESTADOS = {
  Creado: ['Creado', 'base'],
  Registrada: ['Registrada', 'base'],
  Cerrado: ['Cerrado', 'firme'],
  EnRemito: ['En remito', 'firme'],
  EnBolsinSaliente: ['En bolsín saliente', 'firme'],
  Enviado: ['Enviado', 'transito'],
  EnBolsinEnviado: ['En bolsín enviado', 'transito'],
  Recibido: ['Recibido', 'ok'],
  RecibidaYAceptada: ['Recibida y aceptada', 'ok'],
  RecibidoYAceptado: ['Recibido y aceptado', 'ok'],
  RecibidoYAceptadoParcial: ['Recibido y aceptado parcial', 'alerta'],
  NoRecibida: ['No recibida', 'alerta'],
  ParaRedirigir: ['Para redirigir', 'alerta'],
  RecibidaYRechazada: ['Recibida y rechazada', 'baja'],
  Cancelado: ['Cancelado', 'baja'],
  DeBaja: ['De baja', 'baja'],
}
export const ESTADOS_BOLSIN = ['Creado', 'Cerrado', 'Enviado', 'Recibido', 'DeBaja']
export const ESTADOS_REMITO = ['Creado', 'Cancelado', 'EnBolsinSaliente', 'EnBolsinEnviado', 'RecibidoYAceptado', 'RecibidoYAceptadoParcial']
export const ESTADOS_DOCUMENTACION = ['Registrada', 'EnRemito', 'EnBolsinSaliente', 'EnBolsinEnviado', 'RecibidaYAceptada', 'NoRecibida', 'RecibidaYRechazada', 'ParaRedirigir', 'DeBaja']
export const nombreEstado = (e) => ESTADOS[e]?.[0] ?? e
const tono = (e) => ESTADOS[e]?.[1] ?? 'base'

export function Estado({ estado }) {
  return <span className={`estado estado-${tono(estado)}`}>{nombreEstado(estado)}</span>
}

// ---------- escudo de ruta: código de comisión médica ----------
export function Escudo({ codigo, nombre, grande }) {
  const [, sigla = codigo, nro = ''] = codigo?.match(/^CM-([A-Z]+)-(\d+)$/) ?? []
  return (
    <svg className={grande ? 'escudo escudo-grande' : 'escudo'} viewBox="0 0 48 46" role="img" aria-label={nombre ?? codigo}>
      <title>{nombre ? `${nombre} (${codigo})` : codigo}</title>
      <path d="M2 2h44v29L24 44 2 31z" className="escudo-fondo" />
      <path d="M5.5 5.5h37v23.4L24 39.7 5.5 28.9z" className="escudo-filete" />
      <text x="24" y="21" className="escudo-sigla">{sigla}</text>
      <text x="24" y="32" className="escudo-nro">{nro}</text>
    </svg>
  )
}

// ---------- tramo: origen → hitos de estado → destino ----------
const HITOS = {
  bolsin: ['Creado', 'Cerrado', 'Enviado', 'Recibido'],
  remito: ['Creado', 'EnBolsinSaliente', 'EnBolsinEnviado', 'RecibidoYAceptado'],
}
const ALIAS = { RecibidoYAceptadoParcial: 'RecibidoYAceptado' }

export function Tramo({ origen, destino, estado, tipo = 'bolsin', detallado, animado }) {
  const hitos = HITOS[tipo]
  const actual = hitos.indexOf(ALIAS[estado] ?? estado)
  const anulado = actual < 0
  const enTransito = tono(estado) === 'transito'
  const transito = enTransito ? ' en-transito' : ''
  return (
    <div className={`tramo${anulado ? ' tramo-anulado' : ''}${estado === 'RecibidoYAceptadoParcial' ? ' tramo-parcial' : ''}${detallado ? ' tramo-detallado' : ''}`}>
      <Escudo {...origen} grande={detallado} />
      <ol className="tramo-via" aria-label={`Recorrido: ${nombreEstado(estado)}`}>
        {hitos.map((h, i) => (
          <li key={h} className={i < actual ? 'hecho' : i === actual ? `actual${transito}` : ''} title={nombreEstado(h)}>
            <span className="hito" />
            {detallado && <span className="hito-nombre">{nombreEstado(h)}</span>}
            {animado && enTransito && i === actual && <span className="movil" aria-hidden="true" />}
          </li>
        ))}
      </ol>
      <Escudo {...destino} grande={detallado} />
    </div>
  )
}

/** Historial de cambios de estado como hoja de ruta vertical. */
export function Historial({ items }) {
  if (!items?.length) return null
  return (
    <ol className="historial">
      {[...items].reverse().map((c, i) => (
        <li key={i} className={c.fechaHoraFin ? '' : 'vigente'}>
          <Estado estado={c.estado} />
          <span className="historial-meta">{fecha(c.fechaHoraInicio)} · {c.responsable}</span>
        </li>
      ))}
    </ol>
  )
}

// ---------- avisos ----------
const ICONOS = { info: Info, error: AlertTriangle, ok: CheckCircle2, alerta: AlertTriangle }
export function Aviso({ tono = 'info', titulo, children, onCerrar }) {
  const Icono = ICONOS[tono]
  return (
    <div className={`aviso aviso-${tono}`} role={tono === 'error' ? 'alert' : 'status'}>
      <Icono size={18} aria-hidden="true" />
      <div>
        {titulo && <strong>{titulo}</strong>}
        {children && <div>{children}</div>}
      </div>
      {onCerrar && (
        <button className="btn-icono" onClick={onCerrar} aria-label="Cerrar aviso"><X size={16} /></button>
      )}
    </div>
  )
}

// ---------- diálogo modal nativo ----------
export function Dialogo({ abierto, titulo, children, acciones, onCerrar }) {
  const ref = useRef(null)
  useEffect(() => {
    const d = ref.current
    if (abierto && !d.open) d.showModal()
    if (!abierto && d.open) d.close()
  }, [abierto])
  return (
    <dialog ref={ref} className="dialogo" onCancel={(e) => { e.preventDefault(); onCerrar?.() }} aria-labelledby="dialogo-titulo">
      <h2 id="dialogo-titulo">{titulo}</h2>
      <div className="dialogo-cuerpo">{children}</div>
      <div className="dialogo-acciones">{acciones}</div>
    </dialog>
  )
}

// ---------- carga de datos ----------
/** GET de `path` (null = nada que consultar). Mientras los datos no son de la ruta vigente, `cargando` es true. */
export function useConsulta(path) {
  const [estado, setEstado] = useState({ datos: null, error: null, de: undefined, pidiendo: false })
  const vigente = useRef(path)
  vigente.current = path
  const recargar = useCallback(() => {
    if (!path) return Promise.resolve()
    setEstado((e) => ({ ...e, pidiendo: true }))
    const listo = (cambio) => { if (vigente.current === path) setEstado({ datos: null, error: null, ...cambio, de: path, pidiendo: false }) }
    return api(path).then((datos) => listo({ datos })).catch((error) => listo({ error }))
  }, [path])
  useEffect(() => { recargar() }, [recargar])
  const cargando = Boolean(path) && (estado.pidiendo || estado.de !== path)
  return { datos: path ? estado.datos : null, error: path ? estado.error : null, cargando, recargar }
}

/** Catálogo de comisiones médicas (sin sesión), cacheado en memoria. */
let cmCache
export function useComisiones() {
  const [cms, setCms] = useState(cmCache ?? [])
  useEffect(() => {
    if (cmCache) return
    api('/api/comisiones-medicas').then((d) => { cmCache = d; setCms(d) }).catch(() => {})
  }, [])
  return cms
}

/** Ejecuta una operación mostrando el detail del error; devuelve [ejecutar, ocupado, error, limpiarError]. */
export function useOperacion() {
  const [ocupado, setOcupado] = useState(false)
  const [error, setError] = useState(null)
  const ejecutar = useCallback(async (fn) => {
    setOcupado(true)
    setError(null)
    try { return await fn() } catch (e) { setError(e.message); return undefined } finally { setOcupado(false) }
  }, [])
  return [ejecutar, ocupado, error, () => setError(null)]
}

export function Cargando({ texto = 'Cargando…' }) {
  return <div className="cargando" role="status"><span className="spinner" aria-hidden="true" />{texto}</div>
}

export function Vacio({ titulo, children }) {
  return <div className="vacio"><strong>{titulo}</strong>{children && <p>{children}</p>}</div>
}

export function Campo({ etiqueta, ayuda, children, id }) {
  return (
    <label className="campo" htmlFor={id}>
      <span className="campo-etiqueta">{etiqueta}</span>
      {children}
      {ayuda && <span className="campo-ayuda">{ayuda}</span>}
    </label>
  )
}

export function SelectCM({ value, onChange, excluir, vacio = 'Todas', id }) {
  const cms = useComisiones()
  return (
    <select id={id} value={value} onChange={(e) => onChange(e.target.value)}>
      <option value="">{vacio}</option>
      {cms.filter((c) => c.codigo !== excluir).map((c) => <option key={c.codigo} value={c.codigo}>{c.nombre}</option>)}
    </select>
  )
}

export function SelectEstado({ value, onChange, estados, id }) {
  return (
    <select id={id} value={value} onChange={(e) => onChange(e.target.value)}>
      <option value="">Todos</option>
      {estados.map((e) => <option key={e} value={e}>{nombreEstado(e)}</option>)}
    </select>
  )
}

// ---------- panel lateral de trabajo ----------
export function Panel({ titulo, onCerrar, children }) {
  return (
    <aside className="panel">
      <div className="panel-cab">
        <h2>{titulo}</h2>
        <button className="btn-icono" onClick={onCerrar} aria-label="Cerrar panel"><X size={18} /></button>
      </div>
      {children}
    </aside>
  )
}

export function Confirmacion({ texto, boton, onConfirmar, ocupado, volver, peligro }) {
  return (
    <div className="subform">
      <p>{texto}</p>
      <div className="fila-botones">
        <button className={`btn ${peligro ? 'btn-peligro-lleno' : 'btn-primario'}`} onClick={onConfirmar} disabled={ocupado}>{ocupado ? 'Procesando…' : boton}</button>
        {volver}
      </div>
    </div>
  )
}
