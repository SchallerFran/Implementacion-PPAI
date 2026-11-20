package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Long> {

    Optional<Estado> findByAmbitoAndNombre(String ambito, String nombre);

    default Estado bolsin(String nombre) { return buscar(Estado.AMBITO_BOLSIN, nombre); }

    default Estado remito(String nombre) { return buscar(Estado.AMBITO_REMITO, nombre); }

    default Estado documentacion(String nombre) { return buscar(Estado.AMBITO_DOCUMENTACION, nombre); }

    private Estado buscar(String ambito, String nombre) {
        return findByAmbitoAndNombre(ambito, nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + nombre + " de ámbito " + ambito + " en la base"));
    }
}
