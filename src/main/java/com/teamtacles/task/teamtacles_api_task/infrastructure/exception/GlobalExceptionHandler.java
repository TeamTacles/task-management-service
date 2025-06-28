package com.teamtacles.task.teamtacles_api_task.infrastructure.exception;

import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // Logger utilizado para registrar os detalhes completos das exceções no servidor e para permitir o envio de mensagens de erro mais genéricas e seguras ao cliente, evitando assim a exposição de informações sensíveis da aplicação 

    //logger para registrar os erros
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //400 - quando os dados de entrada não são válidos 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage()).collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation error", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); 
    }

    //400 - quando os dados de entrada não são válidos 
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorResponse erroResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Date format", "The parameter 'dueDate' must be in ISO 8601 format: yyyy-MM-dd'T'HH:mm:ss");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroResponse);
    }

    //400 - quando o json esta mal formatado / invalido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warn("Invalid JSON format: ", ex);
        String genericErrorMessage = "Invalid JSON format. Please check your request body.";
        ErrorResponse erroResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid formated Json", genericErrorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroResponse);
    }

    //400 - quando argumento/parâmetro informado é inválido
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid Parameter value:", ex);
        ErrorResponse erroResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Parameter Value", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroResponse);
    }

    // 404 - Quando o recurso nao é encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource Not Found: ", ex.getMessage());
        ErrorResponse erroResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erroResponse);
    }

    // 403 - Quando o usuário não tem permissão de acesso
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> accessDeniedExceptionException(AccessDeniedException ex) {
        logger.warn("Access Denied: ", ex);
        String genericErrorMessage = "You do not have permission to access this resource.";
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Access Forbidden", genericErrorMessage);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse); 
    }

    // 500 - Quando ocorre um erro interno no servidor
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("Internal Server Error: ", ex); 
        String  genericErrorMessage = "An unexpected error occurred. Please try again later.";
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Our server is not responding, please try again later", genericErrorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); 
    }
}