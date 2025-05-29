package cp.proyecto.upsglam_backend.controllers;

import cp.proyecto.upsglam_backend.model.User;
import cp.proyecto.upsglam_backend.services.AuthService;
import cp.proyecto.upsglam_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> createUser(@RequestPart("user") User user, @RequestPart("photoUserProfile") MultipartFile userPhotoProfile) {
        return authService.create(user.getEmail(), user.getPassword())
                .flatMap(uid ->
                        userService.saveUserWithImage(user, uid, userPhotoProfile)
                                .map(saved -> {
                                    Map<String, Object> body = new HashMap<>();
                                    body.put("message", "Usuario creado con éxito");
                                    body.put("idUser", uid);
                                    body.put("email", user.getEmail());
                                    body.put("username", user.getUsername());
                                    return ResponseEntity
                                            .status(HttpStatus.CREATED)
                                            .body(body);
                                })
                )
                .onErrorResume(e -> {
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "Error al registrar usuario");
                    err.put("details", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(err));
                });
    }
}

