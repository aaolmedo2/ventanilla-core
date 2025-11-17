package ec.edu.espe.ventanilla.model;

import ec.edu.espe.ventanilla.enums.TipoConteo;
import ec.edu.espe.ventanilla.enums.EstadoGeneral;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa el desglose de billetes (conteo) al INICIO y FIN de
 * cada turno.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TurnoDenominaciones")
public class TurnoDenominaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "turnoId", nullable = false)
    private Integer turnoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoConteo", nullable = false, length = 10)
    private TipoConteo tipoConteo;

    @Column(name = "billete", nullable = false)
    private Integer billete;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    // Relación Many-to-One con TurnosCajero
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turnoId", insertable = false, updatable = false)
    private TurnosCajero turno;

    // Constructor vacío
    public TurnoDenominaciones() {
    }

    // Constructor con parámetro id
    public TurnoDenominaciones(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TurnoDenominaciones that = (TurnoDenominaciones) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}