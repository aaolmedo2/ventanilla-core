package ec.edu.espe.ventanilla.repository;

import ec.edu.espe.ventanilla.model.TransaccionesTurno;
import ec.edu.espe.ventanilla.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface TransaccionesTurnoRepository extends JpaRepository<TransaccionesTurno, Integer> {

    /**
     * Calcula el total de depósitos para un turno específico
     */
    @Query("SELECT COALESCE(SUM(t.montoTotal), 0) FROM TransaccionesTurno t WHERE t.turnoId = :turnoId AND t.tipoTransaccion = :tipoTransaccion")
    BigDecimal calcularTotalPorTipoTransaccion(@Param("turnoId") Integer turnoId,
            @Param("tipoTransaccion") TipoTransaccion tipoTransaccion);
}