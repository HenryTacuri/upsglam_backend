package cp.proyecto.upsglam_backend.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProcessPhotoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String flaskUrl = "http://python_app:5000/procesar-imagen";

    public byte[] procesarImagenEnFlask(MultipartFile imagen, String tipoFiltro) throws Exception {
        // Convertimos el MultipartFile a un recurso con nombre y tamaño
        ByteArrayResource imageAsResource = new ByteArrayResource(imagen.getBytes()) {
            @Override
            public String getFilename() {
                return imagen.getOriginalFilename();
            }
            @Override
            public long contentLength() {
                return imagen.getSize();
            }
        };

        // Construimos el cuerpo multipart
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("imagen", imageAsResource);
        body.add("tipoFiltro", tipoFiltro);

        // Cabeceras para multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        // Llamada POST a Flask
        ResponseEntity<byte[]> response =
                restTemplate.postForEntity(flaskUrl, requestEntity, byte[].class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error al procesar imagen en Flask: " + response.getStatusCode());
        }

        return response.getBody();
    }

}
