package ec.edu.espe.ventanilla.controller;

import ec.edu.espe.ventanilla.dto.TransaccionDTO;
import ec.edu.espe.ventanilla.model.TransaccionesTurno;
import ec.edu.espe.ventanilla.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    /**
     * Registra una nueva transacción (depósito o retiro)
     * POST /api/transacciones
     */
    @PostMapping
    public ResponseEntity<TransaccionesTurno> registrarTransaccion(@RequestBody TransaccionDTO transaccionDTO) {
        log.info("Recibida petición para registrar transacción tipo: {} en turno: {}",
                transaccionDTO.getTipoTransaccion(), transaccionDTO.getCodigoTurnoActivo());

        TransaccionesTurno transaccionCreada = transaccionService.registrarTransaccion(transaccionDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionCreada);
    }
}