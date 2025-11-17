package ec.edu.espe.ventanilla.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para representar una denominación de billete con su cantidad
 */
@Getter
@Setter
@ToString
public class DenominacionDTO {
    private Integer billete;
    private Integer cantidad;
}