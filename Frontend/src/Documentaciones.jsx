import { useEffect, useState } from 'react'
import { FilePlus2, Paperclip, Plus, Trash2 } from 'lucide-react'
import { api, query } from './api'
import {
  Aviso, Campo, Cargando, Escudo, Estado, ESTADOS_DOCUMENTACION, fecha, Historial, Panel, SelectEstado, useComisiones, useConsulta, useOperacion, Vacio,
} from './ui'

const hoy = () => new Date().toLocaleDateString('sv-SE') // yyyy-MM-dd en hora local

/** CU 7 Registrar documentación · CU 10 Consultar documentación. */
export default function Documentaciones({ sesion }) {
  const [estado, setEstado] = useState('')
  const cms = useComisiones()
  const lista = useConsulta(`/api/documentaciones${query({ estado })}`)
  const [panel, setPanel] = useState(null)
  const [actualizado, setActualizado] = useState(null)
  const [exito, setExito] = useState(null)

  return (
    <div className="pagina">
      <header className="pagina-cab">
        <div>
          <h1>Documentación</h1>
          <p className="texto-sec">Documentación registrada en {sesion.comisionMedica.nombre}.</p>
        </div>
        <button className="btn btn-primario" onClick={() => { setExito(null); setPanel({ modo: 'nuevo' }) }}>
          <FilePlus2 size={17} aria-hidden="true" /> Registrar documentación
        </button>
      </header>

      <div className="filtros">
        <Campo etiqueta="Estado" id="d-estado"><SelectEstado id="d-estado" value={estado} onChange={setEstado} estados={ESTADOS_DOCUMENTACION} /></Campo>
        {estado && <button className="btn btn-texto" onClick={() => setEstado('')}>Quitar filtro</button>}
      </div>

      {exito && <Aviso tono="ok" onCerrar={() => setExito(null)}>{exito}</Aviso>}

      <div className={panel ? 'trabajo con-panel' : 'trabajo'}>
        <section className="tabla-wrap">
          {lista.cargando && !lista.datos ? <Cargando /> : lista.error ? <Aviso tono="error">{lista.error.message}</Aviso> : !lista.datos.length ? (
            <Vacio titulo="No hay documentación para mostrar">{estado ? 'Probá quitando el filtro.' : 'Registrá la primera documentación a enviar.'}</Vacio>
          ) : (
            <table className="tabla">
              <thead>
                <tr><th className="num">N.º</th><th>Asunto</th><th>Tipo</th><th>Fecha de pase</th><th className="num">Archivos</th><th>Estado</th></tr>
              </thead>
              <tbody>
                {lista.datos.map((d) => (
                  <tr key={d.numero}
                    className={`${panel?.doc?.numero === d.numero ? 'fila-activa' : ''} ${actualizado === d.numero ? 'fila-actualizada' : ''} ${d.estado === 'DeBaja' ? 'fila-anulada' : ''}`}
                    onClick={() => setPanel({ modo: 'detalle', doc: d })}>
                    <td className="num">{d.numero}</td>
                    <td><button className="link-fila" onClick={(e) => { e.stopPropagation(); setPanel({ modo: 'detalle', doc: d }) }}>{d.asunto}</button></td>
                    <td>{d.tipoDocumento}</td>
                    <td>{fecha(d.fechaPase)}</td>
                    <td className="num">{d.archivos.length}</td>
                    <td><Estado estado={d.estado} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        {panel?.modo === 'nuevo' && (
          <NuevaDocumentacion onCerrar={() => setPanel(null)} onRegistrada={(d) => {
            setExito(`Se registró la documentación ${d.numero}: ${d.asunto}.`)
            setActualizado(d.numero)
            setPanel({ modo: 'detalle', doc: d })
            lista.recargar()
          }} />
        )}
        {panel?.modo === 'detalle' && (
          <Panel titulo={`Documentación ${panel.doc.numero}`} onCerrar={() => setPanel(null)}>
            <div className="panel-estado"><Estado estado={panel.doc.estado} /></div>
            <p className="panel-asunto">{panel.doc.asunto}</p>
            <dl className="datos">
              <dt>Tipo</dt><dd>{panel.doc.tipoDocumento}</dd>
              <dt>Fecha de pase</dt><dd>{fecha(panel.doc.fechaPase)}</dd>
              <dt>CM origen</dt><dd className="dd-cm"><Escudo codigo={panel.doc.cmOrigen} nombre={cms.find((c) => c.codigo === panel.doc.cmOrigen)?.nombre} />{cms.find((c) => c.codigo === panel.doc.cmOrigen)?.nombre ?? panel.doc.cmOrigen}</dd>
            </dl>
            <h3 className="subtitulo">Archivos <span className="contador">{panel.doc.archivos.length}</span></h3>
            {panel.doc.archivos.length ? (
              <ul className="lista-archivos">
                {panel.doc.archivos.map((a, i) => (
                  <li key={i}><Paperclip size={15} aria-hidden="true" /><span><strong>{a.nombreArchivo}</strong>{a.tituloDocumento && <span className="texto-sec texto-chico">{a.tituloDocumento}</span>}</span></li>
                ))}
              </ul>
            ) : <p className="texto-sec">Sin archivos adjuntos.</p>}
            <h3 className="subtitulo">Historial</h3>
            <Historial items={panel.doc.historial} />
          </Panel>
        )}
      </div>
    </div>
  )
}

function NuevaDocumentacion({ onCerrar, onRegistrada }) {
  const [tipos, setTipos] = useState([])
  const [tipoDocumento, setTipo] = useState('')
  const [asunto, setAsunto] = useState('')
  const [fechaPase, setFechaPase] = useState(hoy())
  const [archivos, setArchivos] = useState([{ nombreArchivo: '', tituloDocumento: '' }])
  const [ejecutar, ocupado, error] = useOperacion()

  useEffect(() => { api('/api/tipos-documento').then(setTipos).catch(() => {}) }, [])

  const cambiar = (i, campo, valor) => setArchivos(archivos.map((a, j) => (j === i ? { ...a, [campo]: valor } : a)))

  const registrar = (e) => {
    e.preventDefault()
    const adjuntos = archivos.filter((a) => a.nombreArchivo.trim())
      .map((a) => ({ nombreArchivo: a.nombreArchivo.trim(), tituloDocumento: a.tituloDocumento.trim() || undefined }))
    ejecutar(async () => onRegistrada(await api('/api/documentaciones', {
      method: 'POST', body: { tipoDocumento, asunto: asunto.trim(), fechaPase, archivos: adjuntos },
    })))
  }

  return (
    <Panel titulo="Registrar documentación" onCerrar={onCerrar}>
      <form className="subform" onSubmit={registrar}>
        <Campo etiqueta="Tipo de documento" id="doc-tipo">
          <select id="doc-tipo" value={tipoDocumento} onChange={(e) => setTipo(e.target.value)} required>
            <option value="">Seleccioná un tipo</option>
            {tipos.map((t) => <option key={t.nombre} value={t.nombre}>{t.nombre}</option>)}
          </select>
        </Campo>
        <Campo etiqueta="Asunto" id="doc-asunto">
          <input id="doc-asunto" value={asunto} onChange={(e) => setAsunto(e.target.value)} required placeholder="Expediente de incapacidad laboral N.º 1101" />
        </Campo>
        <Campo etiqueta="Fecha de pase" id="doc-fecha" ayuda="Fecha a partir de la cual se puede enviar.">
          <input id="doc-fecha" type="date" value={fechaPase} onChange={(e) => setFechaPase(e.target.value)} required />
        </Campo>
        <fieldset className="archivos">
          <legend className="campo-etiqueta">Archivos</legend>
          {archivos.map((a, i) => (
            <div key={i} className="archivo-fila">
              <input aria-label={`Nombre del archivo ${i + 1}`} placeholder="expediente-1101.pdf" value={a.nombreArchivo} onChange={(e) => cambiar(i, 'nombreArchivo', e.target.value)} />
              <input aria-label={`Título del archivo ${i + 1}`} placeholder="Título (opcional)" value={a.tituloDocumento} onChange={(e) => cambiar(i, 'tituloDocumento', e.target.value)} />
              <button type="button" className="btn-icono" aria-label={`Quitar archivo ${i + 1}`} disabled={archivos.length === 1}
                onClick={() => setArchivos(archivos.filter((_, j) => j !== i))}><Trash2 size={16} /></button>
            </div>
          ))}
          <button type="button" className="btn btn-texto btn-chico" onClick={() => setArchivos([...archivos, { nombreArchivo: '', tituloDocumento: '' }])}>
            <Plus size={16} aria-hidden="true" /> Agregar archivo
          </button>
        </fieldset>
        {error && <Aviso tono="error">{error}</Aviso>}
        <div className="fila-botones">
          <button className="btn btn-primario" disabled={ocupado}>{ocupado ? 'Registrando…' : 'Registrar'}</button>
          <button type="button" className="btn btn-texto" onClick={onCerrar}>Cancelar</button>
        </div>
      </form>
    </Panel>
  )
}
