package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.modelo.ComisionMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface BolsinRepository extends JpaRepository<Bolsin, Long> {

    Optional<Bolsin> findByNumeroBolsin(Integer numeroBolsin);

    List<Bolsin> findByOrigenOrDestinoOrderByNumeroBolsin(ComisionMedica origen, ComisionMedica destino);

    boolean existsByNumeroPrecinto(String numeroPrecinto);

    @Query("select coalesce(max(b.numeroBolsin), 0) + 1 from Bolsin b")
    int siguienteNumero();

    default Bolsin porNumero(Integer numero) {
        return findByNumeroBolsin(numero).orElseThrow(() -> new NoSuchElementException("No existe el bolsín " + numero));
    }
}
