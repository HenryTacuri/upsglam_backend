package cp.proyecto.upsglam_backend.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import cp.proyecto.upsglam_backend.services.AuthService;
import cp.proyecto.upsglam_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody Map<String, String> userCredentials) {
        String email = userCredentials.get("email");
        String password = userCredentials.get("password");

        return authService.login(email, password)
                .flatMap(response -> {
                    String idToken = response.idToken();

                    FirebaseToken decodedToken;
                    try {
                        decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                    } catch (FirebaseAuthException e) {
                        return Mono.error(new RuntimeException("Token inválido: " + e.getMessage()));
                    }

                    String uid = decodedToken.getUid();

                    return userService.getDataUser(uid)
                            .map(dataUser -> {
                                Map<String, String> dataResponse = new HashMap<>();
                                dataResponse.put("message", "Login exitoso");
                                dataResponse.put("userToken", idToken);
                                dataResponse.put("userUID", uid);
                                dataResponse.put("username", String.valueOf(dataUser.get("username")));
                                dataResponse.put("userEmail", email);
                                dataResponse.put("photoUserProfile", String.valueOf(dataUser.get("photoUserProfile")));

                                return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataResponse);
                            });
                })
                .onErrorResume(e -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error));
                });
    }



    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, String>>> logoutUser(@RequestBody Map<String, String> payload) {
        String uid = payload.get("userUID");
        return authService.logoutUser(uid)
                .then(Mono.just(
                        ResponseEntity.ok(Map.of("message", "Logout exitoso"))))
                .onErrorResume(ex -> {
                    String errorMsg = (ex instanceof FirebaseAuthException)
                            ? ex.getMessage()
                            : "Error inesperado al procesar logout.";
                    return Mono.just(
                            ResponseEntity
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("error", "Error al revocar tokens: " + errorMsg))
                    );
                });
    }

}
