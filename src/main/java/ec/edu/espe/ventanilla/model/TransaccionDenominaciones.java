package ec.edu.espe.ventanilla.model;

import ec.edu.espe.ventanilla.enums.EstadoGeneral;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad que representa el desglose de billetes para cada transacción
 * individual.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TransaccionDenominaciones")
public class TransaccionDenominaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "transaccionId", nullable = false)
    private Integer transaccionId;

    @Column(name = "billete", nullable = false)
    private Integer billete;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    // Relación Many-to-One con TransaccionesTurno
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccionId", insertable = false, updatable = false)
    private TransaccionesTurno transaccion;

    // Constructor vacío
    public TransaccionDenominaciones() {
    }

    // Constructor con parámetro id
    public TransaccionDenominaciones(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TransaccionDenominaciones that = (TransaccionDenominaciones) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}