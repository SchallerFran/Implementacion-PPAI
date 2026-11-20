import { useState } from 'react'
import { Lock, LockOpen, PackageCheck, PackagePlus, Pencil, Search, Trash2, Truck } from 'lucide-react'
import { api, query } from './api'
import {
  Aviso, Campo, Cargando, Confirmacion, Estado, ESTADOS_BOLSIN, fecha, Historial, numero, SelectCM, SelectEstado, Tramo,
  Panel, useConsulta, useOperacion, Vacio,
} from './ui'

const cmOrigen = (b) => ({ codigo: b.codigoCMOrigen, nombre: b.nombreCMOrigen })
const cmDestino = (b) => ({ codigo: b.codigoCMDestino, nombre: b.nombreCMDestino })

/** CU 19 Generar · 20 Modificar · 21 Eliminar · 22 Consultar · 27 Retiro · 28 Recepción · 39 Cerrar · 40 Reabrir. */
export default function Bolsines({ sesion, esEB }) {
  const miCM = sesion.comisionMedica.codigo
  const [estado, setEstado] = useState('')
  const [precintoTxt, setPrecintoTxt] = useState('')
  const [precinto, setPrecinto] = useState('')
  const lista = useConsulta(`/api/bolsines${query({ estado, numeroPrecinto: precinto })}`)
  const [panel, setPanel] = useState(null)
  const [actualizado, setActualizado] = useState(null)
  const [exito, setExito] = useState(null)

  function alActualizar(b, mensaje) {
    setExito(mensaje)
    setActualizado(b.numeroBolsin)
    setPanel({ modo: 'detalle', bolsin: b })
    lista.recargar()
  }

  return (
    <div className="pagina">
      <header className="pagina-cab">
        <div>
          <h1>Bolsines</h1>
          <p className="texto-sec">Bolsines enviados o recibidos por {sesion.comisionMedica.nombre}.</p>
        </div>
        {esEB && (
          <button className="btn btn-primario" onClick={() => { setExito(null); setPanel({ modo: 'nuevo' }) }}>
            <PackagePlus size={17} aria-hidden="true" /> Generar bolsín
          </button>
        )}
      </header>

      <form className="filtros" onSubmit={(e) => { e.preventDefault(); setPrecinto(precintoTxt.trim()) }}>
        <Campo etiqueta="Estado" id="f-estado"><SelectEstado id="f-estado" value={estado} onChange={setEstado} estados={ESTADOS_BOLSIN} /></Campo>
        <Campo etiqueta="Número de precinto" id="f-precinto">
          <input id="f-precinto" value={precintoTxt} onChange={(e) => setPrecintoTxt(e.target.value)} placeholder="P-500101" />
        </Campo>
        <button className="btn btn-secundario"><Search size={16} aria-hidden="true" /> Buscar</button>
        {(estado || precinto) && (
          <button type="button" className="btn btn-texto" onClick={() => { setEstado(''); setPrecintoTxt(''); setPrecinto('') }}>Quitar filtros</button>
        )}
      </form>

      {exito && <Aviso tono="ok" onCerrar={() => setExito(null)}>{exito}</Aviso>}

      <div className={panel ? 'trabajo con-panel' : 'trabajo'}>
        <section className="tabla-wrap">
          {lista.cargando && !lista.datos ? <Cargando /> : lista.error ? <Aviso tono="error">{lista.error.message}</Aviso> : !lista.datos.length ? (
            <Vacio titulo="No hay bolsines para mostrar">{estado || precinto ? 'Probá quitando los filtros.' : 'Todavía no se generaron bolsines en esta comisión médica.'}</Vacio>
          ) : (
            <table className="tabla">
              <thead>
                <tr><th>Bolsín</th><th>Recorrido</th><th>Precinto</th><th className="num">Peso</th><th>Estado</th></tr>
              </thead>
              <tbody>
                {lista.datos.map((b) => (
                  <tr key={b.numeroBolsin}
                    className={`${panel?.bolsin?.numeroBolsin === b.numeroBolsin ? 'fila-activa' : ''} ${actualizado === b.numeroBolsin ? 'fila-actualizada' : ''} ${b.estado === 'DeBaja' ? 'fila-anulada' : ''}`}
                    onClick={() => setPanel({ modo: 'detalle', bolsin: b })}>
                    <td>
                      <button className="link-fila" onClick={(e) => { e.stopPropagation(); setPanel({ modo: 'detalle', bolsin: b }) }}>N.º {b.numeroBolsin}</button>
                      <span className="texto-sec texto-chico">{fecha(b.fecha)}</span>
                    </td>
                    <td><Tramo origen={cmOrigen(b)} destino={cmDestino(b)} estado={b.estado} /></td>
                    <td className="dato-mono">{b.numeroPrecinto ?? <span className="texto-sec">Sin precinto</span>}</td>
                    <td className="num">{b.peso != null ? `${numero(b.peso)} g` : '—'}</td>
                    <td><Estado estado={b.estado} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        {panel?.modo === 'nuevo' && (
          <NuevoBolsin miCM={miCM} onCerrar={() => setPanel(null)}
            onCreado={(b) => alActualizar(b, `Se generó el bolsín ${b.numeroBolsin} hacia ${b.nombreCMDestino}.`)} />
        )}
        {panel?.modo === 'detalle' && (
          <DetalleBolsin key={panel.bolsin.numeroBolsin} bolsin={panel.bolsin} miCM={miCM} esEB={esEB}
            onCerrar={() => setPanel(null)} onActualizado={alActualizar} />
        )}
      </div>
    </div>
  )
}

function DetalleBolsin({ bolsin: b, miCM, esEB, onCerrar, onActualizado }) {
  const [modo, setModo] = useState('ver')
  const [ejecutar, ocupado, error] = useOperacion()
  const [nuevoPrecinto, setNuevoPrecinto] = useState('')
  const [noRecibidas, setNoRecibidas] = useState([])
  const [remitosSel, setRemitosSel] = useState(b.remitos.map((r) => r.numero))

  const esOrigen = b.codigoCMOrigen === miCM
  const esDestino = b.codigoCMDestino === miCM
  const acciones = !esEB ? [] : [
    ...(esOrigen && b.estado === 'Creado' ? [['modificar', 'Modificar remitos', Pencil], ['precintar', 'Cerrar con precinto', Lock]] : []),
    ...(esOrigen && b.estado === 'Cerrado' ? [['retiro', 'Registrar retiro', Truck], ['abrir', 'Reabrir', LockOpen]] : []),
    ...(esOrigen && (b.estado === 'Creado' || b.estado === 'Cerrado') ? [['eliminar', 'Eliminar', Trash2]] : []),
    ...(esDestino && b.estado === 'Enviado' ? [['recepcion', 'Registrar recepción', PackageCheck]] : []),
  ]

  const operar = (ruta, metodo, body, mensaje) => ejecutar(async () => {
    const r = await api(`/api/bolsines/${b.numeroBolsin}${ruta}`, { method: metodo, body })
    onActualizado(r, mensaje(r))
    setModo('ver')
  })

  const documentacion = b.remitos.flatMap((r) => r.detalles.map((d) => ({ ...d.documentacion, remito: r.numero })))
  const volver = <button type="button" className="btn btn-texto" onClick={() => setModo('ver')} disabled={ocupado}>Volver</button>

  return (
    <Panel titulo={`Bolsín ${b.numeroBolsin}`} onCerrar={onCerrar}>
      <div className="panel-estado"><Estado estado={b.estado} /></div>
      <Tramo origen={cmOrigen(b)} destino={cmDestino(b)} estado={b.estado} detallado />
      <dl className="datos">
        <dt>Origen</dt><dd>{b.nombreCMOrigen}</dd>
        <dt>Destino</dt><dd>{b.nombreCMDestino}</dd>
        <dt>Precinto</dt><dd className="dato-mono">{b.numeroPrecinto ?? 'Sin precinto'}</dd>
        <dt>Peso</dt><dd>{b.peso != null ? `${numero(b.peso)} g` : '—'}</dd>
        <dt>Fecha</dt><dd>{fecha(b.fecha)}</dd>
      </dl>

      {error && <Aviso tono="error">{error}</Aviso>}

      {modo === 'ver' && acciones.length > 0 && (
        <div className="acciones">
          {acciones.map(([m, texto, Icono]) => (
            <button key={m} className={`btn ${m === 'eliminar' ? 'btn-peligro' : 'btn-secundario'} btn-chico`} onClick={() => setModo(m)}>
              <Icono size={16} aria-hidden="true" /> {texto}
            </button>
          ))}
        </div>
      )}

      {modo === 'precintar' && (
        <form className="subform" onSubmit={(e) => { e.preventDefault(); operar('/precintar', 'POST', { numeroPrecinto: nuevoPrecinto.trim() }, (r) => `El bolsín ${r.numeroBolsin} quedó cerrado con el precinto ${r.numeroPrecinto}.`) }}>
          <Campo etiqueta="Número de precinto" id="precinto" ayuda="El número impreso en el precinto colocado.">
            <input id="precinto" value={nuevoPrecinto} onChange={(e) => setNuevoPrecinto(e.target.value)} required autoFocus />
          </Campo>
          <div className="fila-botones"><button className="btn btn-primario" disabled={ocupado}>Cerrar bolsín</button>{volver}</div>
        </form>
      )}

      {modo === 'modificar' && (
        <ModificarRemitos bolsin={b} miCM={miCM} seleccion={remitosSel} setSeleccion={setRemitosSel} ocupado={ocupado} volver={volver}
          onConfirmar={() => operar('', 'PUT', { remitos: remitosSel }, (r) => `Se actualizaron los remitos del bolsín ${r.numeroBolsin}.`)} />
      )}

      {modo === 'recepcion' && (
        <form className="subform" onSubmit={(e) => { e.preventDefault(); operar('/recepcion', 'POST', { documentacionesNoRecibidas: noRecibidas }, (r) => `Se registró la recepción del bolsín ${r.numeroBolsin}.`) }}>
          <p className="texto-sec">Marcá la documentación que <strong>no</strong> llegó. Si el contenido es igual al registrado, confirmá sin marcar nada.</p>
          <ul className="lista-check">
            {documentacion.map((d) => (
              <li key={d.numero}>
                <label>
                  <input type="checkbox" checked={noRecibidas.includes(d.numero)}
                    onChange={(e) => setNoRecibidas(e.target.checked ? [...noRecibidas, d.numero] : noRecibidas.filter((n) => n !== d.numero))} />
                  <span><strong>{d.asunto}</strong><span className="texto-sec texto-chico">{d.tipoDocumento} · Remito {d.remito} · Doc. {d.numero}</span></span>
                </label>
              </li>
            ))}
          </ul>
          <div className="fila-botones">
            <button className="btn btn-primario" disabled={ocupado}>{noRecibidas.length ? `Registrar con ${noRecibidas.length} no recibida${noRecibidas.length > 1 ? 's' : ''}` : 'Contenido igual al registrado'}</button>
            {volver}
          </div>
        </form>
      )}

      {modo === 'retiro' && (
        <Confirmacion texto={`El correo retira el bolsín ${b.numeroBolsin}: pasa a Enviado junto con sus remitos y documentación.`}
          boton="Registrar retiro" ocupado={ocupado} volver={volver}
          onConfirmar={() => operar('/retiro', 'POST', undefined, (r) => `El bolsín ${r.numeroBolsin} quedó en estado Enviado.`)} />
      )}
      {modo === 'abrir' && (
        <Confirmacion texto={`Se quita el precinto ${b.numeroPrecinto} y el bolsín vuelve a Creado. Para cerrarlo de nuevo vas a necesitar otro precinto.`}
          boton="Reabrir bolsín" ocupado={ocupado} volver={volver}
          onConfirmar={() => operar('/abrir', 'POST', undefined, (r) => `El bolsín ${r.numeroBolsin} se reabrió.`)} />
      )}
      {modo === 'eliminar' && (
        <Confirmacion peligro texto={`El bolsín ${b.numeroBolsin} pasa a De baja; sus remitos vuelven a Creado y la documentación a En remito.`}
          boton="Eliminar bolsín" ocupado={ocupado} volver={volver}
          onConfirmar={() => operar('', 'DELETE', undefined, (r) => `El bolsín ${r.numeroBolsin} se dio de baja.`)} />
      )}

      <h3 className="subtitulo">Remitos <span className="contador">{b.remitos.length}</span></h3>
      <ul className="lista-remitos">
        {b.remitos.map((r) => (
          <li key={r.numero}>
            <div className="lista-remitos-cab"><strong>Remito {r.numero}</strong><Estado estado={r.estado} /></div>
            <ul>
              {r.detalles.map(({ documentacion: d, areaCMCDestino }) => (
                <li key={d.numero}>
                  <span>{d.asunto}</span>
                  <span className="texto-sec texto-chico">{d.tipoDocumento}{areaCMCDestino ? ` · Área ${areaCMCDestino}` : ''} · <Estado estado={d.estado} /></span>
                </li>
              ))}
            </ul>
          </li>
        ))}
      </ul>

      <h3 className="subtitulo">Historial</h3>
      <Historial items={b.historial} />
    </Panel>
  )
}

/** Remitos en Creado de la CM del usuario hacia el destino dado. */
function useRemitosDisponibles(miCM, destino) {
  const r = useConsulta(destino ? `/api/remitos${query({ estado: 'Creado', codigoCMDestino: destino })}` : null)
  return { ...r, datos: r.datos?.filter((x) => x.codigoCMOrigen === miCM) }
}

function ListaRemitos({ remitos, seleccion, setSeleccion }) {
  return (
    <ul className="lista-check">
      {remitos.map((r) => (
        <li key={r.numero}>
          <label>
            <input type="checkbox" checked={seleccion.includes(r.numero)}
              onChange={(e) => setSeleccion(e.target.checked ? [...seleccion, r.numero] : seleccion.filter((n) => n !== r.numero))} />
            <span>
              <strong>Remito {r.numero}</strong>
              <span className="texto-sec texto-chico">
                {fecha(r.fecha)} · {r.detalles.length} documentación{r.detalles.length === 1 ? '' : 'es'}: {[...new Set(r.detalles.map((d) => d.documentacion.tipoDocumento))].join(', ')}
              </span>
            </span>
          </label>
        </li>
      ))}
    </ul>
  )
}

function ModificarRemitos({ bolsin, miCM, seleccion, setSeleccion, onConfirmar, ocupado, volver }) {
  const disponibles = useRemitosDisponibles(miCM, bolsin.codigoCMDestino)
  const todos = [...bolsin.remitos, ...(disponibles.datos ?? [])]
  return (
    <div className="subform">
      <p className="texto-sec">Destildá los remitos que querés quitar o tildá remitos en Creado hacia {bolsin.nombreCMDestino} para agregarlos.</p>
      {disponibles.cargando ? <Cargando /> : <ListaRemitos remitos={todos} seleccion={seleccion} setSeleccion={setSeleccion} />}
      <div className="fila-botones">
        <button className="btn btn-primario" onClick={onConfirmar} disabled={ocupado || !seleccion.length}>Guardar cambios</button>
        {volver}
      </div>
    </div>
  )
}

function NuevoBolsin({ miCM, onCerrar, onCreado }) {
  const [destino, setDestino] = useState('')
  const [seleccion, setSeleccion] = useState([])
  const [peso, setPeso] = useState('')
  const [ejecutar, ocupado, error] = useOperacion()
  const remitos = useRemitosDisponibles(miCM, destino)
  const elegidos = (remitos.datos ?? []).filter((r) => seleccion.includes(r.numero))
  const docs = elegidos.reduce((n, r) => n + r.detalles.length, 0)

  const generar = (e) => {
    e.preventDefault()
    ejecutar(async () => onCreado(await api('/api/bolsines', { method: 'POST', body: { codigoCMDestino: destino, remitos: seleccion, peso: Number(peso) } })))
  }

  return (
    <Panel titulo="Generar bolsín" onCerrar={onCerrar}>
      <form className="subform" onSubmit={generar}>
        <Campo etiqueta="Comisión médica destino" id="n-destino">
          <SelectCM id="n-destino" value={destino} onChange={(v) => { setDestino(v); setSeleccion([]) }} excluir={miCM} vacio="Seleccioná una CM" />
        </Campo>
        {destino && (remitos.cargando ? <Cargando /> : remitos.error ? <Aviso tono="error">{remitos.error.message}</Aviso> : !remitos.datos.length ? (
          <Aviso tono="info" titulo="No hay remitos para esta CM">No existen remitos en estado Creado hacia la comisión médica seleccionada.</Aviso>
        ) : (
          <>
            <span className="campo-etiqueta">Remitos a incluir</span>
            <ListaRemitos remitos={remitos.datos} seleccion={seleccion} setSeleccion={setSeleccion} />
            <Campo etiqueta="Peso (gramos)" id="n-peso">
              <input id="n-peso" type="number" min="1" step="1" inputMode="numeric" value={peso} onChange={(e) => setPeso(e.target.value)} required />
            </Campo>
            {seleccion.length > 0 && (
              <p className="resumen">
                Se incluirán <strong>{seleccion.length} remito{seleccion.length > 1 ? 's' : ''}</strong> ({elegidos.map((r) => `n.º ${r.numero}`).join(', ')}) con {docs} documentación{docs === 1 ? '' : 'es'}.
              </p>
            )}
          </>
        ))}
        {error && <Aviso tono="error">{error}</Aviso>}
        <div className="fila-botones">
          <button className="btn btn-primario" disabled={ocupado || !seleccion.length || !peso}>{ocupado ? 'Generando…' : 'Generar bolsín'}</button>
          <button type="button" className="btn btn-texto" onClick={onCerrar}>Cancelar</button>
        </div>
      </form>
    </Panel>
  )
}
