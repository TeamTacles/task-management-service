package com.teamtacles.task.teamtacles_api_task.infrastructure.exception;

public class ServiceUnavailableException extends RuntimeException{
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
