package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.modelo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Requests y responses JSON de la API. */
public final class Dtos {
    private Dtos() {}

    // ---------- requests ----------
    public record LoginRequest(@NotBlank String nombre, @NotBlank String contrasena) {}

    public record ArchivoDto(@NotBlank String nombreArchivo, String tituloDocumento) {
        public static ArchivoDto de(Archivo a) { return new ArchivoDto(a.getNombreArchivo(), a.getTituloDocumento()); }
    }

    public record RegistrarDocumentacionRequest(@NotBlank String tipoDocumento, @NotBlank String asunto,
                                                @NotNull LocalDate fechaPase, List<@Valid ArchivoDto> archivos) {}

    public record GenerarRemitoRequest(@NotBlank String codigoCMDestino, @NotEmpty List<@NotNull Integer> documentaciones,
                                       String areaCMCDestino) {}

    public record ModificarRemitoRequest(@NotEmpty List<@NotNull Integer> documentaciones, String areaCMCDestino) {}

    public record GenerarBolsinRequest(@NotBlank String codigoCMDestino, @NotEmpty List<@NotNull Integer> remitos,
                                       @NotNull @Positive Integer peso) {}

    public record ModificarBolsinRequest(@NotEmpty List<@NotNull Integer> remitos) {}

    public record PrecintarRequest(@NotBlank String numeroPrecinto) {}

    /** Vacío o sin documentaciones = el contenido del bolsín es igual al registrado. */
    public record RecepcionRequest(List<@NotNull Integer> documentacionesNoRecibidas) {
        public List<Integer> noRecibidas() { return documentacionesNoRecibidas == null ? List.of() : documentacionesNoRecibidas; }
    }

    public record SeleccionRequest(@NotNull Integer numeroBolsin) {}

    public record ConfirmacionRequest(@NotNull Boolean enviarMail) {}

    // ---------- responses ----------
    public record ComisionMedicaDto(String codigo, String nombre, String direccion, String email, String telefono, String localidad) {
        public static ComisionMedicaDto de(ComisionMedica cm) {
            return new ComisionMedicaDto(cm.getCodigo(), cm.getNombre(), cm.getDireccion(), cm.getEmail(), cm.getTelefono(),
                    cm.getLocalidad().getNombre());
        }
    }

    public record SesionDto(UUID sesionId, String usuario, String empleado, String email, String rol, ComisionMedicaDto comisionMedica) {
        public static SesionDto de(Sesion s, Empleado e) {
            return new SesionDto(s.getId(), s.getUsuario().getNombre(), e.getNombreCompleto(), e.getEmail(),
                    e.getRol().getNombre(), ComisionMedicaDto.de(e.getCM()));
        }
    }

    public record TipoDocumentoDto(String nombre, String descripcion) {
        public static TipoDocumentoDto de(TipoDocumento t) { return new TipoDocumentoDto(t.getNombre(), t.getDescripcion()); }
    }

    public record CambioEstadoDto(String estado, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String responsable) {}

    public record DocumentacionDto(int numero, String asunto, LocalDate fechaPase, String tipoDocumento, String cmOrigen,
                                   String estado, List<ArchivoDto> archivos, List<CambioEstadoDto> historial) {
        public static DocumentacionDto de(Documentacion d) {
            return new DocumentacionDto(d.getNumero(), d.getAsunto(), d.getFechaPase(), d.mostrarTipoDocumentacion(),
                    d.getCmOrigen().getCodigo(), d.getEstadoActual().getNombre(),
                    d.getArchivoAdjunto().stream().map(ArchivoDto::de).toList(),
                    d.getCambioEstado().stream().map(ce -> new CambioEstadoDto(ce.getEstado().getNombre(), ce.getFechaHoraInicio(),
                            ce.getFechaHoraFin(), ce.getResponsableCE().getNombreCompleto())).toList());
        }
    }

    public record DetalleRemitoDto(String areaCMCDestino, DocumentacionDto documentacion) {}

    public record RemitoDto(int numero, LocalDate fecha, String codigoCMOrigen, String codigoCMDestino, String nombreCMDestino,
                            String estado, List<DetalleRemitoDto> detalles) {
        public static RemitoDto de(Remito r) {
            return new RemitoDto(r.getNumero(), r.getFecha(), r.getOrigen().getCodigo(), r.getDestino().getCodigo(),
                    r.getDestino().getNombre(), r.getEstado().getNombre(),
                    r.getDetalleRemito().stream()
                            .map(d -> new DetalleRemitoDto(d.getAreaCMCDestino(), DocumentacionDto.de(d.getDocumentacion())))
                            .toList());
        }
    }

    public record BolsinDto(int numeroBolsin, LocalDate fecha, String numeroPrecinto, Integer peso,
                            String codigoCMOrigen, String nombreCMOrigen, String codigoCMDestino, String nombreCMDestino,
                            String estado, List<RemitoDto> remitos, List<CambioEstadoDto> historial) {
        public static BolsinDto de(Bolsin b) {
            return new BolsinDto(b.getNumeroBolsin(), b.getFecha(), b.getNumeroPrecinto(), b.getPeso(),
                    b.getOrigen().getCodigo(), b.getOrigen().getNombre(), b.getCMDestino().getCodigo(), b.getCMDestino().getNombre(),
                    b.getEstadoActual().getNombre(),
                    b.obtenerInformacionRemito().stream().map(RemitoDto::de).toList(),
                    b.getCambioEstado().stream().map(ce -> new CambioEstadoDto(ce.getEstado().getNombre(), ce.getFechaHoraInicio(),
                            ce.getFechaHoraFin(), ce.getResponsableCE().getNombreCompleto())).toList());
        }
    }
}
