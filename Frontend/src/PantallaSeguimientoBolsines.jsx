import { useEffect, useState } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { MapContainer, Marker, TileLayer, useMap } from 'react-leaflet'
import { Mail, MapPinOff, RefreshCw, Search, X } from 'lucide-react'
import { api, query } from './api'
import { Aviso, Campo, Cargando, Dialogo, Escudo, fecha, SelectCM, Tramo } from './ui'

const DISPOSITIVOS = ['XTR-4500L', 'NavTrack QX-7A', 'GeoPulse MTR-900']
const ARGENTINA = [-38.4, -63.6]

/**
 * Boundary del CU 36 Consultar seguimiento de bolsines.
 * Los métodos llevan los nombres del diagrama de secuencia; el estado del caso de uso vive en el backend por sesión.
 */
export default function PantallaSeguimientoBolsines({ sesion, onCancelar }) {
  // Atributos de la pantalla (diseño)
  const numeroBolsinLbl = 'Número de bolsín'
  const numeroPrecintoLbl = 'Número de precinto'
  const [numeroBolsinTxt, setNumeroBolsinTxt] = useState('')
  const [numeroPrecintoTxt, setNumeroPrecintoTxt] = useState('')

  const [codigoCMDestino, setCodigoCMDestino] = useState('')
  const [dispositivoGPS, setDispositivoGPS] = useState(DISPOSITIVOS[0])
  const [fase, setFase] = useState('cargando') // cargando | sinBolsines | error | seleccion | confirmacion | finalizado
  const [consulta, setConsulta] = useState(null)
  const [enMapa, setEnMapa] = useState([])
  const [avisoFiltro, setAvisoFiltro] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [seleccion, setSeleccion] = useState(null)
  const [resultado, setResultado] = useState(null)
  const [resaltado, setResaltado] = useState(null)
  const [ocupado, setOcupado] = useState(false)

  useEffect(() => { habilitarVentana(DISPOSITIVOS[0]) }, [])

  // Pasos 1-6: consulta la ubicación de los bolsines Enviado desde la CM del usuario.
  async function habilitarVentana(dispositivo) {
    setFase('cargando')
    setAviso(null)
    setAvisoFiltro(null)
    setResultado(null)
    setSeleccion(null)
    setNumeroBolsinTxt('')
    try {
      const r = await api(`/api/seguimiento-bolsines${query({ dispositivoGPS: dispositivo })}`, { method: 'POST' })
      mostrarBolsinEnMapaEnviadoDesdeCMOrigen(r)
      pedirSeleccionBolsinEnMapa()
    } catch (e) {
      setAviso(e.message)
      setFase(e.status === 404 ? 'sinBolsines' : 'error') // 404 = A1
    }
  }

  function mostrarBolsinEnMapaEnviadoDesdeCMOrigen(r) {
    setConsulta(r)
    setEnMapa(r.bolsines)
  }

  function pedirSeleccionBolsinEnMapa() {
    setFase('seleccion')
  }

  // Paso 6: filtro por número de precinto y/o CM destino.
  async function filtrarBolsines(e) {
    e.preventDefault()
    setAvisoFiltro(null)
    const numeroPrecinto = numeroPrecintoTxt.trim()
    if (!numeroPrecinto && !codigoCMDestino) return setEnMapa(consulta.bolsines)
    try {
      setEnMapa(await api(`/api/seguimiento-bolsines/bolsines${query({ numeroPrecinto, codigoCMDestino })}`))
    } catch (err) {
      setAvisoFiltro(err.message) // 404 = A2
      setEnMapa([])
    }
  }

  function limpiarFiltro() {
    setNumeroPrecintoTxt('')
    setCodigoCMDestino('')
    setAvisoFiltro(null)
    setEnMapa(consulta.bolsines)
  }

  // Pasos 7-8
  async function tomarSeleccionBolsinEnMapa(numeroBolsin) {
    if (fase !== 'seleccion' || ocupado) return
    setNumeroBolsinTxt(String(numeroBolsin))
    setAviso(null)
    setOcupado(true)
    try {
      pedirConfirmacionDeMailAGerenteCMDestino(
        await api('/api/seguimiento-bolsines/seleccion', { method: 'POST', body: { numeroBolsin } }))
    } catch (e) {
      setAviso(e.message)
    } finally {
      setOcupado(false)
    }
  }

  function pedirConfirmacionDeMailAGerenteCMDestino(r) {
    setSeleccion(r)
    setFase('confirmacion')
  }

  // Pasos 9-11 (Sí) o A5 (No)
  async function tomarConfirmacionMailCMGerenteDestino(enviarMail) {
    setOcupado(true)
    try {
      setResultado(await api('/api/seguimiento-bolsines/confirmacion', { method: 'POST', body: { enviarMail } }))
      setFase('finalizado')
    } catch (e) {
      setAviso(e.message)
      setFase('seleccion')
    } finally {
      setOcupado(false)
      setSeleccion(null)
    }
  }

  // A4
  async function cancelar() {
    await api('/api/seguimiento-bolsines', { method: 'DELETE' }).catch(() => {})
    onCancelar()
  }

  const cm = consulta ? { codigo: consulta.codigoCM, nombre: consulta.nombreCM } : sesion.comisionMedica
  const sinUbicacion = consulta?.bolsinesSinUbicacion ?? []
  const filtrando = Boolean(numeroPrecintoTxt || codigoCMDestino || avisoFiltro)
  const operable = fase === 'seleccion' || fase === 'confirmacion'

  return (
    <div className="seguimiento">
      <aside className="seg-panel" aria-label="Consulta de seguimiento">
        <div className="seg-cab">
          <h1>Seguimiento de bolsines</h1>
          {fase !== 'finalizado' && (
            <button className="btn btn-secundario btn-chico" onClick={cancelar}><X size={16} aria-hidden="true" /> Cancelar</button>
          )}
        </div>

        <div className="seg-origen">
          <Escudo codigo={cm.codigo} nombre={cm.nombre} grande />
          <div>
            <span className="texto-sec">Comisión médica origen</span>
            <strong>{cm.nombre}</strong>
            {consulta && <span className="texto-sec">Consultado el {fecha(consulta.fechaActual)}</span>}
          </div>
        </div>

        <Campo etiqueta="Modelo de GPS Tracker" id="gps">
          <select id="gps" value={dispositivoGPS} disabled={fase === 'cargando' || ocupado}
            onChange={(e) => { setDispositivoGPS(e.target.value); habilitarVentana(e.target.value) }}>
            {DISPOSITIVOS.map((d) => <option key={d}>{d}</option>)}
          </select>
        </Campo>

        {fase === 'cargando' && <Cargando texto="Consultando la ubicación al GPS Tracker…" />}
        {fase === 'sinBolsines' && <Aviso tono="info" titulo="No hay bolsines en tránsito">{aviso}</Aviso>}
        {fase === 'error' && (
          <>
            <Aviso tono="error">{aviso}</Aviso>
            <button className="btn btn-secundario" onClick={() => habilitarVentana(dispositivoGPS)}><RefreshCw size={16} aria-hidden="true" /> Reintentar</button>
          </>
        )}

        {operable && (
          <>
            <form className="seg-filtro" onSubmit={filtrarBolsines}>
              <Campo etiqueta={numeroPrecintoLbl} id="numeroPrecintoTxt">
                <input id="numeroPrecintoTxt" value={numeroPrecintoTxt} onChange={(e) => setNumeroPrecintoTxt(e.target.value)} placeholder="P-500101" />
              </Campo>
              <Campo etiqueta="CM destino" id="cmDestino">
                <SelectCM id="cmDestino" value={codigoCMDestino} onChange={setCodigoCMDestino} excluir={cm.codigo} />
              </Campo>
              <div className="fila-botones">
                <button className="btn btn-primario btn-chico"><Search size={16} aria-hidden="true" /> Filtrar</button>
                {filtrando && <button type="button" className="btn btn-texto btn-chico" onClick={limpiarFiltro}>Ver todos</button>}
              </div>
            </form>

            {aviso && <Aviso tono="error" onCerrar={() => setAviso(null)}>{aviso}</Aviso>}
            {avisoFiltro && <Aviso tono="info" titulo="Sin coincidencias">{avisoFiltro}</Aviso>}

            {enMapa.length > 0 && (
              <section>
                <h2 className="subtitulo">En tránsito <span className="contador">{enMapa.length}</span></h2>
                <p className="texto-sec texto-chico">Seleccioná un bolsín en el mapa o en la lista.</p>
                <ul className="lista-tramos">
                  {enMapa.map((m) => (
                    <li key={m.numeroBolsin}>
                      <button className={`tramo-item${resaltado === m.numeroBolsin ? ' resaltado' : ''}${numeroBolsinTxt === String(m.numeroBolsin) ? ' elegido' : ''}`}
                        onClick={() => tomarSeleccionBolsinEnMapa(m.numeroBolsin)} disabled={ocupado}
                        onMouseEnter={() => setResaltado(m.numeroBolsin)} onMouseLeave={() => setResaltado(null)}
                        onFocus={() => setResaltado(m.numeroBolsin)} onBlur={() => setResaltado(null)}>
                        <span className="tramo-item-cab">
                          <strong>Bolsín {m.numeroBolsin}</strong>
                          <span className="dato-mono">{m.numeroPrecinto}</span>
                        </span>
                        <Tramo origen={cm} destino={{ codigo: m.codigoCMDestino, nombre: m.nombreCMDestino }} estado="Enviado"
                          animado={resaltado === m.numeroBolsin || numeroBolsinTxt === String(m.numeroBolsin)} />
                        <span className="texto-sec texto-chico">Hacia {m.nombreCMDestino} · actualizado {fecha(m.fechaHoraUltimaActualizacion)}</span>
                      </button>
                    </li>
                  ))}
                </ul>
              </section>
            )}

            {sinUbicacion.length > 0 && (
              <section className="sin-ubicacion">
                <h2 className="subtitulo"><MapPinOff size={16} aria-hidden="true" /> Sin ubicación del GPS <span className="contador contador-alerta">{sinUbicacion.length}</span></h2>
                <p className="texto-sec texto-chico">
                  El GPS no informó la ubicación de {sinUbicacion.length === 1 ? '1 bolsín' : `${sinUbicacion.length} bolsines`}; no se muestra{sinUbicacion.length === 1 ? '' : 'n'} en el mapa.
                </p>
                <ul>
                  {sinUbicacion.map((b) => (
                    <li key={b.numeroBolsin}>
                      <strong>Bolsín {b.numeroBolsin}</strong> <span className="dato-mono">{b.numeroPrecinto}</span>
                      <span className="texto-sec texto-chico">{b.motivo}</span>
                    </li>
                  ))}
                </ul>
              </section>
            )}
          </>
        )}

        {fase === 'finalizado' && resultado && (
          <section className="seg-resultado">
            <Aviso tono={resultado.mailEnviado ? 'ok' : 'info'} titulo={resultado.mailEnviado ? 'Ubicación notificada' : 'Consulta finalizada'}>
              {resultado.mensaje}
            </Aviso>
            {resultado.notificacion && (
              <dl className="datos">
                <dt>Destinatario</dt><dd>{resultado.notificacion.destinatario}</dd>
                <dt>{numeroBolsinLbl}</dt><dd>{resultado.notificacion.numeroBolsin}</dd>
                <dt>Última actualización</dt><dd>{fecha(resultado.notificacion.fechaHoraUltimaActualizacion)}</dd>
                <dt>Coordenadas</dt><dd className="dato-mono">{resultado.notificacion.latitud.toFixed(4)}, {resultado.notificacion.longitud.toFixed(4)}</dd>
              </dl>
            )}
            <div className="fila-botones">
              <button className="btn btn-primario" onClick={() => habilitarVentana(dispositivoGPS)}><RefreshCw size={16} aria-hidden="true" /> Nueva consulta</button>
              <button className="btn btn-texto" onClick={onCancelar}>Ir a Bolsines</button>
            </div>
          </section>
        )}
      </aside>

      <section className="seg-mapa" aria-label="Mapa de bolsines en tránsito">
        <Mapa marcadores={operable || fase === 'finalizado' ? enMapa : []} resaltado={resaltado} setResaltado={setResaltado}
          onSeleccionar={tomarSeleccionBolsinEnMapa} seleccionable={fase === 'seleccion'} origen={cm} />
        {fase === 'sinBolsines' && (
          <div className="mapa-nota"><strong>Sin bolsines en tránsito</strong><span>No hay bolsines enviados desde {cm.nombre} para mostrar en el mapa.</span></div>
        )}
      </section>

      <Dialogo abierto={fase === 'confirmacion'} titulo="Notificar ubicación al gerente"
        onCerrar={() => { if (!ocupado) { setSeleccion(null); setFase('seleccion') } }}
        acciones={<>
          <button className="btn btn-secundario" disabled={ocupado} onClick={() => tomarConfirmacionMailCMGerenteDestino(false)}>No enviar</button>
          <button className="btn btn-primario" disabled={ocupado} onClick={() => tomarConfirmacionMailCMGerenteDestino(true)} autoFocus>
            {ocupado ? <><span className="spinner" aria-hidden="true" /> Enviando…</> : <><Mail size={16} aria-hidden="true" /> Sí, enviar correo</>}
          </button>
        </>}>
        {seleccion && (
          <>
            <p className="pregunta">{seleccion.pregunta}</p>
            <Tramo origen={cm} destino={{ codigo: seleccion.bolsin.codigoCMDestino, nombre: seleccion.bolsin.nombreCMDestino }} estado="Enviado" detallado />
            <dl className="datos">
              <dt>{numeroBolsinLbl}</dt><dd>{seleccion.bolsin.numeroBolsin}</dd>
              <dt>{numeroPrecintoLbl}</dt><dd className="dato-mono">{seleccion.bolsin.numeroPrecinto}</dd>
              <dt>Última actualización</dt><dd>{fecha(seleccion.bolsin.fechaHoraUltimaActualizacion)}</dd>
            </dl>
          </>
        )}
      </Dialogo>
    </div>
  )
}

// Mapa con Leaflet + OpenStreetMap (decisión del equipo); el backend solo entrega coordenadas.
const pin = (numeroBolsin, activo, seleccionable) => L.divIcon({
  className: '',
  html: `<div class="pin${activo ? ' pin-activo' : ''}${seleccionable ? '' : ' pin-inactivo'}"><span>${numeroBolsin}</span></div>`,
  iconSize: [40, 48],
  iconAnchor: [20, 46],
})

function Mapa({ marcadores, resaltado, setResaltado, onSeleccionar, seleccionable, origen }) {
  const info = marcadores.find((m) => m.numeroBolsin === resaltado)
  return (
    <>
      <MapContainer className="mapa" center={ARGENTINA} zoom={5} minZoom={3} zoomControl={false} attributionControl>
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' />
        {marcadores.map((m) => (
          <Marker key={m.numeroBolsin} position={[m.latitud, m.longitud]} title={`Bolsín ${m.numeroBolsin}`}
            icon={pin(m.numeroBolsin, resaltado === m.numeroBolsin, seleccionable)}
            zIndexOffset={resaltado === m.numeroBolsin ? 1000 : 0} keyboard
            eventHandlers={{
              click: () => onSeleccionar(m.numeroBolsin),
              mouseover: () => setResaltado(m.numeroBolsin),
              mouseout: () => setResaltado(null),
            }} />
        ))}
        <Encuadre marcadores={marcadores} />
        <ZoomAbajo />
      </MapContainer>
      {info && (
        <div className="info-mapa" role="status">
          <strong>Bolsín {info.numeroBolsin}</strong>
          <Tramo origen={origen} destino={{ codigo: info.codigoCMDestino, nombre: info.nombreCMDestino }} estado="Enviado" animado />
          <span>Precinto <b className="dato-mono">{info.numeroPrecinto}</b></span>
          <span>Destino {info.nombreCMDestino}</span>
          <span>Actualizado {fecha(info.fechaHoraUltimaActualizacion)}</span>
          <span className="texto-chico">{info.latitud.toFixed(4)}, {info.longitud.toFixed(4)}</span>
          {seleccionable && <em>Hacé clic en el marcador para seleccionarlo</em>}
        </div>
      )}
    </>
  )
}

function ZoomAbajo() {
  const map = useMap()
  useEffect(() => {
    const z = L.control.zoom({ position: 'bottomright', zoomInTitle: 'Acercar', zoomOutTitle: 'Alejar' }).addTo(map)
    return () => z.remove()
  }, [map])
  return null
}

/** Ajusta el mapa para que se vean todos los marcadores. */
function Encuadre({ marcadores }) {
  const map = useMap()
  useEffect(() => {
    if (!marcadores.length) return
    if (marcadores.length === 1) map.setView([marcadores[0].latitud, marcadores[0].longitud], 7)
    else map.fitBounds(marcadores.map((m) => [m.latitud, m.longitud]), { padding: [70, 70], maxZoom: 7 })
  }, [map, marcadores])
  return null
}
