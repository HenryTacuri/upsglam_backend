package cp.proyecto.upsglam_backend.exceptions;

public class InvalidLoginCredentialsException extends RuntimeException {

    public InvalidLoginCredentialsException(String message) {
        super(message);
    }

}
