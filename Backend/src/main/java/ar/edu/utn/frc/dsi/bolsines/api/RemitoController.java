package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.api.Dtos.GenerarRemitoRequest;
import ar.edu.utn.frc.dsi.bolsines.api.Dtos.ModificarRemitoRequest;
import ar.edu.utn.frc.dsi.bolsines.api.Dtos.RemitoDto;
import ar.edu.utn.frc.dsi.bolsines.modelo.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.modelo.Empleado;
import ar.edu.utn.frc.dsi.bolsines.modelo.Remito;
import ar.edu.utn.frc.dsi.bolsines.repositorio.ComisionMedicaRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.DocumentacionRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EstadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.RemitoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ar.edu.utn.frc.dsi.bolsines.modelo.Estado.*;

/** Máquina de estados de Remito: CU 15 Generar, 16 Modificar, 17 Cancelar, 18 Consultar. */
@RestController
@RequestMapping("/api/remitos")
public class RemitoController {
    private final RemitoRepository remitos;
    private final DocumentacionRepository documentaciones;
    private final ComisionMedicaRepository comisiones;
    private final EstadoRepository estados;
    private final SesionActual sesionActual;

    public RemitoController(RemitoRepository remitos, DocumentacionRepository documentaciones, ComisionMedicaRepository comisiones,
                            EstadoRepository estados, SesionActual sesionActual) {
        this.remitos = remitos;
        this.documentaciones = documentaciones;
        this.comisiones = comisiones;
        this.estados = estados;
        this.sesionActual = sesionActual;
    }

    /** 15. Generar Remito: Creado; documentación Registrada -> EnRemito. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public RemitoDto generar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @Valid @RequestBody GenerarRemitoRequest req) {
        Empleado empleado = sesionActual.empleado(sesionId);
        Remito r = new Remito(remitos.siguienteNumero(), empleado.getCM(), comisiones.porCodigo(req.codigoCMDestino()),
                documentaciones(req.documentaciones()), req.areaCMCDestino(),
                estados.remito(CREADO), estados.documentacion(EN_REMITO), empleado);
        return RemitoDto.de(remitos.save(r));
    }

    /** 16. Modificar Remito: solo en Creado; la documentación quitada vuelve a Registrada. */
    @PutMapping("/{numero}")
    @Transactional
    public RemitoDto modificar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero,
                               @Valid @RequestBody ModificarRemitoRequest req) {
        Empleado empleado = sesionActual.empleado(sesionId);
        Remito r = remitoDeMiCM(numero, empleado);
        r.modificar(documentaciones(req.documentaciones()), req.areaCMCDestino(),
                estados.documentacion(REGISTRADA), estados.documentacion(EN_REMITO), empleado);
        return RemitoDto.de(r);
    }

    /** 17. Cancelar Remito: Creado -> Cancelado; documentación -> Registrada. */
    @PostMapping("/{numero}/cancelar")
    @Transactional
    public RemitoDto cancelar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero) {
        Empleado empleado = sesionActual.empleado(sesionId);
        Remito r = remitoDeMiCM(numero, empleado);
        r.cancelar(estados.remito(CANCELADO), estados.documentacion(REGISTRADA), empleado);
        return RemitoDto.de(r);
    }

    /** 18. Consultar Remito: remitos enviados o recibidos por la CM del usuario. Filtros opcionales. */
    @GetMapping
    @Transactional(readOnly = true)
    public List<RemitoDto> listar(@RequestHeader(SesionActual.HEADER) UUID sesionId,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) String codigoCMDestino) {
        var cm = sesionActual.empleado(sesionId).getCM();
        return remitos.findByOrigenOrDestinoOrderByNumero(cm, cm).stream()
                .filter(r -> estado == null || r.getEstado().es(estado))
                .filter(r -> codigoCMDestino == null || r.getDestino().getCodigo().equalsIgnoreCase(codigoCMDestino))
                .map(RemitoDto::de)
                .toList();
    }

    @GetMapping("/{numero}")
    @Transactional(readOnly = true)
    public RemitoDto consultar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero) {
        sesionActual.obtener(sesionId);
        return RemitoDto.de(remitos.porNumero(numero));
    }

    private Remito remitoDeMiCM(int numero, Empleado empleado) {
        Remito r = remitos.porNumero(numero);
        if (!r.tenesEstaCMOrigen(empleado.getCM())) {
            throw new SecurityException("El remito " + numero + " no fue generado por la " + empleado.mostrarCM());
        }
        return r;
    }

    private List<Documentacion> documentaciones(List<Integer> numeros) {
        return numeros.stream().distinct().map(documentaciones::porNumero).toList();
    }
}
