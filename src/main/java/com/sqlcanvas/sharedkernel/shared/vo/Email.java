package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;

import java.util.regex.Pattern;

public record Email(String value) implements ValueObject {

    // 簡易的なメールアドレス形式チェック用正規表現
    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?^`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    public Email {
        if (value == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
    }

    public static Result<Email> of(String value) {
        if (value == null || value.isBlank()) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "メールアドレスは必須です");
        }
        if (!PATTERN.matcher(value).matches()) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "メールアドレスの形式が不正です");
        }
        return Result.success(new Email(value));
    }
}