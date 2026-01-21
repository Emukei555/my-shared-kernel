package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 正の数値 (値 > 0) を表す BigDecimal ラッパー。
 * <p>
 * 用途例: 重量、体積、単価(0円不可)
 * </p>
 */
public record PositiveBigDecimal(BigDecimal value) implements ValueObject, Comparable<PositiveBigDecimal> {

    public PositiveBigDecimal {
        Objects.requireNonNull(value, "Value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("PositiveBigDecimal must be greater than 0: " + value);
        }
    }

    public static Result<PositiveBigDecimal> of(BigDecimal value) {
        if (value == null) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "値は必須です");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "値は正数である必要があります");
        }
        return Result.success(new PositiveBigDecimal(value));
    }

    // doubleからの変換用コンビニエンスメソッド
    public static Result<PositiveBigDecimal> of(double value) {
        return of(BigDecimal.valueOf(value));
    }

    // --- Operations ---

    public PositiveBigDecimal add(PositiveBigDecimal other) {
        return new PositiveBigDecimal(this.value.add(other.value));
    }

    public Result<PositiveBigDecimal> multiply(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "乗数は正数である必要があります");
        }
        return Result.success(new PositiveBigDecimal(this.value.multiply(multiplier)));
    }

    // スケール調整（DB保存前などに使う）
    public PositiveBigDecimal setScale(int newScale, RoundingMode roundingMode) {
        return new PositiveBigDecimal(this.value.setScale(newScale, roundingMode));
    }

    @Override
    public int compareTo(@NonNull PositiveBigDecimal other) {
        return this.value.compareTo(other.value);
    }
}