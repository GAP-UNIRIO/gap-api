package com.gap.api.Exception;

import com.gap.api.Model.DTO.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleMethodArgumentNotValid deve retornar a resposta correta para erros de validação")
    void handleMethodArgumentNotValid_ShouldReturnCorrectResponse() {
        // Arrange
        FieldError fieldError = new FieldError("objectName", "field", "must not be null");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class)
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        BaseResponse<?> body = (BaseResponse<?>) response.getBody();
        assertEquals("Dados inválidos.", body.message());
    }

    @Test
    @DisplayName("handleExceptionInternal deve retornar a resposta correta para exceções internas")
    void handleExceptionInternal_ShouldReturnCorrectResponse() {
        // Arrange
        Exception exception = new Exception("Internal error");
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleExceptionInternal(
                exception, problemDetail, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, mock(WebRequest.class)
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        BaseResponse<?> body = (BaseResponse<?>) response.getBody();
        assertEquals("Internal error", body.message());
    }

    @Test
    @DisplayName("handleDataIntegrity deve retornar a resposta correta para violações de integridade")
    void handleDataIntegrity_ShouldReturnCorrectResponse() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Integrity violation");

        // Act
        ResponseEntity<BaseResponse<Void>> response = exceptionHandler.handleDataIntegrity(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("A operação viola uma restrição de integridade (registro em uso ou valor duplicado).", response.getBody().message());
    }

    @Test
    @DisplayName("handleIllegalArgumentException deve retornar a resposta correta para argumentos inválidos")
    void handleIllegalArgumentException_ShouldReturnCorrectResponse() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid data");

        // Act
        ResponseEntity<BaseResponse<Void>> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid data", response.getBody().message());
    }
}
