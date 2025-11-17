package ec.edu.espe.ventanilla.model;

import ec.edu.espe.ventanilla.enums.TipoTransaccion;
import ec.edu.espe.ventanilla.enums.EstadoGeneral;
import ec.edu.espe.ventanilla.enums.EstadoTransaccion;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Entidad que representa el log de cada movimiento (depósito/retiro) asociado a
 * un turno.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TransaccionesTurno")
public class TransaccionesTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "turnoId", nullable = false)
    private Integer turnoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoTransaccion", nullable = false, length = 10)
    private TipoTransaccion tipoTransaccion;

    @Column(name = "montoTotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fechaTransaccion", nullable = false)
    private OffsetDateTime fechaTransaccion;

    @Column(name = "cuentaReferencia", nullable = false, length = 12)
    private String cuentaReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoGeneral estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estadoCore", length = 15)
    private EstadoTransaccion estadoCore;

    @Column(name = "mensajeCore", length = 500)
    private String mensajeCore;

    // Relación Many-to-One con TurnosCajero
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turnoId", insertable = false, updatable = false)
    private TurnosCajero turno;

    // Constructor vacío
    public TransaccionesTurno() {
    }

    // Constructor con parámetro id
    public TransaccionesTurno(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TransaccionesTurno that = (TransaccionesTurno) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}