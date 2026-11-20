package ar.edu.utn.frc.dsi.bolsines.api;

import ar.edu.utn.frc.dsi.bolsines.api.Dtos.ComisionMedicaDto;
import ar.edu.utn.frc.dsi.bolsines.api.Dtos.TipoDocumentoDto;
import ar.edu.utn.frc.dsi.bolsines.repositorio.ComisionMedicaRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.TipoDocumentoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Datos de soporte para combos del frontend (consulta de CM y tipos de documento). */
@RestController
@RequestMapping("/api")
public class CatalogoController {
    private final ComisionMedicaRepository comisiones;
    private final TipoDocumentoRepository tipos;

    public CatalogoController(ComisionMedicaRepository comisiones, TipoDocumentoRepository tipos) {
        this.comisiones = comisiones;
        this.tipos = tipos;
    }

    @GetMapping("/comisiones-medicas")
    @Transactional(readOnly = true)
    public List<ComisionMedicaDto> comisionesMedicas() {
        return comisiones.findAllByOrderByNombre().stream().map(ComisionMedicaDto::de).toList();
    }

    @GetMapping("/tipos-documento")
    public List<TipoDocumentoDto> tiposDocumento() {
        return tipos.findAllByOrderByNombre().stream().map(TipoDocumentoDto::de).toList();
    }
}
