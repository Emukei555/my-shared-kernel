package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;

import java.util.regex.Pattern;

public record PhoneNumber(String value) implements ValueObject {

    // 日本の電話番号（ハイフン許容）: 090-1234-5678 or 03-1234-5678
    private static final Pattern PATTERN = Pattern.compile("^0\\d{1,4}-?\\d{1,4}-?\\d{3,4}$");

    public PhoneNumber {
        if (value == null) {
            throw new IllegalArgumentException("PhoneNumber cannot be null");
        }
    }

    public static Result<PhoneNumber> of(String value) {
        if (value == null || value.isBlank()) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "電話番号は必須です");
        }
        if (!PATTERN.matcher(value).matches()) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "電話番号の形式が不正です");
        }
        return Result.success(new PhoneNumber(value));
    }

    /**
     * ハイフンを除去した数値のみの形式を取得
     */
    public String asUnformatted() {
        return value.replace("-", "");
    }
}