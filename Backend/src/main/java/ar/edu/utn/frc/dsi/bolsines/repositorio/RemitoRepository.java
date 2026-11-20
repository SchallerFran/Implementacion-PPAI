package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.modelo.Remito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface RemitoRepository extends JpaRepository<Remito, Long> {

    Optional<Remito> findByNumero(Integer numero);

    List<Remito> findByOrigenOrDestinoOrderByNumero(ComisionMedica origen, ComisionMedica destino);

    @Query("select coalesce(max(r.numero), 0) + 1 from Remito r")
    int siguienteNumero();

    default Remito porNumero(Integer numero) {
        return findByNumero(numero).orElseThrow(() -> new NoSuchElementException("No existe el remito " + numero));
    }
}
