package ec.edu.espe.ventanilla.service;

import ec.edu.espe.ventanilla.dto.AperturaTurnoDTO;
import ec.edu.espe.ventanilla.dto.CierreTurnoDTO;
import ec.edu.espe.ventanilla.dto.DenominacionDTO;
import ec.edu.espe.ventanilla.enums.EstadoGeneral;
import ec.edu.espe.ventanilla.enums.EstadoTurno;
import ec.edu.espe.ventanilla.enums.TipoConteo;
import ec.edu.espe.ventanilla.enums.TipoTransaccion;
import ec.edu.espe.ventanilla.exception.BusinessException;
import ec.edu.espe.ventanilla.exception.ResourceNotFoundException;
import ec.edu.espe.ventanilla.exception.SystemException;
import ec.edu.espe.ventanilla.model.TurnoDenominaciones;
import ec.edu.espe.ventanilla.model.TurnosCajero;
import ec.edu.espe.ventanilla.repository.TurnoDenominacionesRepository;
import ec.edu.espe.ventanilla.repository.TurnosCajeroRepository;
import ec.edu.espe.ventanilla.repository.TransaccionesTurnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnosCajeroRepository turnosCajeroRepository;
    private final TurnoDenominacionesRepository turnoDenominacionesRepository;
    private final TransaccionesTurnoRepository transaccionesTurnoRepository;

    /**
     * Abre un nuevo turno para un cajero
     */
    @Transactional
    public TurnosCajero abrirTurno(AperturaTurnoDTO aperturaTurnoDTO) {
        log.info("Iniciando apertura de turno para cajero: {}, caja: {}",
                aperturaTurnoDTO.getCodigoCajero(), aperturaTurnoDTO.getCodigoCaja());

        try {
            // Validar que el cajero no tenga un turno abierto
            if (turnosCajeroRepository.existeTurnoAbierto(aperturaTurnoDTO.getCodigoCajero(), EstadoTurno.ABIERTO)) {
                throw new BusinessException(
                        "El cajero " + aperturaTurnoDTO.getCodigoCajero() + " ya tiene un turno abierto");
            }

            // Validar que el monto inicial coincida con las denominaciones
            BigDecimal montoCalculado = calcularMontoFromDenominaciones(aperturaTurnoDTO.getDenominacionesIniciales());
            if (montoCalculado.compareTo(aperturaTurnoDTO.getMontoInicial()) != 0) {
                throw new BusinessException("El monto inicial no coincide con el total de denominaciones. " +
                        "Esperado: " + aperturaTurnoDTO.getMontoInicial() + ", Calculado: " + montoCalculado);
            }

            // Generar código de turno automáticamente
            OffsetDateTime ahora = OffsetDateTime.now();
            String fechaFormateada = ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String codigoTurno = aperturaTurnoDTO.getCodigoCaja() + "-" +
                    aperturaTurnoDTO.getCodigoCajero() + "-" + fechaFormateada;

            log.info("Código de turno generado: {}", codigoTurno);

            // Crear turno
            TurnosCajero turno = new TurnosCajero();
            turno.setCodigoCaja(aperturaTurnoDTO.getCodigoCaja());
            turno.setCodigoCajero(aperturaTurnoDTO.getCodigoCajero());
            turno.setContrasenia(aperturaTurnoDTO.getContrasenia());
            turno.setCodigoTurno(codigoTurno);
            turno.setMontoInicial(aperturaTurnoDTO.getMontoInicial());
            turno.setInicioTurno(ahora);
            turno.setEstado(EstadoTurno.ABIERTO);

            TurnosCajero turnoGuardado = turnosCajeroRepository.save(turno);
            log.info("Turno creado con ID: {}", turnoGuardado.getId());

            // Guardar denominaciones de inicio
            guardarDenominacionesTurno(turnoGuardado.getId(),
                    aperturaTurnoDTO.getDenominacionesIniciales(), TipoConteo.INICIO);

            log.info("Turno abierto exitosamente para cajero: {}", aperturaTurnoDTO.getCodigoCajero());
            return turnoGuardado;

        } catch (BusinessException e) {
            log.error("Error de negocio al abrir turno: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error del sistema al abrir turno para cajero: {}",
                    aperturaTurnoDTO.getCodigoCajero(), e);
            throw new SystemException("Error interno del sistema al abrir turno", e);
        }
    }

    /**
     * Cierra un turno y realiza el cuadre de caja
     */
    @Transactional
    public TurnosCajero cerrarTurno(CierreTurnoDTO cierreTurnoDTO) {
        log.info("Iniciando cierre de turno: {}", cierreTurnoDTO.getCodigoTurnoACerrar());

        try {
            // Buscar turno abierto
            TurnosCajero turno = turnosCajeroRepository
                    .findByCodigoTurnoAndEstado(cierreTurnoDTO.getCodigoTurnoACerrar(), EstadoTurno.ABIERTO)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró turno abierto con código: " +
                            cierreTurnoDTO.getCodigoTurnoACerrar()));

            // Validar que el monto final coincida con las denominaciones
            BigDecimal montoCalculadoDenominaciones = calcularMontoFromDenominaciones(
                    cierreTurnoDTO.getDenominacionesFinales());
            if (montoCalculadoDenominaciones.compareTo(cierreTurnoDTO.getMontoFinal()) != 0) {
                throw new BusinessException("El monto final no coincide con el total de denominaciones. " +
                        "Esperado: " + cierreTurnoDTO.getMontoFinal() + ", Calculado: " + montoCalculadoDenominaciones);
            }

            // Calcular el monto que debería tener según las transacciones
            BigDecimal montoCalculadoSistema = calcularMontoEsperadoSistema(turno);

            log.info("Cuadre de caja - Turno ID: {}, Monto Sistema: {}, Monto Físico: {}",
                    turno.getId(), montoCalculadoSistema, cierreTurnoDTO.getMontoFinal());

            // Actualizar turno
            turno.setFinTurno(OffsetDateTime.now());
            turno.setMontoFinal(cierreTurnoDTO.getMontoFinal());
            turno.setEstado(EstadoTurno.CERRADO);

            TurnosCajero turnoActualizado = turnosCajeroRepository.save(turno);

            // Guardar denominaciones de fin
            guardarDenominacionesTurno(turno.getId(),
                    cierreTurnoDTO.getDenominacionesFinales(), TipoConteo.FIN);

            BigDecimal diferencia = cierreTurnoDTO.getMontoFinal().subtract(montoCalculadoSistema);

            if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
                log.info("Cuadre de caja perfecto para turno: {}", cierreTurnoDTO.getCodigoTurnoACerrar());
            } else {
                log.warn("Diferencia en cuadre de caja para turno: {}. Diferencia: {}",
                        cierreTurnoDTO.getCodigoTurnoACerrar(), diferencia);
            }

            return turnoActualizado;

        } catch (BusinessException | ResourceNotFoundException e) {
            log.error("Error al cerrar turno: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error del sistema al cerrar turno: {}",
                    cierreTurnoDTO.getCodigoTurnoACerrar(), e);
            throw new SystemException("Error interno del sistema al cerrar turno", e);
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
     * Calcula el monto esperado por el sistema basado en las transacciones
     */
    private BigDecimal calcularMontoEsperadoSistema(TurnosCajero turno) {
        BigDecimal montoInicial = turno.getMontoInicial();

        BigDecimal totalDepositos = transaccionesTurnoRepository
                .calcularTotalPorTipoTransaccion(turno.getId(), TipoTransaccion.DEPOSITO);

        BigDecimal totalRetiros = transaccionesTurnoRepository
                .calcularTotalPorTipoTransaccion(turno.getId(), TipoTransaccion.RETIRO);

        return montoInicial.add(totalDepositos).subtract(totalRetiros);
    }

    /**
     * Guarda las denominaciones de un turno
     */
    private void guardarDenominacionesTurno(Integer turnoId, List<DenominacionDTO> denominaciones,
            TipoConteo tipoConteo) {
        denominaciones.forEach(denominacionDTO -> {
            TurnoDenominaciones denominacion = new TurnoDenominaciones();
            denominacion.setTurnoId(turnoId);
            denominacion.setTipoConteo(tipoConteo);
            denominacion.setBillete(denominacionDTO.getBillete());
            denominacion.setCantidad(denominacionDTO.getCantidad());
            denominacion.setMonto(BigDecimal.valueOf(denominacionDTO.getBillete() * denominacionDTO.getCantidad()));
            denominacion.setEstado(EstadoGeneral.ACTIVO);

            turnoDenominacionesRepository.save(denominacion);
        });

        log.debug("Guardadas {} denominaciones de {} para turno ID: {}",
                denominaciones.size(), tipoConteo, turnoId);
    }
}