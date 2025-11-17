package ec.edu.espe.ventanilla.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la petición de apertura de turno
 * POST /api/turnos/abrir
 */
@Getter
@Setter
@ToString
public class AperturaTurnoDTO {
    private String codigoCaja;
    private String codigoCajero;
    private String contrasenia;
    private BigDecimal montoInicial;
    private List<DenominacionDTO> denominacionesIniciales;
}