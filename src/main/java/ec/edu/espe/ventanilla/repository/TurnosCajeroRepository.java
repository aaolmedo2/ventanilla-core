package ec.edu.espe.ventanilla.repository;

import ec.edu.espe.ventanilla.model.TurnosCajero;
import ec.edu.espe.ventanilla.enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TurnosCajeroRepository extends JpaRepository<TurnosCajero, Integer> {

    /**
     * Busca un turno activo por código de turno
     */
    Optional<TurnosCajero> findByCodigoTurnoAndEstado(String codigoTurno, EstadoTurno estado);

    /**
     * Verifica si existe un turno abierto para un cajero específico
     */
    @Query("SELECT COUNT(t) > 0 FROM TurnosCajero t WHERE t.codigoCajero = :codigoCajero AND t.estado = :estado")
    boolean existeTurnoAbierto(@Param("codigoCajero") String codigoCajero, @Param("estado") EstadoTurno estado);
}