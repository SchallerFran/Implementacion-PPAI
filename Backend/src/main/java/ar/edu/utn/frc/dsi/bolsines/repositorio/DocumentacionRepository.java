package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.modelo.Documentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface DocumentacionRepository extends JpaRepository<Documentacion, Long> {

    Optional<Documentacion> findByNumero(Integer numero);

    List<Documentacion> findByCmOrigenOrderByNumero(ComisionMedica cmOrigen);

    // ponytail: max+1 con constraint unique; dos altas simultáneas fallan con 409 en vez de duplicar. Secuencia si hay concurrencia real
    @Query("select coalesce(max(d.numero), 0) + 1 from Documentacion d")
    int siguienteNumero();

    default Documentacion porNumero(Integer numero) {
        return findByNumero(numero).orElseThrow(() -> new NoSuchElementException("No existe la documentación " + numero));
    }
}
