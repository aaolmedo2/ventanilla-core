package ec.edu.espe.ventanilla.dto;

import ec.edu.espe.ventanilla.enums.TipoTransaccion;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

/**
 * DTO para enviar transacciones al CORE según el formato esperado:
 * - cuentaOrigenId: para retiros (cuenta de donde se saca dinero)
 * - cuentaDestinoId: para depósitos (cuenta donde se deposita dinero)
 * - monto: monto de la transacción
 * - canal: código del turno
 * - descripcion: código del turno
 * - tipoTransaccion: DEPOSITO/RETIRO
 * 
 * POST http://localhost:85/api/core/transacciones
 */
@Getter
@Setter
@ToString
public class TransaccionCoreDTO {

    private String cuentaOrigenId;

    private String cuentaDestinoId;

    private BigDecimal monto;

    private String canal;

    private String descripcion;

    private TipoTransaccion tipoTransaccion;
}