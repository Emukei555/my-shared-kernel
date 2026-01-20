package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;

import java.util.regex.Pattern;

public record PostalCode(String value) implements ValueObject {

    // 日本の郵便番号: 123-4567 (ハイフン必須とする場合の例)
    private static final Pattern PATTERN = Pattern.compile("^\\d{3}-\\d{4}$");

    public PostalCode {
        if (value == null) {
            throw new IllegalArgumentException("PostalCode cannot be null");
        }
    }

    public static Result<PostalCode> of(String value) {
        if (value == null || value.isBlank()) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "郵便番号は必須です");
        }
        // ハイフンなしで来た場合に補完するロジックを入れても良いが、ここでは厳格にチェック
        if (!PATTERN.matcher(value).matches()) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "郵便番号は '123-4567' の形式である必要があります");
        }
        return Result.success(new PostalCode(value));
    }
}