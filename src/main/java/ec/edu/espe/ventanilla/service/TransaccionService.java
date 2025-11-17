package ec.edu.espe.ventanilla.service;

import ec.edu.espe.ventanilla.dto.DenominacionDTO;
import ec.edu.espe.ventanilla.dto.TransaccionDTO;
import ec.edu.espe.ventanilla.dto.TransaccionCoreDTO;
import ec.edu.espe.ventanilla.dto.RespuestaCoreDTO;
import ec.edu.espe.ventanilla.enums.EstadoGeneral;
import ec.edu.espe.ventanilla.enums.EstadoTurno;
import ec.edu.espe.ventanilla.enums.EstadoTransaccion;
import ec.edu.espe.ventanilla.exception.BusinessException;
import ec.edu.espe.ventanilla.exception.ResourceNotFoundException;
import ec.edu.espe.ventanilla.exception.SystemException;
import ec.edu.espe.ventanilla.model.TransaccionDenominaciones;
import ec.edu.espe.ventanilla.model.TransaccionesTurno;
import ec.edu.espe.ventanilla.model.TurnosCajero;
import ec.edu.espe.ventanilla.repository.TransaccionDenominacionesRepository;
import ec.edu.espe.ventanilla.repository.TransaccionesTurnoRepository;
import ec.edu.espe.ventanilla.repository.TurnosCajeroRepository;
import ec.edu.espe.ventanilla.client.CoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionesTurnoRepository transaccionesTurnoRepository;
    private final TransaccionDenominacionesRepository transaccionDenominacionesRepository;
    private final TurnosCajeroRepository turnosCajeroRepository;
    private final CoreClient coreClient;

    /**
     * Registra una transacción (depósito o retiro)
     */
    @Transactional
    public TransaccionesTurno registrarTransaccion(TransaccionDTO transaccionDTO) {
        log.info("Iniciando registro de transacción tipo: {} para turno: {}",
                transaccionDTO.getTipoTransaccion(), transaccionDTO.getCodigoTurnoActivo());

        try {
            // Buscar turno activo
            TurnosCajero turno = turnosCajeroRepository
                    .findByCodigoTurnoAndEstado(transaccionDTO.getCodigoTurnoActivo(), EstadoTurno.ABIERTO)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró turno abierto con código: " +
                            transaccionDTO.getCodigoTurnoActivo()));

            // Validar que el monto total coincida con las denominaciones
            BigDecimal montoCalculado = calcularMontoFromDenominaciones(transaccionDTO.getDenominaciones());
            if (montoCalculado.compareTo(transaccionDTO.getMontoTotal()) != 0) {
                throw new BusinessException("El monto total no coincide con el total de denominaciones. " +
                        "Esperado: " + transaccionDTO.getMontoTotal() + ", Calculado: " + montoCalculado);
            }

            // Crear transacción inicial (estado PENDIENTE)
            OffsetDateTime fechaTransaccion = OffsetDateTime.now();
            TransaccionesTurno transaccion = new TransaccionesTurno();
            transaccion.setTurnoId(turno.getId());
            transaccion.setTipoTransaccion(transaccionDTO.getTipoTransaccion());
            transaccion.setMontoTotal(transaccionDTO.getMontoTotal());
            transaccion.setFechaTransaccion(fechaTransaccion);
            transaccion.setCuentaReferencia(transaccionDTO.getCuentaReferencia());
            transaccion.setEstado(EstadoGeneral.ACTIVO);
            transaccion.setEstadoCore(EstadoTransaccion.PENDIENTE);

            TransaccionesTurno transaccionGuardada = transaccionesTurnoRepository.save(transaccion);
            log.info("Transacción creada con ID: {} - Estado: PENDIENTE", transaccionGuardada.getId());

            // Enviar al CORE para validación
            try {
                TransaccionCoreDTO transaccionCore = new TransaccionCoreDTO();
                transaccionCore.setTipoTransaccion(transaccionDTO.getTipoTransaccion());
                transaccionCore.setMonto(transaccionDTO.getMontoTotal());
                transaccionCore.setCanal(turno.getCodigoTurno());
                transaccionCore.setDescripcion(turno.getCodigoTurno());

                // Configurar cuentas según tipo de transacción
                String numeroCuenta = transaccionDTO.getCuentaReferencia();
                if (transaccionDTO.getTipoTransaccion().equals(ec.edu.espe.ventanilla.enums.TipoTransaccion.DEPOSITO)) {
                    // Para depósitos: solo cuenta destino (donde entra el dinero)
                    transaccionCore.setCuentaDestinoId(numeroCuenta);
                    transaccionCore.setCuentaOrigenId(null); // Explícitamente null
                } else if (transaccionDTO.getTipoTransaccion()
                        .equals(ec.edu.espe.ventanilla.enums.TipoTransaccion.RETIRO)) {
                    // Para retiros: solo cuenta origen (de donde sale el dinero)
                    transaccionCore.setCuentaOrigenId(numeroCuenta);
                    transaccionCore.setCuentaDestinoId(null); // Explícitamente null
                }

                log.info("Enviando transacción ID: {} al CORE para validación", transaccionGuardada.getId());
                RespuestaCoreDTO respuestaCore = coreClient.enviarTransaccionAlCore(transaccionCore);

                // Actualizar estado según respuesta del CORE
                if (respuestaCore.isExitoso()) {
                    transaccionGuardada.setEstadoCore(EstadoTransaccion.COMPLETADO);
                    transaccionGuardada.setMensajeCore(respuestaCore.getMensaje());
                    log.info("Transacción ID: {} COMPLETADA exitosamente en CORE", transaccionGuardada.getId());
                } else {
                    transaccionGuardada.setEstadoCore(EstadoTransaccion.ERROR);
                    transaccionGuardada.setMensajeCore(respuestaCore.getMensaje());
                    log.warn("Transacción ID: {} RECHAZADA por CORE: {}",
                            transaccionGuardada.getId(), respuestaCore.getMensaje());
                }

            } catch (Exception e) {
                log.error("Error al comunicarse con CORE para transacción ID: {}", transaccionGuardada.getId(), e);
                transaccionGuardada.setEstadoCore(EstadoTransaccion.ERROR);
                transaccionGuardada.setMensajeCore("Error de comunicación con CORE: " + e.getMessage());
            }

            // Guardar estado final
            transaccionGuardada = transaccionesTurnoRepository.save(transaccionGuardada);

            // Guardar denominaciones de la transacción
            guardarDenominacionesTransaccion(transaccionGuardada.getId(), transaccionDTO.getDenominaciones());

            log.info("Transacción {} registrada - Estado CORE: {} - Cuenta: {}",
                    transaccionDTO.getTipoTransaccion(),
                    transaccionGuardada.getEstadoCore(),
                    transaccionDTO.getCuentaReferencia());

            return transaccionGuardada;

        } catch (BusinessException | ResourceNotFoundException e) {
            log.error("Error al registrar transacción: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error del sistema al registrar transacción tipo: {} para turno: {}",
                    transaccionDTO.getTipoTransaccion(), transaccionDTO.getCodigoTurnoActivo(), e);
            throw new SystemException("Error interno del sistema al registrar transacción", e);
        }
    }

    /**
     * Calcula el monto total a partir de una lista de denominaciones
     */
    private BigDecimal calcularMontoFromDenominaciones(List<DenominacionDTO> denominaciones) {
        return denominaciones.stream()
                .map(d -> BigDecimal.valueOf(d.getBillete() * d.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Guarda las denominaciones de una transacción
     */
    private void guardarDenominacionesTransaccion(Integer transaccionId, List<DenominacionDTO> denominaciones) {
        denominaciones.forEach(denominacionDTO -> {
            TransaccionDenominaciones denominacion = new TransaccionDenominaciones();
            denominacion.setTransaccionId(transaccionId);
            denominacion.setBillete(denominacionDTO.getBillete());
            denominacion.setCantidad(denominacionDTO.getCantidad());
            denominacion.setMonto(BigDecimal.valueOf(denominacionDTO.getBillete() * denominacionDTO.getCantidad()));
            denominacion.setEstado(EstadoGeneral.ACTIVO);

            transaccionDenominacionesRepository.save(denominacion);
        });

        log.debug("Guardadas {} denominaciones para transacción ID: {}",
                denominaciones.size(), transaccionId);
    }
}