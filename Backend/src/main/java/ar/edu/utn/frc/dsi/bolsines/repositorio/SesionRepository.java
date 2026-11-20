package ar.edu.utn.frc.dsi.bolsines.repositorio;

import ar.edu.utn.frc.dsi.bolsines.modelo.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SesionRepository extends JpaRepository<Sesion, UUID> {
}
