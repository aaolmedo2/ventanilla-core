package ec.edu.espe.ventanilla.controller;

import ec.edu.espe.ventanilla.exception.BusinessException;
import ec.edu.espe.ventanilla.exception.InvalidDataException;
import ec.edu.espe.ventanilla.exception.ResourceNotFoundException;
import ec.edu.espe.ventanilla.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para todos los controladores
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {

                log.error("Recurso no encontrado: {}", ex.getMessage());

                Map<String, Object> response = createErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Recurso no encontrado",
                                ex.getMessage(),
                                request.getDescription(false));

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Map<String, Object>> handleBusinessException(
                        BusinessException ex, WebRequest request) {

                log.error("Error de negocio: {}", ex.getMessage());

                Map<String, Object> response = createErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Error de negocio",
                                ex.getMessage(),
                                request.getDescription(false));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(InvalidDataException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidDataException(
                        InvalidDataException ex, WebRequest request) {

                log.error("Datos inválidos: {}", ex.getMessage());

                Map<String, Object> response = createErrorResponse(
                                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                                "Datos inválidos",
                                ex.getMessage(),
                                request.getDescription(false));

                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        @ExceptionHandler(SystemException.class)
        public ResponseEntity<Map<String, Object>> handleSystemException(
                        SystemException ex, WebRequest request) {

                log.error("Error del sistema: {}", ex.getMessage(), ex);

                Map<String, Object> response = createErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Error interno del sistema",
                                "Ha ocurrido un error interno. Por favor, contacte al administrador.",
                                request.getDescription(false));

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGlobalException(
                        Exception ex, WebRequest request) {

                log.error("Error no controlado: {}", ex.getMessage(), ex);

                Map<String, Object> response = createErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Error interno",
                                "Ha ocurrido un error inesperado. Por favor, contacte al administrador.",
                                request.getDescription(false));

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        private Map<String, Object> createErrorResponse(int status, String error, String message, String path) {
                Map<String, Object> response = new HashMap<>();
                response.put("timestamp", OffsetDateTime.now());
                response.put("status", status);
                response.put("error", error);
                response.put("message", message);
                response.put("path", path.replace("uri=", ""));
                return response;
        }
}