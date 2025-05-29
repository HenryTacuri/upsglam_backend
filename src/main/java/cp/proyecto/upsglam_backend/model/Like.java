package cp.proyecto.upsglam_backend.model;

public class Like {

    private String userUID;
    private String username;
    private String photoUserProfile;

    public Like() {}

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
}
