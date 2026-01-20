package com.sqlcanvas.sharedkernel.shared.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getCode();

    String getDefaultMessage();

    HttpStatus getStatus();
}