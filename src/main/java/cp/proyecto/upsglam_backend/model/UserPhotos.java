package cp.proyecto.upsglam_backend.model;

import java.util.List;

public class UserPhotos {

    private String userUID;
    private String username;
    private String photoUserProfile;

    private List<Photo> photos;

    public UserPhotos() {}

    public UserPhotos(String userUID, String username, String photoUserProfile, List<Photo> photos) {
        this.userUID = userUID;
        this.username = username;
        this.photoUserProfile = photoUserProfile;
        this.photos = photos;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUserProfile() {
        return photoUserProfile;
    }

    public void setPhotoUserProfile(String photoUserProfile) {
        this.photoUserProfile = photoUserProfile;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}
