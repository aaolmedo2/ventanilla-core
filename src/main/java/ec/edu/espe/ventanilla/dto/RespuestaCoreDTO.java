package ec.edu.espe.ventanilla.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para recibir respuestas del CORE
 * Respuesta de POST http://localhost:85/api/transacciones-core
 */
@Getter
@Setter
@ToString
public class RespuestaCoreDTO {
    private boolean exitoso;
    private String mensaje;
    private String codigoError;
    private String transaccionId;
}