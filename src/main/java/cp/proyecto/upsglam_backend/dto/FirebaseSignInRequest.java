package cp.proyecto.upsglam_backend.dto;

public record FirebaseSignInRequest(String email, String password, boolean returnSecureToken) {}
