package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.modelo.Empleado;
import ar.edu.utn.frc.dsi.bolsines.modelo.Sesion;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.SesionRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/** Resuelve la sesión del header X-Sesion-Id que devuelve POST /api/sesiones. */
@Component
public class SesionActual {
    public static final String HEADER = "X-Sesion-Id";

    private final SesionRepository sesiones;
    private final EmpleadoRepository empleados;

    public SesionActual(SesionRepository sesiones, EmpleadoRepository empleados) {
        this.sesiones = sesiones;
        this.empleados = empleados;
    }

    public Sesion obtener(UUID id) {
        return sesiones.findById(id)
                .filter(Sesion::estaActiva)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Sesión inexistente o cerrada. Inicie sesión nuevamente"));
    }

    public Empleado empleado(UUID id) {
        Sesion sesion = obtener(id);
        return empleados.findByUsuario(sesion.getUsuario())
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "El usuario no está asignado a ningún empleado"));
    }
}
