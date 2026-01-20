package com.sqlcanvas.sharedkernel.shared.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // --- 400 Client Errors ---
    INVALID_PARAMETER("SYS-400", "入力値が不正です。", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("SYS-404", "指定されたリソースが見つかりませんでした。", HttpStatus.NOT_FOUND),

    // --- 401/403 Auth Errors ---
    UNAUTHORIZED("SYS-401", "認証が必要です。", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("SYS-403", "アクセス権限がありません。", HttpStatus.FORBIDDEN),

    // --- 409 Conflict ---
    CONFLICT("SYS-409", "リソースが競合しています。", HttpStatus.CONFLICT),

    // --- 500 Server Errors ---
    SYSTEM_ERROR("SYS-500", "システムエラーが発生しました。", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("SYS-503", "現在サービスを利用できません。", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;

    // こうすることで Lombok が public String getDefaultMessage() を生成します
    private final String defaultMessage;

    private final HttpStatus status;
}