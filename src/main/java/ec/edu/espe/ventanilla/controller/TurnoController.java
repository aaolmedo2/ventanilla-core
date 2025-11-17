package ec.edu.espe.ventanilla.controller;

import ec.edu.espe.ventanilla.dto.AperturaTurnoDTO;
import ec.edu.espe.ventanilla.dto.CierreTurnoDTO;
import ec.edu.espe.ventanilla.model.TurnosCajero;
import ec.edu.espe.ventanilla.service.TurnoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    /**
     * Abre un nuevo turno
     * POST /api/turnos/abrir
     */
    @PostMapping("/abrir")
    public ResponseEntity<TurnosCajero> abrirTurno(@RequestBody AperturaTurnoDTO aperturaTurnoDTO) {
        log.info("Recibida petición para abrir turno - Cajero: {}, Caja: {}",
                aperturaTurnoDTO.getCodigoCajero(), aperturaTurnoDTO.getCodigoCaja());

        TurnosCajero turnoCreado = turnoService.abrirTurno(aperturaTurnoDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(turnoCreado);
    }

    /**
     * Cierra un turno existente
     * PUT /api/turnos/cerrar
     */
    @PutMapping("/cerrar")
    public ResponseEntity<TurnosCajero> cerrarTurno(@RequestBody CierreTurnoDTO cierreTurnoDTO) {
        log.info("Recibida petición para cerrar turno: {}", cierreTurnoDTO.getCodigoTurnoACerrar());

        TurnosCajero turnoCerrado = turnoService.cerrarTurno(cierreTurnoDTO);

        return ResponseEntity.ok(turnoCerrado);
    }
}