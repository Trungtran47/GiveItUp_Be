package com.giveitup.giveitup_be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    CATEGORY_EXISTED(1006, "Category existed", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTED(1007, "Category not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(4001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(1009, "Role does not exist", HttpStatus.BAD_REQUEST),
    POST_EXISTED(1010, "Post existed", HttpStatus.BAD_REQUEST),
    POST_NOT_EXISTED(1011, "Post not existed", HttpStatus.NOT_FOUND),
    BANK_ACCOUNT_EXISTED(1012, "Bank account existed", HttpStatus.BAD_REQUEST),
    BANK_ACCOUNT_NOT_EXISTED(1013, "Bank account not existed", HttpStatus.NOT_FOUND),
    PARENT_COMMENT_NOT_FOUND(1014, "Parent comment not existed", HttpStatus.NOT_FOUND),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
