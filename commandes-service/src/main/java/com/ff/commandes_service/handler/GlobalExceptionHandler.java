package com.ff.commandes_service.handler;
// Dans un package comme com.ff.products_service.exception.handler
import com.ff.commandes_service.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice // Ceci combine @ControllerAdvice et @ResponseBody
public class GlobalExceptionHandler {

    // Gestionnaire pour votre IllegalArgumentException spécifique
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST; // Ou HttpStatus.NOT_FOUND si c'est plus approprié pour "No orders found"
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(), // Ex: "Bad Request"
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    // Vous pouvez ajouter d'autres gestionnaires pour d'autres types d'exceptions
    // Par exemple, pour les exceptions génériques non gérées spécifiquement
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, WebRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Une erreur inattendue est survenue: " + ex.getMessage(), // Message générique pour l'utilisateur
                path
        );

        // Pour le débogage, vous pourriez vouloir logger l'exception complète
        // logger.error("Unhandled exception: ", ex);

        return new ResponseEntity<>(errorResponse, status);
    }
}