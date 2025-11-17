package ec.edu.espe.ventanilla.repository;

import ec.edu.espe.ventanilla.model.TurnoDenominaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnoDenominacionesRepository extends JpaRepository<TurnoDenominaciones, Integer> {
}