package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.Empleado;
import ar.edu.utn.frc.dsi.bolsines.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByUsuario(Usuario usuario);
}
