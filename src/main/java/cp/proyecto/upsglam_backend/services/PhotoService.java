package cp.proyecto.upsglam_backend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    @Autowired
    private Firestore firestore;

    private Instant date;


    private CollectionReference getUserCollection() {
        return firestore.collection("photos");
    }

    private CollectionReference getUserDataCollection() {
        return firestore.collection("users");
    }

    public Mono<PhotoResponse> saveUserPhoto(String userUID, MultipartFile userPhotoProfile, MultipartFile imageFiler) {
        return uploadPhotoToFirebase(userPhotoProfile, imageFiler, userUID)
                .flatMap(imageUrl ->
                        // savePhotoData devuelve Mono<PhotoResponse>
                        savePhotoData(userUID, imageUrl[0], imageUrl[1])
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String[]> uploadPhotoToFirebase(MultipartFile file, MultipartFile imageFiler, String userUID) {
        return Mono.fromCallable(() -> {
            String urlsPhotos[] = new String[2];

            for (int i = 0; i<2; i++) {
                String fileName = "UsersPhotos/" + userUID + "_" + UUID.randomUUID().toString();
                Bucket bucket = StorageClient.getInstance().bucket();
                if(i == 0) {
                    bucket.create(fileName, file.getInputStream(), file.getContentType());
                } else {
                    bucket.create(fileName, imageFiler.getInputStream(), imageFiler.getContentType());
                }

                String DOWNLOAD_URL  = "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName() + "/o/%s?alt=media";
                urlsPhotos[i] = String.format(DOWNLOAD_URL , URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            }

            return urlsPhotos;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> updatePhotoToFirebase(MultipartFile file, String userUID) {
        return Mono.fromCallable(() -> {

            String fileName = "UsersPhotos/" + userUID + "_" + UUID.randomUUID().toString();
            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.create(fileName, file.getInputStream(), file.getContentType());
            String DOWNLOAD_URL  = "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName() + "/o/%s?alt=media";

            return String.format(DOWNLOAD_URL , URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PhotoResponse> savePhotoData(String userUID, String imageUrl, String imageFilter) {
        return getUsername(userUID)
                .flatMap(username -> {
                    return Mono.fromCallable(() -> {
                                List<Like> likes = new ArrayList<>();
                                Like like = new Like();
                                like.setUserUID("dfadsfads");
                                like.setUsername(username[0]);
                                like.setPhotoUserProfile(
                                        "https://firebasestorage.googleapis.com/v0/b/proyecto-interciclo-cp.firebasestorage.app/o/person.png?alt=media&token=0f36c0d8-be05-48e3-a70b-0f0bab2f1743"
                                );
                                likes.add(like);

                                List<Comment> comments = new ArrayList<>();
                                Comment comment = new Comment();
                                comment.setUserUID("dfadsfads");
                                comment.setUsername(username[0]);
                                comment.setPhotoUserProfile(
                                        "https://firebasestorage.googleapis.com/v0/b/proyecto-interciclo-cp.firebasestorage.app/o/person.png?alt=media&token=0f36c0d8-be05-48e3-a70b-0f0bab2f1743"
                                );
                                comment.setComment("Fotografía con el filtro A");
                                comments.add(comment);

                                Photo photo = new Photo();
                                photo.setUrlPhoto(imageUrl);
                                photo.setUrlPhotoFilter(imageFilter);
                                photo.setLikes(likes);
                                photo.setComments(comments);
                                photo.setDate(Timestamp.now());

                                Map<String, Object> photoMap = new HashMap<>();
                                photoMap.put("urlPhoto", photo.getUrlPhoto());
                                photoMap.put("urlPhotoFilter", photo.getUrlPhotoFilter());
                                photoMap.put("likes", photo.getLikes());
                                photoMap.put("comments", photo.getComments());
                                photoMap.put("date", Timestamp.now());

                                DocumentReference docRef = getUserCollection().document(userUID);

                                try {
                                    DocumentSnapshot snap = docRef.get().get(); // operación bloqueante
                                    if (snap.exists()) {
                                        docRef.update("photos", FieldValue.arrayUnion(photoMap)).get();
                                        UserPhotos updated = snap.toObject(UserPhotos.class);
                                        updated.getPhotos().add(photo);
                                        updated.setUserUID(userUID);
                                        updated.setUsername(username[0]);
                                        updated.setPhotoUserProfile(username[1]);
                                        return new PhotoResponse(updated);
                                    } else {
                                        UserPhotos newUserPhotos = new UserPhotos();
                                        newUserPhotos.setUserUID(userUID);
                                        newUserPhotos.setUsername(username[0]);
                                        newUserPhotos.setPhotoUserProfile(username[1]);
                                        newUserPhotos.setPhotos(Collections.singletonList(photo));
                                        docRef.set(newUserPhotos).get();
                                        return new PhotoResponse(newUserPhotos);
                                    }
                                } catch (ExecutionException | InterruptedException e) {
                                    throw new RuntimeException("Error al guardar foto en Firestore", e);
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .onErrorMap(ex -> new RuntimeException("No se pudo obtener el username o guardar la foto: " + ex.getMessage(), ex));
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

    public Mono<PhotoResponse> updateUserPhoto(String userUID, String oldUrlPhoto, MultipartFile newPhoto) {
        return updatePhotoToFirebase(newPhoto, userUID)
                .flatMap(newImageUrl -> updatePhotoUrlInFirestore(userUID, oldUrlPhoto, newImageUrl))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PhotoResponse> updatePhotoUrlInFirestore(String userUID, String oldUrlPhoto, String newUrlPhoto) {
        return Mono.fromCallable(() -> {
            DocumentReference docRef = getUserCollection().document(userUID);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            UserPhotos userPhotos = snapshot.toObject(UserPhotos.class);
            if (userPhotos == null || userPhotos.getPhotos() == null) {
                throw new RuntimeException("No hay fotos para actualizar");
            }

            List<Photo> updatedPhotos = new ArrayList<>();
            boolean found = false;

            for (Photo photo : userPhotos.getPhotos()) {
                if (photo.getUrlPhoto().equals(oldUrlPhoto)) {
                    photo.setUrlPhotoFilter(newUrlPhoto); // Actualiza solo la URL
                    photo.setDate(Timestamp.now());
                    found = true;
                }
                updatedPhotos.add(photo);
            }

            if (!found) {
                throw new RuntimeException("Foto con esa URL no encontrada");
            }

            // Actualiza la lista completa en Firestore
            docRef.update("photos", updatedPhotos).get();

            userPhotos.setPhotos(updatedPhotos);
            return new PhotoResponse(userPhotos);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PhotoResponse> deleteUserPhoto(String userUID, String photoUrlToDelete) {
        return Mono.fromCallable(() -> {
            DocumentReference docRef = getUserCollection().document(userUID);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            UserPhotos userPhotos = snapshot.toObject(UserPhotos.class);
            if (userPhotos == null || userPhotos.getPhotos() == null) {
                throw new RuntimeException("No hay fotos para eliminar");
            }

            // 1️⃣ Filtra las fotos quitando la que coincide con la url
            List<Photo> updatedPhotos = userPhotos.getPhotos().stream()
                    .filter(photo -> !photo.getUrlPhoto().equals(photoUrlToDelete))
                    .collect(Collectors.toList());

            // 2️⃣ Actualiza Firestore
            docRef.update("photos", updatedPhotos).get();

            // 3️⃣ Elimina de Storage
            deleteFromFirebaseStorage(photoUrlToDelete);

            // 4️⃣ Retorna respuesta
            userPhotos.setPhotos(updatedPhotos);
            return new PhotoResponse(userPhotos);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public void deleteFromFirebaseStorage(String fullUrl) {
        try {
            // Extrae el path codificado del archivo (ej: UsersPhotos/user123_filename.jpg)
            String[] splitUrl = fullUrl.split("/o/");
            if (splitUrl.length < 2) return;

            String encodedPath = splitUrl[1].split("\\?")[0]; // "UsersPhotos/user123_xxx.jpg"
            String filePath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);

            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.get(filePath);
            if (blob != null && blob.exists()) {
                blob.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar imagen del Storage", e);
        }
    }

    public Mono<String[]> getUsername(String documentId) {
        return Mono.fromCallable(() -> {
                    ApiFuture<DocumentSnapshot> future = getUserDataCollection()
                            .document(documentId)
                            .get();
                    DocumentSnapshot document = future.get();
                    String dataUser[] = new String[2];
                    dataUser[0] = document.getString("username");
                    dataUser[1] = document.getString("photoUserProfile");
                    return dataUser;
        })
                .subscribeOn(Schedulers.boundedElastic());

    }

}
