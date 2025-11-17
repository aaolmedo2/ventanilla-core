package ec.edu.espe.ventanilla.repository;

import ec.edu.espe.ventanilla.model.TransaccionDenominaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionDenominacionesRepository extends JpaRepository<TransaccionDenominaciones, Integer> {
}