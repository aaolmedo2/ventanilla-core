package ec.edu.espe.ventanilla.dto;

import ec.edu.espe.ventanilla.enums.TipoTransaccion;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la petición de transacciones (depósito/retiro)
 * POST /api/transacciones
 */
@Getter
@Setter
@ToString
public class TransaccionDTO {
    private String codigoTurnoActivo;
    private TipoTransaccion tipoTransaccion;
    private BigDecimal montoTotal;
    private String cuentaReferencia;
    private List<DenominacionDTO> denominaciones;
}