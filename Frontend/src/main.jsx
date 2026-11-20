import { StrictMode, useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import { FileStack, FileText, LogOut, MapPinned, Package } from 'lucide-react'
import '@fontsource/montserrat/500.css'
import '@fontsource/montserrat/600.css'
import '@fontsource/montserrat/700.css'
import './styles.css'
import { alPerderSesion, api, guardarSesion, haySesionGuardada } from './api'
import { Aviso, Cargando, Escudo } from './ui'
import PantallaSeguimientoBolsines from './PantallaSeguimientoBolsines'
import Bolsines from './Bolsines'
import Remitos from './Remitos'
import Documentaciones from './Documentaciones'

const EB = 'Encargado de Bolsines'

const SECCIONES = [
  { id: 'seguimiento', titulo: 'Seguimiento', icono: MapPinned, soloEB: true, Pantalla: PantallaSeguimientoBolsines },
  { id: 'bolsines', titulo: 'Bolsines', icono: Package, Pantalla: Bolsines },
  { id: 'remitos', titulo: 'Remitos', icono: FileStack, Pantalla: Remitos },
  { id: 'documentacion', titulo: 'Documentación', icono: FileText, Pantalla: Documentaciones },
]

function useRuta() {
  const leer = () => location.hash.replace(/^#\/?/, '')
  const [ruta, setRuta] = useState(leer)
  useEffect(() => {
    const f = () => setRuta(leer())
    addEventListener('hashchange', f)
    return () => removeEventListener('hashchange', f)
  }, [])
  return [ruta, (r) => { location.hash = `/${r}` }]
}

function App() {
  const [sesion, setSesion] = useState(null)
  const [verificando, setVerificando] = useState(haySesionGuardada())
  const [expirada, setExpirada] = useState(false)

  useEffect(() => {
    alPerderSesion(() => { setSesion(null); setExpirada(true) })
    if (!haySesionGuardada()) return
    api('/api/sesiones/actual').then(setSesion).catch(() => {}).finally(() => setVerificando(false))
  }, [])

  if (verificando) return <div className="pantalla-centro"><Cargando texto="Verificando sesión…" /></div>
  if (!sesion) return <Login expirada={expirada} onIngreso={(s) => { setExpirada(false); setSesion(s) }} />
  return <Shell sesion={sesion} onSalir={() => setSesion(null)} />
}

function Login({ onIngreso, expirada }) {
  const [nombre, setNombre] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [error, setError] = useState(null)
  const [ocupado, setOcupado] = useState(false)

  async function ingresar(e) {
    e.preventDefault()
    setOcupado(true)
    setError(null)
    try {
      const s = await api('/api/sesiones', { method: 'POST', body: { nombre: nombre.trim(), contrasena } })
      guardarSesion(s.sesionId)
      onIngreso(s)
    } catch (err) {
      setError(err.status === 401 ? 'Usuario o contraseña incorrectos.' : err.message)
    } finally {
      setOcupado(false)
    }
  }

  return (
    <div className="login">
      <section className="login-marca">
        <div className="marca marca-grande"><Logo /> Bolsines</div>
        <p className="login-lema">Seguimiento y trazabilidad de la documentación que viaja entre comisiones médicas.</p>
        <div className="login-ruta" aria-hidden="true">
          <Escudo codigo="CM-CBA-01" grande />
          <span className="login-via"><span className="movil" /></span>
          <Escudo codigo="CM-ROS-02" grande />
        </div>
        <p className="login-pie">UTN · FRC — Diseño de Sistemas de Información<br />PPAI 2026 · 3K5 · Grupo 5</p>
      </section>
      <section className="login-form-wrap">
        <form className="login-form" onSubmit={ingresar}>
          <h1>Iniciar sesión</h1>
          <p className="texto-sec">Ingresá con tu usuario de la comisión médica.</p>
          {expirada && !error && <Aviso tono="info">Tu sesión terminó. Volvé a ingresar para continuar.</Aviso>}
          {error && <Aviso tono="error">{error}</Aviso>}
          <label className="campo">
            <span className="campo-etiqueta">Usuario</span>
            <input value={nombre} onChange={(e) => setNombre(e.target.value)} autoComplete="username" placeholder="eb.cordoba" required autoFocus />
          </label>
          <label className="campo">
            <span className="campo-etiqueta">Contraseña</span>
            <input type="password" value={contrasena} onChange={(e) => setContrasena(e.target.value)} autoComplete="current-password" required />
          </label>
          <button className="btn btn-primario btn-bloque" disabled={ocupado}>
            {ocupado ? <><span className="spinner" aria-hidden="true" /> Ingresando…</> : 'Ingresar'}
          </button>
        </form>
      </section>
    </div>
  )
}

function Shell({ sesion, onSalir }) {
  const esEB = sesion.rol === EB
  const visibles = SECCIONES.filter((s) => !s.soloEB || esEB)
  const [ruta, ir] = useRuta()
  const actual = visibles.find((s) => s.id === ruta) ?? visibles[0]
  const { Pantalla } = actual

  async function cerrarSesion() {
    await api('/api/sesiones', { method: 'DELETE' }).catch(() => {})
    guardarSesion(null)
    onSalir()
  }

  return (
    <div className="shell">
      <header className="barra">
        <div className="marca"><Logo /> Bolsines</div>
        <nav className="nav" aria-label="Secciones">
          {visibles.map(({ id, titulo, icono: Icono }) => (
            // opcionConsultarUbicacion(): elegir "Seguimiento" abre la PantallaSeguimientoBolsines
            <a key={id} href={`#/${id}`} className={id === actual.id ? 'activo' : ''} aria-current={id === actual.id ? 'page' : undefined}>
              <Icono size={17} aria-hidden="true" /> {titulo}
            </a>
          ))}
        </nav>
        <div className="usuario">
          <Escudo codigo={sesion.comisionMedica.codigo} nombre={sesion.comisionMedica.nombre} />
          <div className="usuario-datos">
            <strong>{sesion.empleado}</strong>
            <span>{sesion.rol} · {sesion.comisionMedica.nombre}</span>
          </div>
          <button className="btn-icono btn-icono-claro" onClick={cerrarSesion} aria-label="Cerrar sesión" title="Cerrar sesión">
            <LogOut size={18} />
          </button>
        </div>
      </header>
      <main className="contenido">
        <Pantalla key={actual.id} sesion={sesion} esEB={esEB} onCancelar={() => ir('bolsines')} />
      </main>
    </div>
  )
}

function Logo() {
  return (
    <svg viewBox="0 0 48 48" className="logo" aria-hidden="true">
      <path d="M3 3h42v29L24 45 3 32z" fill="#fff" />
      <path d="M7 7h34v22.6L24 40 7 29.6z" fill="none" stroke="var(--azul-700)" strokeWidth="2" />
      <circle cx="24" cy="21" r="6" fill="var(--amarillo)" />
    </svg>
  )
}

createRoot(document.getElementById('root')).render(<StrictMode><App /></StrictMode>)
