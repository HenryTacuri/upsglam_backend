package cp.proyecto.upsglam_backend.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import cp.proyecto.upsglam_backend.dto.FirebaseSignInRequest;
import cp.proyecto.upsglam_backend.dto.FirebaseSignInResponse;
import cp.proyecto.upsglam_backend.exceptions.AccountAlreadyExistsException;
import cp.proyecto.upsglam_backend.exceptions.InvalidLoginCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.google.firebase.auth.UserRecord.CreateRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final String API_KEY_PARAM = "key";
    private static final String INVALID_CREDENTIALS_ERROR = "INVALID_LOGIN_CREDENTIALS";
    private static final String SIGN_IN_BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword";

    @Value("${firebase.api.key}")
    private String webApiKey;

    @Autowired
    private FirebaseAuth firebaseAuth;

    private final WebClient webClient;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(SIGN_IN_BASE_URL).build();
    }


    public Mono<String> create(String emailId, String password) {
        return Mono.fromCallable(() -> {
            CreateRequest request = new CreateRequest()
                    .setEmail(emailId)
                    .setPassword(password)
                    .setEmailVerified(true);

            try {
                return firebaseAuth.createUser(request).getUid();
            } catch (FirebaseAuthException e) {
                if (e.getMessage().contains("EMAIL_EXISTS")) {
                    throw new AccountAlreadyExistsException("Account already exists");
                }
                throw e;
            }
        }).subscribeOn(Schedulers.boundedElastic()); // Ejecuta en un hilo separado
    }


    public Mono<FirebaseSignInResponse> login(String email, String password) {
        FirebaseSignInRequest requestBody = new FirebaseSignInRequest(email, password, true);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam(API_KEY_PARAM, webApiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    if (errorBody.contains(INVALID_CREDENTIALS_ERROR)) {
                                        return Mono.error(new InvalidLoginCredentialsException("Credenciales inválidas"));
                                    } else {
                                        return Mono.error(new RuntimeException("Error desconocido al autenticar: " + errorBody));
                                    }
                                })
                )
                .bodyToMono(FirebaseSignInResponse.class);
    }


    public Mono<Void> logoutUser(String uid) {
        return Mono.fromCallable(() -> {
                    FirebaseAuth.getInstance().revokeRefreshTokens(uid);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }


}


