package cp.proyecto.upsglam_backend.model;

import java.util.List;

public class Photo {

    private String urlPhoto;
    private List<Like> likes;
    private List<Comment> comments;

    public Photo() {

    }

    public Photo(String urlPhoto, List<Like> likes, List<Comment> comments) {
        this.urlPhoto = urlPhoto;
        this.likes = likes;
        this.comments = comments;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

}
