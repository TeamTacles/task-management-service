package com.teamtacles.task.teamtacles_api_task.infrastructure.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
	
	private LocalDateTime dateTime;
	private int status;
	private String errorTitle;
	private String errorMessage;
	
    public ErrorResponse(int status, String errorTitle, String errorMessage) {
	    this.dateTime = LocalDateTime.now();
	    this.status = status;
	    this.errorTitle = errorTitle;
        this.errorMessage = errorMessage;
    }
}