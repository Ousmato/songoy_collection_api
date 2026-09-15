package songoycollection.config.GestionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionErreur {

    private static final Logger log = LoggerFactory.getLogger(ExceptionErreur.class);

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Requête refusée : {}", ex.getMessage());
        Map<String, String> erreur = new HashMap<>();
        erreur.put("message", ex.getMessage() == null
                ? "Les données envoyées sont invalides."
                : ex.getMessage());
        return erreur;
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> hanlerInvaliArgument(MethodArgumentNotValidException ex) {
        Map<String, String> erreur = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(objectError -> {
            String fieldName = ((FieldError) objectError).getField();
            String errorMessage = objectError.getDefaultMessage();
            erreur.put(fieldName, errorMessage);

        });
        return new ResponseEntity<>(erreur, HttpStatus.BAD_REQUEST);

    }

    @ResponseBody
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String, String> handleMaxUploadSizeExceeded() {
        Map<String, String> erreur = new HashMap<>();
        erreur.put("message",
                "La taille des images est trop grande. "
                        + "Veuillez reduire la taille des fichiers puis reessayer.");
        return erreur;
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> erreur = new HashMap<>();
        erreur.put("message", ex.getMessage() == null
                ? "Vous n'avez pas l'autorisation pour acceder a cette ressource."
                : ex.getMessage());
        return erreur;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Map<String, String> globalHandler(Exception ex) {
        Map<String, String> erreur = new HashMap<>();
        erreur.put("message", ex.getMessage());
        return erreur;
    }
}
