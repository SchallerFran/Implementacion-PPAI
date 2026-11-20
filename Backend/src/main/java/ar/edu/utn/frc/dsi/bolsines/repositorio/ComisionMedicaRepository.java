package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.ComisionMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ComisionMedicaRepository extends JpaRepository<ComisionMedica, Long> {

    Optional<ComisionMedica> findByCodigo(String codigo);

    List<ComisionMedica> findAllByOrderByNombre();

    default ComisionMedica porCodigo(String codigo) {
        return findByCodigo(codigo).orElseThrow(() -> new NoSuchElementException("No existe la comisión médica " + codigo));
    }
}
