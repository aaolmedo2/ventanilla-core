package ec.edu.espe.ventanilla.model;

import ec.edu.espe.ventanilla.enums.EstadoTurno;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Entidad que representa la sesión de trabajo (turno) de un cajero en una caja
 * específica.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TurnosCajero")
public class TurnosCajero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigoCaja", nullable = false, length = 50)
    private String codigoCaja;

    @Column(name = "codigoCajero", nullable = false, length = 50)
    private String codigoCajero;

    @Column(name = "contrasenia", nullable = false, length = 100)
    private String contrasenia;

    @Column(name = "codigoTurno", nullable = false, length = 100)
    private String codigoTurno;

    @Column(name = "inicioTurno", nullable = false)
    private OffsetDateTime inicioTurno;

    @Column(name = "montoInicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "finTurno")
    private OffsetDateTime finTurno;

    @Column(name = "montoFinal", precision = 12, scale = 2)
    private BigDecimal montoFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoTurno estado = EstadoTurno.ABIERTO;

    // Constructor vacío
    public TurnosCajero() {
    }

    // Constructor con parámetro id
    public TurnosCajero(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TurnosCajero that = (TurnosCajero) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}