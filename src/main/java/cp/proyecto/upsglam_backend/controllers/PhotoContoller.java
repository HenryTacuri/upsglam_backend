package cp.proyecto.upsglam_backend.controllers;

import cp.proyecto.upsglam_backend.services.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/photos")
public class PhotoContoller {

    private final PhotoService photoService;

    @Autowired
    public PhotoContoller(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> createUser(@RequestPart("userUID") String userUID, @RequestPart("photoUser") MultipartFile photoUser) {
        return photoService.saveUserPhoto(userUID, photoUser)
                .map(photoResp -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("photos", photoResp.userPhotos().getPhotos());
                    return ResponseEntity
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body);
                })
                .onErrorResume(e ->
                        Mono.just(ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", e.getMessage()))
                        )
                );

    }

    @GetMapping("/getPhotosUser/{userUID}")
    public Mono<ResponseEntity<Map<String, Object>>> getUserPhotos(@PathVariable String userUID) {
        return photoService.getUserPhotos(userUID)
                .map(data -> ResponseEntity.ok(data))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Collections.singletonMap("error", e.getMessage()))
                        )
                );
    }

    @GetMapping(value = "/getAllPhotos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Map<String, Object>>> getAllPhotos() {
        // collectList() convierte el Flux<Map> en un Mono<List<Map>>
        return photoService
                .getAllUserPhotos()
                .collectList();
    }

}
