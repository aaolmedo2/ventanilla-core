package ec.edu.espe.ventanilla.exception;

/**
 * Excepción personalizada para datos inválidos o malformados
 */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException() {
        super();
    }

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDataException(Throwable cause) {
        super(cause);
    }
}