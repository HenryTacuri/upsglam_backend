package cp.proyecto.upsglam_backend.controllers;

import cp.proyecto.upsglam_backend.services.ProcessPhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/processphoto")
public class ProcessPhotoController {

    private final ProcessPhotoService processPhotoService;


    @Autowired
    public ProcessPhotoController(ProcessPhotoService processPhotoService) {
        this.processPhotoService = processPhotoService;
    }

    @PostMapping("/pycuda")
    public ResponseEntity<byte[]> subirYProcesar(
            @RequestParam("imagen") MultipartFile imagen,
            @RequestParam("tipoFiltro") String tipoFiltro
    ) throws Exception {

        byte[] resultadoPng = processPhotoService.procesarImagenEnFlask(imagen, tipoFiltro);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        // Opcional: forzar descarga con
        // headers.setContentDispositionFormData("attachment", "resultado.png");

        return new ResponseEntity<>(resultadoPng, headers, HttpStatus.OK);
    }

}

