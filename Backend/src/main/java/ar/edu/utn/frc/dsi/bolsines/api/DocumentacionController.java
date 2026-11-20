package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.api.Dtos.DocumentacionDto;
import ar.edu.utn.frc.dsi.bolsines.api.Dtos.RegistrarDocumentacionRequest;
import ar.edu.utn.frc.dsi.bolsines.modelo.Archivo;
import ar.edu.utn.frc.dsi.bolsines.modelo.Documentacion;
import ar.edu.utn.frc.dsi.bolsines.modelo.Empleado;
import ar.edu.utn.frc.dsi.bolsines.modelo.Estado;
import ar.edu.utn.frc.dsi.bolsines.repositorio.DocumentacionRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EstadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.TipoDocumentoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/** CU 7 Registrar Documentación / CU 10 Consultar Documentación. */
@RestController
@RequestMapping("/api/documentaciones")
public class DocumentacionController {
    private final DocumentacionRepository documentaciones;
    private final TipoDocumentoRepository tipos;
    private final EstadoRepository estados;
    private final SesionActual sesionActual;

    public DocumentacionController(DocumentacionRepository documentaciones, TipoDocumentoRepository tipos,
                                   EstadoRepository estados, SesionActual sesionActual) {
        this.documentaciones = documentaciones;
        this.tipos = tipos;
        this.estados = estados;
        this.sesionActual = sesionActual;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public DocumentacionDto registrar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @Valid @RequestBody RegistrarDocumentacionRequest req) {
        Empleado empleado = sesionActual.empleado(sesionId);
        var tipo = tipos.findByNombre(req.tipoDocumento())
                .orElseThrow(() -> new NoSuchElementException("No existe el tipo de documento " + req.tipoDocumento()));
        List<Archivo> archivos = req.archivos() == null ? List.of()
                : req.archivos().stream().map(a -> new Archivo(a.nombreArchivo(), a.tituloDocumento())).toList();
        Documentacion d = new Documentacion(documentaciones.siguienteNumero(), tipo, req.asunto(), req.fechaPase(),
                empleado.getCM(), archivos, estados.documentacion(Estado.REGISTRADA), empleado);
        return DocumentacionDto.de(documentaciones.save(d));
    }

    /** Documentación registrada en la CM del usuario logueado. */
    @GetMapping
    @Transactional(readOnly = true)
    public List<DocumentacionDto> listar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @RequestParam(required = false) String estado) {
        return documentaciones.findByCmOrigenOrderByNumero(sesionActual.empleado(sesionId).getCM()).stream()
                .filter(d -> estado == null || d.getEstadoActual().es(estado))
                .map(DocumentacionDto::de)
                .toList();
    }

    @GetMapping("/{numero}")
    @Transactional(readOnly = true)
    public DocumentacionDto consultar(@RequestHeader(SesionActual.HEADER) UUID sesionId, @PathVariable int numero) {
        sesionActual.obtener(sesionId);
        return DocumentacionDto.de(documentaciones.porNumero(numero));
    }
}
