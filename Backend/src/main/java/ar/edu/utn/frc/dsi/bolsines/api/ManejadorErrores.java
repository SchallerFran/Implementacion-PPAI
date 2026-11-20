package ar.edu.utn.frc.dsi.bolsines.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones a respuestas RFC 7807 (application/problem+json):
 * 404 no encontrado / 409 transición de estado inválida / 400 dato inválido / 401 sin sesión / 403 sin permiso.
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail noEncontrado(NoSuchElementException e) {
        return problema(HttpStatus.NOT_FOUND, "No encontrado", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail conflicto(IllegalStateException e) {
        return problema(HttpStatus.CONFLICT, "Operación no permitida en el estado actual", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail datoInvalido(IllegalArgumentException e) {
        return problema(HttpStatus.BAD_REQUEST, "Dato inválido", e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    ProblemDetail sinPermiso(SecurityException e) {
        return problema(HttpStatus.FORBIDDEN, "Sin permiso", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridad(DataIntegrityViolationException e) {
        return problema(HttpStatus.CONFLICT, "Conflicto de datos", "Ya existe un registro con ese número. Reintente la operación");
    }

    /** Bean Validation: el detail indica qué campos fallaron, para que el frontend lo pueda mostrar. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return handleExceptionInternal(ex, problema(HttpStatus.BAD_REQUEST, "Datos inválidos", detalle), headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers,
                                                                          HttpStatusCode status, WebRequest request) {
        if (ex instanceof MissingRequestHeaderException h && SesionActual.HEADER.equals(h.getHeaderName())) {
            ProblemDetail p = problema(HttpStatus.UNAUTHORIZED, "Sin sesión", "Falta el header " + SesionActual.HEADER + ". Inicie sesión en POST /api/sesiones");
            return handleExceptionInternal(ex, p, headers, HttpStatus.UNAUTHORIZED, request);
        }
        return super.handleServletRequestBindingException(ex, headers, status, request);
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalle);
        p.setTitle(titulo);
        return p;
    }
}
