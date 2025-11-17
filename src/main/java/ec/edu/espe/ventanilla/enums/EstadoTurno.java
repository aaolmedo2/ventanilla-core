package ec.edu.espe.ventanilla.enums;

/**
 * Enumeración para el estado del turno
 * Basado en la restricción: check (estado in ('ABIERTO','CERRADO'))
 * Note: En el SQL aparece como ACTIVO/INACTIVO pero el comentario indica
 * ABIERTO/CERRADO
 */
public enum EstadoTurno {
    ABIERTO,
    CERRADO
}