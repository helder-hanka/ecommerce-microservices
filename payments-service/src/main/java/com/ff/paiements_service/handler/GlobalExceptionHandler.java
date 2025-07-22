package com.ff.paiements_service.handler;
// Dans un package comme com.ff.products_service.exception.handler

import com.ff.paiements_service.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Ceci combine @ControllerAdvice et @ResponseBody
public class GlobalExceptionHandler {

    /**
     * Gère les exceptions de validation des arguments de méthode (ex: @Valid sur un @RequestBody).
     * Retourne un statut 400 Bad Request et des détails sur les erreurs de validation.
     *
     * @param ex L'exception MethodArgumentNotValidException.
     * @param request La requête web.
     * @return ResponseEntity avec ErrorResponse et HttpStatus.BAD_REQUEST.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        String errorMessage = "Erreur de validation des champs: " + errors.toString();
        String path = ((ServletWebRequest) request).getRequest().getRequestURI(); // Utilisation de ServletWebRequest pour le path

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                errorMessage,
                path, // Utilisation du path
                LocalDateTime.now() // Ajout du timestamp
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les exceptions IllegalArgumentException (utilisées dans PaymentService).
     * Retourne un statut 404 Not Found ou 400 Bad Request selon le contexte.
     *
     * @param ex L'exception IllegalArgumentException.
     * @param request La requête web.
     * @return ResponseEntity avec ErrorResponse et HttpStatus.NOT_FOUND ou HttpStatus.BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST; // Par défaut, pour les arguments invalides
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        // Correction de la condition pour détecter les messages "non trouvés"
        if (message.contains("not found") || message.contains("no payments found")) {
            status = HttpStatus.NOT_FOUND; // Si le message indique que la ressource n'a pas été trouvée
        }

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(), // Ex: "Bad Request" ou "Not Found"
                ex.getMessage(), // Message original de l'exception
                path,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Gère toutes les autres exceptions non capturées.
     * Retourne un statut 500 Internal Server Error.
     *
     * @param ex L'exception générique.
     * @param request La requête web.
     * @return ResponseEntity avec ErrorResponse et HttpStatus.INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, WebRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String path = ((ServletWebRequest) request).getRequest().getRequestURI(); // Utilisation de ServletWebRequest pour le path

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Une erreur inattendue est survenue: " + ex.getMessage(), // Message générique pour l'utilisateur
                path, // Utilisation du path
                LocalDateTime.now() // Ajout du timestamp
        );

        // Pour le débogage, vous pourriez vouloir logger l'exception complète
        // logger.error("Unhandled exception: ", ex);

        return new ResponseEntity<>(errorResponse, status);
    }
}