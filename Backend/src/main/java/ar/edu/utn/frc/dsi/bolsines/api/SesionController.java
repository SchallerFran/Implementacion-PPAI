package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.api.Dtos.LoginRequest;
import ar.edu.utn.frc.dsi.bolsines.api.Dtos.SesionDto;
import ar.edu.utn.frc.dsi.bolsines.modelo.Empleado;
import ar.edu.utn.frc.dsi.bolsines.modelo.Sesion;
import ar.edu.utn.frc.dsi.bolsines.modelo.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.SesionRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/** CU 5 Iniciar sesión / CU 6 Cerrar sesión. */
@RestController
@RequestMapping("/api/sesiones")
public class SesionController {
    private final UsuarioRepository usuarios;
    private final EmpleadoRepository empleados;
    private final SesionRepository sesiones;
    private final SesionActual sesionActual;

    public SesionController(UsuarioRepository usuarios, EmpleadoRepository empleados, SesionRepository sesiones, SesionActual sesionActual) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.sesiones = sesiones;
        this.sesionActual = sesionActual;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public SesionDto iniciar(@Valid @RequestBody LoginRequest req) {
        Usuario usuario = usuarios.findByNombre(req.nombre())
                .filter(u -> u.validarContrasena(req.contrasena()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        Empleado empleado = empleados.findByUsuario(usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está asignado a ningún empleado"));
        return SesionDto.de(sesiones.save(new Sesion(usuario)), empleado);
    }

    @GetMapping("/actual")
    @Transactional(readOnly = true)
    public SesionDto actual(@RequestHeader(SesionActual.HEADER) UUID sesionId) {
        return SesionDto.de(sesionActual.obtener(sesionId), sesionActual.empleado(sesionId));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void cerrar(@RequestHeader(SesionActual.HEADER) UUID sesionId) {
        sesionActual.obtener(sesionId).cerrar();
    }
}
