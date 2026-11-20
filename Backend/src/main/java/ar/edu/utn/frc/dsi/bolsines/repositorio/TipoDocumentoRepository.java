package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    Optional<TipoDocumento> findByNombre(String nombre);

    List<TipoDocumento> findAllByOrderByNombre();
}
