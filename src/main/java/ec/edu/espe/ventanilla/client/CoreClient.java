package ec.edu.espe.ventanilla.client;

import ec.edu.espe.ventanilla.dto.RespuestaCoreDTO;
import ec.edu.espe.ventanilla.dto.TransaccionCoreDTO;
import ec.edu.espe.ventanilla.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente REST para comunicarse con el sistema CORE
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoreClient {

    private final RestTemplate restTemplate;

    @Value("${core.base.url:http://localhost:85}")
    private String coreBaseUrl;

    @Value("${core.transacciones.endpoint:/api/core/transacciones}")
    private String transaccionesEndpoint;

    /**
     * Envía una transacción al CORE para validación y procesamiento
     * 
     * @param transaccionCoreDTO Datos de la transacción a enviar
     * @return Respuesta del CORE
     */
    public RespuestaCoreDTO enviarTransaccionAlCore(TransaccionCoreDTO transaccionCoreDTO) {
        String url = coreBaseUrl + transaccionesEndpoint;

        log.info("Enviando transacción al CORE: {} - Monto: {} - Canal: {}",
                transaccionCoreDTO.getTipoTransaccion(),
                transaccionCoreDTO.getMonto(),
                transaccionCoreDTO.getCanal());

        try {
            ResponseEntity<RespuestaCoreDTO> response = restTemplate.postForEntity(
                    url,
                    transaccionCoreDTO,
                    RespuestaCoreDTO.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                RespuestaCoreDTO respuesta = response.getBody();
                log.info("Respuesta recibida del CORE - Exitoso: {}, Mensaje: {}",
                        respuesta.isExitoso(), respuesta.getMensaje());
                return respuesta;
            } else {
                log.error("Error en respuesta del CORE. Status: {}", response.getStatusCode());
                throw new SystemException("Error al comunicarse con el CORE. Status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Error de comunicación con el CORE en URL: {}", url, e);

            // Crear respuesta de error por falta de comunicación
            RespuestaCoreDTO respuestaError = new RespuestaCoreDTO();
            respuestaError.setExitoso(false);
            respuestaError.setMensaje("Error de comunicación con el CORE: " + e.getMessage());
            respuestaError.setCodigoError("COMM_ERROR");

            return respuestaError;
        }
    }
}