package cp.proyecto.upsglam_backend.model;

public class Comment {

    private String userUID;
    private String username;
    private String comment;
    private String photoUserProfile;

    public Comment() {}

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
