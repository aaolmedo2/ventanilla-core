package ec.edu.espe.ventanilla.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la petición de cierre de turno
 * PUT /api/turnos/cerrar
 */
@Getter
@Setter
@ToString
public class CierreTurnoDTO {
    private String codigoTurnoACerrar;
    private BigDecimal montoFinal;
    private List<DenominacionDTO> denominacionesFinales;
}