package cp.proyecto.upsglam_backend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import cp.proyecto.upsglam_backend.dto.PhotoResponse;
import cp.proyecto.upsglam_backend.model.Comment;
import cp.proyecto.upsglam_backend.model.Like;
import cp.proyecto.upsglam_backend.model.Photo;
import cp.proyecto.upsglam_backend.model.UserPhotos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class PhotoService {

    @Autowired
    private Firestore firestore;

    private CollectionReference getUserCollection() {
        return firestore.collection("photos");
    }

    public Mono<PhotoResponse> saveUserPhoto(String userUID, MultipartFile userPhotoProfile) {
        return uploadPhotoToFirebase(userPhotoProfile, userUID)
                .flatMap(imageUrl ->
                        // savePhotoData devuelve Mono<PhotoResponse>
                        savePhotoData(userUID, imageUrl)
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> uploadPhotoToFirebase(MultipartFile file, String userUID) {
        return Mono.fromCallable(() -> {
            String fileName = "UsersPhotos/" + userUID + "_" + UUID.randomUUID().toString();
            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.create(fileName, file.getInputStream(), file.getContentType());


            String DOWNLOAD_URL  = "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName() + "/o/%s?alt=media";

            return String.format(DOWNLOAD_URL , URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PhotoResponse> savePhotoData(String userUID, String imageUrl) {
        return Mono.fromCallable(() -> {
            List<Comment> comments = new ArrayList<>();
            List<Like> likes = new ArrayList<>();

            Like like = new Like();
            like.setUserUID("OyMjfj7yb7dVARDzsAVow2CckL12");
            like.setUsername("henry21");
            like.setPhotoUserProfile("https://firebasestorage.googleapis.com/v0/b/proyecto-interciclo-cp.firebasestorage.app/o/person.png?alt=media&token=0f36c0d8-be05-48e3-a70b-0f0bab2f1743");
            likes.add(like);

            Comment comment = new Comment();
            comment.setUserUID("OyMjfj7yb7dVARDzsAVow2CckL12");
            comment.setUsername("henry21");
            comment.setPhotoUserProfile("https://firebasestorage.googleapis.com/v0/b/proyecto-interciclo-cp.firebasestorage.app/o/person.png?alt=media&token=0f36c0d8-be05-48e3-a70b-0f0bab2f1743");
            comment.setComment("Fotografía con el filtro A");
            comments.add(comment);

            Photo photo = new Photo();
            photo.setUrlPhoto(imageUrl);
            photo.setLikes(likes);
            photo.setComments(comments);

            // 2) Convierte esa Photo a un Map para usarla en arrayUnion:
            Map<String,Object> photoMap = new HashMap<>();
            photoMap.put("urlPhoto",    photo.getUrlPhoto());
            photoMap.put("likes",       photo.getLikes());
            photoMap.put("comments",    photo.getComments());

            DocumentReference docRef = getUserCollection().document(userUID);

            try {
                // 3) Intenta leer el documento
                DocumentSnapshot snap = docRef.get().get();
                if (snap.exists()) {
                    // 4a) Si existe, actualiza sólo el array "photos"
                    docRef.update("photos", FieldValue.arrayUnion(photoMap)).get();
                    // Opcional: refresca el estado si quieres devolver el objeto completo
                    UserPhotos updated = snap.toObject(UserPhotos.class);
                    updated.getPhotos().add(photo);
                    updated.setUserUID("OyMjfj7yb7dVARDzsAVow2CckL12");
                    updated.setUsername("henry21");
                    updated.setPhotoUserProfile("https://firebasestorage.googleapis.com/v0/b/proyecto-interciclo-cp.firebasestorage.app/o/person.png?alt=media&token=0f36c0d8-be05-48e3-a70b-0f0bab2f1743");                    return new PhotoResponse(updated);
                } else {
                    // 4b) Si NO existe, crea el documento con tu lista inicial
                    UserPhotos userPhotos = new UserPhotos();
                    userPhotos.setUserUID("OyMjfj7yb7dVARDzsAVow2CckL12");
                    userPhotos.setUsername("henry21");
                    userPhotos.setPhotoUserProfile("https://firebasestorage.googleapis.com/v0/b/proyecto-interciclo-cp.firebasestorage.app/o/person.png?alt=media&token=0f36c0d8-be05-48e3-a70b-0f0bab2f1743");
                    userPhotos.setPhotos(Collections.singletonList(photo));
                    docRef.set(userPhotos).get();
                    return new PhotoResponse(userPhotos);
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Error al guardar foto en Firestore", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<Map<String, Object>> getUserPhotos(String userUID) {
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

    public Flux<Map<String, Object>> getAllUserPhotos() {
        return Mono.fromCallable(() -> {
                    ApiFuture<QuerySnapshot> future = getUserCollection().get();
                    QuerySnapshot querySnapshot = future.get();
                    return querySnapshot.getDocuments();
                })
                .flatMapMany(Flux::fromIterable)
                .map(DocumentSnapshot::getData)
                .subscribeOn(Schedulers.boundedElastic());
    }


}
