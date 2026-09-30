package org.sistemagestion.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    private static final String BASE_TYPE = "https://api.sistema_gestion/errors/";

    @ExceptionHandler(ResourceNotFoundException.class) // 404
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {

        // El mensaje técnico (en inglés) queda solo en el log
        log.warn("Recurso no encontrado - {} - {}", request.getDescription(false), ex.getMessage());

        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", "not-found",
                "No se encontró el registro solicitado. Es posible que haya sido eliminado.");
    }

    @ExceptionHandler(BusinessRuleException.class) // 409
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {

        // Estos mensajes los escribimos para el usuario: se envían tal cual
        return problem(HttpStatus.CONFLICT, "Regla de negocio incumplida", "business-rule", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // 409
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {

        log.warn("Violación de integridad - {} - {}", request.getDescription(false), ex.getMostSpecificCause().getMessage());

        return problem(HttpStatus.CONFLICT, "Conflicto de datos", "data-integrity",
                "No se pudo completar la operación porque el registro está relacionado con otros datos.");
    }

    @ExceptionHandler(Exception.class) // 500
    public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {

        log.error("Error inesperado - {}: {}", request.getDescription(false), ex.getMessage(), ex);

        // Nunca se envía ex.getMessage(): puede filtrar SQL, rutas o clases internas
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "internal",
                "Ocurrió un error inesperado. Si se repite, contactá al administrador.");
    }

    // ===== Errores 400 que Spring resuelve por su cuenta: solo cambiamos el formato y el idioma =====

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Error de validación", "error-validation",
                "Los datos enviados no son válidos. Revisá los campos del formulario.");
        problem.setProperty("errors", errors);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return badRequest(ex, headers, status, request, "El formato de los datos enviados no es válido.");
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return badRequest(ex, headers, status, request, "Uno de los parámetros enviados no es válido.");
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return badRequest(ex, headers, status, request, "Falta un parámetro obligatorio en la solicitud.");
    }

    private ResponseEntity<Object> badRequest(Exception ex, HttpHeaders headers, HttpStatusCode status,
                                              WebRequest request, String detail) {
        log.warn("Solicitud inválida - {} - {}", request.getDescription(false), ex.getMessage());

        return handleExceptionInternal(ex,
                problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", "bad-request", detail),
                headers, status, request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String type, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(BASE_TYPE + type));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}