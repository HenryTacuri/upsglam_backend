package cp.proyecto.upsglam_backend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import cp.proyecto.upsglam_backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Service
public class UserService {

    @Autowired
    private Firestore firestore;

    private CollectionReference getUserCollection() {
        return firestore.collection("users");
    }

    public Mono<Boolean> saveUserWithImage(User user, String userUID, MultipartFile userPhotoProfile) {
        return uploadImageToFirebase(userPhotoProfile, userUID)
                .flatMap(imageUrl -> saveUserData(user, userUID, imageUrl));
    }

    public Mono<String> uploadImageToFirebase(MultipartFile file, String userUID) {
        return Mono.fromCallable(() -> {
            String fileName = "PhotoProfile/" + userUID;
            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.create(fileName, file.getInputStream(), file.getContentType());


            String DOWNLOAD_URL  = "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName() + "/o/%s?alt=media";

            return String.format(DOWNLOAD_URL , URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> saveUserData(User user, String userUID, String imageUrl) {
        return Mono.fromCallable(() -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("firstname", user.getFirstName());
            userData.put("lastname", user.getLastName());
            userData.put("gender", user.getGender());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("photoUserProfile", imageUrl); // almacena la URL de la imagen

            ApiFuture<WriteResult> future = getUserCollection().document(userUID).set(userData);
            future.get(); // bloquear dentro de hilo elástico
            return true;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Map<String, Object>> getDataUser(String documentId) {
        return Mono.fromCallable(() -> {
            ApiFuture<DocumentSnapshot> future = getUserCollection().document(documentId).get();
            DocumentSnapshot document = future.get(); // operación bloqueante


            if (document.exists()) {
                return document.getData();
            } else {
                throw new RuntimeException("Documento no encontrado");
            }
        }).subscribeOn(Schedulers.boundedElastic()); // evitar bloquear el hilo reactor principal
    }

    public Mono<Map<String, Object>> getDataProfile(String userUID) {
        return Mono.fromCallable(() -> {
            ApiFuture<DocumentSnapshot> future = getUserCollection().document(userUID).get();
            DocumentSnapshot document = future.get(); // operación bloqueante

            if (document.exists()) {
                return document.getData();
            } else {
                throw new RuntimeException("Documento no encontrado");
            }
        }).subscribeOn(Schedulers.boundedElastic()); // evitar bloquear el hilo reactor principal
    }

}


