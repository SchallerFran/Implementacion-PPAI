import { useState } from 'react'
import { Ban, FilePlus2, Pencil } from 'lucide-react'
import { api, query } from './api'
import {
  Aviso, Campo, Cargando, Confirmacion, Estado, ESTADOS_REMITO, fecha, Panel, SelectCM, SelectEstado, Tramo,
  useComisiones, useConsulta, useOperacion, Vacio,
} from './ui'

const esCentral = (cms, codigo) => cms.find((c) => c.codigo === codigo)?.nombre.includes('Central')

/** CU 15 Generar · 16 Modificar · 17 Cancelar · 18 Consultar remito. */
export default function Remitos({ sesion }) {
  const miCM = sesion.comisionMedica.codigo
  const cms = useComisiones()
  const cm = (codigo) => ({ codigo, nombre: cms.find((c) => c.codigo === codigo)?.nombre })
  const [estado, setEstado] = useState('')
  const [destino, setDestino] = useState('')
  const lista = useConsulta(`/api/remitos${query({ estado, codigoCMDestino: destino })}`)
  const [panel, setPanel] = useState(null)
  const [actualizado, setActualizado] = useState(null)
  const [exito, setExito] = useState(null)

  function alActualizar(r, mensaje) {
    setExito(mensaje)
    setActualizado(r.numero)
    setPanel({ modo: 'detalle', remito: r })
    lista.recargar()
  }

  return (
    <div className="pagina">
      <header className="pagina-cab">
        <div>
          <h1>Remitos</h1>
          <p className="texto-sec">Remitos generados o recibidos por {sesion.comisionMedica.nombre}.</p>
        </div>
        <button className="btn btn-primario" onClick={() => { setExito(null); setPanel({ modo: 'nuevo' }) }}>
          <FilePlus2 size={17} aria-hidden="true" /> Generar remito
        </button>
      </header>

      <div className="filtros">
        <Campo etiqueta="Estado" id="r-estado"><SelectEstado id="r-estado" value={estado} onChange={setEstado} estados={ESTADOS_REMITO} /></Campo>
        <Campo etiqueta="CM destino" id="r-destino"><SelectCM id="r-destino" value={destino} onChange={setDestino} /></Campo>
        {(estado || destino) && <button className="btn btn-texto" onClick={() => { setEstado(''); setDestino('') }}>Quitar filtros</button>}
      </div>

      {exito && <Aviso tono="ok" onCerrar={() => setExito(null)}>{exito}</Aviso>}

      <div className={panel ? 'trabajo con-panel' : 'trabajo'}>
        <section className="tabla-wrap">
          {lista.cargando && !lista.datos ? <Cargando /> : lista.error ? <Aviso tono="error">{lista.error.message}</Aviso> : !lista.datos.length ? (
            <Vacio titulo="No hay remitos para mostrar">{estado || destino ? 'Probá quitando los filtros.' : 'Generá un remito a partir de la documentación registrada.'}</Vacio>
          ) : (
            <table className="tabla">
              <thead>
                <tr><th>Remito</th><th>Recorrido</th><th className="num">Documentación</th><th>Estado</th></tr>
              </thead>
              <tbody>
                {lista.datos.map((r) => (
                  <tr key={r.numero}
                    className={`${panel?.remito?.numero === r.numero ? 'fila-activa' : ''} ${actualizado === r.numero ? 'fila-actualizada' : ''} ${r.estado === 'Cancelado' ? 'fila-anulada' : ''}`}
                    onClick={() => setPanel({ modo: 'detalle', remito: r })}>
                    <td>
                      <button className="link-fila" onClick={(e) => { e.stopPropagation(); setPanel({ modo: 'detalle', remito: r }) }}>N.º {r.numero}</button>
                      <span className="texto-sec texto-chico">{fecha(r.fecha)}</span>
                    </td>
                    <td><Tramo tipo="remito" origen={cm(r.codigoCMOrigen)} destino={{ codigo: r.codigoCMDestino, nombre: r.nombreCMDestino }} estado={r.estado} /></td>
                    <td className="num">{r.detalles.length}</td>
                    <td><Estado estado={r.estado} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        {panel?.modo === 'nuevo' && (
          <FormRemito miCM={miCM} onCerrar={() => setPanel(null)}
            onListo={(r) => alActualizar(r, `Se generó el remito ${r.numero} hacia ${r.nombreCMDestino}.`)} />
        )}
        {panel?.modo === 'detalle' && (
          <DetalleRemito key={panel.remito.numero} remito={panel.remito} origen={cm(panel.remito.codigoCMOrigen)} miCM={miCM}
            onCerrar={() => setPanel(null)} onActualizado={alActualizar} />
        )}
      </div>
    </div>
  )
}

function DetalleRemito({ remito: r, origen, miCM, onCerrar, onActualizado }) {
  const [modo, setModo] = useState('ver')
  const [ejecutar, ocupado, error] = useOperacion()
  const editable = r.codigoCMOrigen === miCM && r.estado === 'Creado'

  if (modo === 'modificar') {
    return <FormRemito miCM={miCM} remito={r} onCerrar={() => setModo('ver')}
      onListo={(n) => { onActualizado(n, `Se actualizó la documentación del remito ${n.numero}.`); setModo('ver') }} />
  }

  return (
    <Panel titulo={`Remito ${r.numero}`} onCerrar={onCerrar}>
      <div className="panel-estado"><Estado estado={r.estado} /></div>
      <Tramo tipo="remito" origen={origen} destino={{ codigo: r.codigoCMDestino, nombre: r.nombreCMDestino }} estado={r.estado} detallado />
      <dl className="datos">
        <dt>Origen</dt><dd>{origen.nombre ?? r.codigoCMOrigen}</dd>
        <dt>Destino</dt><dd>{r.nombreCMDestino}</dd>
        <dt>Fecha</dt><dd>{fecha(r.fecha)}</dd>
      </dl>
      {error && <Aviso tono="error">{error}</Aviso>}
      {editable && modo === 'ver' && (
        <div className="acciones">
          <button className="btn btn-secundario btn-chico" onClick={() => setModo('modificar')}><Pencil size={16} aria-hidden="true" /> Modificar documentación</button>
          <button className="btn btn-peligro btn-chico" onClick={() => setModo('cancelar')}><Ban size={16} aria-hidden="true" /> Cancelar remito</button>
        </div>
      )}
      {modo === 'cancelar' && (
        <Confirmacion peligro texto={`El remito ${r.numero} pasa a Cancelado y su documentación vuelve a Registrada.`}
          boton="Cancelar remito" ocupado={ocupado}
          volver={<button className="btn btn-texto" onClick={() => setModo('ver')} disabled={ocupado}>Volver</button>}
          onConfirmar={() => ejecutar(async () => {
            const n = await api(`/api/remitos/${r.numero}/cancelar`, { method: 'POST' })
            onActualizado(n, `El remito ${n.numero} se canceló.`)
            setModo('ver')
          })} />
      )}
      <h3 className="subtitulo">Documentación <span className="contador">{r.detalles.length}</span></h3>
      <ul className="lista-remitos">
        {r.detalles.map(({ documentacion: d, areaCMCDestino }) => (
          <li key={d.numero}>
            <div className="lista-remitos-cab"><strong>{d.asunto}</strong><Estado estado={d.estado} /></div>
            <span className="texto-sec texto-chico">Doc. {d.numero} · {d.tipoDocumento} · pase {fecha(d.fechaPase)}{areaCMCDestino ? ` · Área ${areaCMCDestino}` : ''}</span>
          </li>
        ))}
      </ul>
    </Panel>
  )
}

/** Alta (CU 15) o modificación (CU 16) de un remito. */
function FormRemito({ miCM, remito, onCerrar, onListo }) {
  const cms = useComisiones()
  const [destino, setDestino] = useState(remito?.codigoCMDestino ?? '')
  const actuales = remito?.detalles.map((d) => d.documentacion) ?? []
  const [seleccion, setSeleccion] = useState(actuales.map((d) => d.numero))
  const [area, setArea] = useState(remito?.detalles[0]?.areaCMCDestino ?? '')
  const registradas = useConsulta('/api/documentaciones?estado=Registrada')
  const [ejecutar, ocupado, error] = useOperacion()
  const opciones = [...actuales, ...(registradas.datos ?? [])]

  const guardar = (e) => {
    e.preventDefault()
    const areaCMCDestino = esCentral(cms, destino) && area.trim() ? area.trim() : undefined
    ejecutar(async () => onListo(remito
      ? await api(`/api/remitos/${remito.numero}`, { method: 'PUT', body: { documentaciones: seleccion, areaCMCDestino } })
      : await api('/api/remitos', { method: 'POST', body: { codigoCMDestino: destino, documentaciones: seleccion, areaCMCDestino } })))
  }

  return (
    <Panel titulo={remito ? `Modificar remito ${remito.numero}` : 'Generar remito'} onCerrar={onCerrar}>
      <form className="subform" onSubmit={guardar}>
        {!remito && (
          <Campo etiqueta="Comisión médica destino" id="rm-destino">
            <SelectCM id="rm-destino" value={destino} onChange={setDestino} excluir={miCM} vacio="Seleccioná una CM" />
          </Campo>
        )}
        {esCentral(cms, destino) && (
          <Campo etiqueta="Área de la CMC destino" id="rm-area" ayuda="Opcional. Área de la sede central a la que va dirigida.">
            <input id="rm-area" value={area} onChange={(e) => setArea(e.target.value)} />
          </Campo>
        )}
        <span className="campo-etiqueta">Documentación a incluir</span>
        {registradas.cargando ? <Cargando /> : !opciones.length ? (
          <Aviso tono="info" titulo="No hay documentación registrada">Registrá documentación antes de generar un remito.</Aviso>
        ) : (
          <ul className="lista-check">
            {opciones.map((d) => (
              <li key={d.numero}>
                <label>
                  <input type="checkbox" checked={seleccion.includes(d.numero)}
                    onChange={(e) => setSeleccion(e.target.checked ? [...seleccion, d.numero] : seleccion.filter((n) => n !== d.numero))} />
                  <span><strong>{d.asunto}</strong><span className="texto-sec texto-chico">Doc. {d.numero} · {d.tipoDocumento} · pase {fecha(d.fechaPase)}</span></span>
                </label>
              </li>
            ))}
          </ul>
        )}
        {error && <Aviso tono="error">{error}</Aviso>}
        <div className="fila-botones">
          <button className="btn btn-primario" disabled={ocupado || !destino || !seleccion.length}>
            {ocupado ? 'Guardando…' : remito ? 'Guardar cambios' : `Generar remito${seleccion.length ? ` (${seleccion.length})` : ''}`}
          </button>
          <button type="button" className="btn btn-texto" onClick={onCerrar}>{remito ? 'Volver' : 'Cancelar'}</button>
        </div>
      </form>
    </Panel>
  )
}
