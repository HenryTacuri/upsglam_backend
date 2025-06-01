package cp.proyecto.upsglam_backend.model;

import com.google.cloud.Timestamp;

import java.time.Instant;
import java.util.List;

public class Photo {

    private String urlPhoto;
    private String urlPhotoFilter;
    private Timestamp date;
    private List<Like> likes;
    private List<Comment> comments;

    public Photo() {

    }

    public Photo(String urlPhoto, String urlPhotoFilter, Timestamp date, List<Like> likes, List<Comment> comments) {
        this.urlPhoto = urlPhoto;
        this.urlPhotoFilter = urlPhotoFilter;
        this.date = date;
        this.likes = likes;
        this.comments = comments;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public String getUrlPhotoFilter() {
        return urlPhotoFilter;
    }

    public void setUrlPhotoFilter(String urlPhotoFilter) {
        this.urlPhotoFilter = urlPhotoFilter;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
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
