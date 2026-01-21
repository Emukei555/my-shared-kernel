package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import lombok.NonNull;

/**
 * 正の整数 (値 > 0) を表す Value Object。
 * <p>
 * 用途例: 数量(1以上)、ページ番号、ID(Integerの場合)
 * </p>
 */
public record PositiveInt(int value) implements ValueObject, Comparable<PositiveInt> {

    // 不変条件の防御 (コンストラクタ)
    public PositiveInt {
        if (value <= 0) {
            throw new IllegalArgumentException("PositiveInt must be greater than 0: " + value);
        }
    }

    /**
     * ファクトリメソッド
     */
    public static Result<PositiveInt> of(int value) {
        if (value <= 0) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "値は1以上の正数である必要があります");
        }
        return Result.success(new PositiveInt(value));
    }

    // --- Operations ---

    public PositiveInt add(PositiveInt other) {
        // 正数 + 正数 は必ず正数 (オーバーフローは検知)
        return new PositiveInt(Math.addExact(this.value, other.value));
    }

    public Result<PositiveInt> multiply(int multiplier) {
        if (multiplier <= 0) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "乗数は正数である必要があります");
        }
        try {
            return Result.success(new PositiveInt(Math.multiplyExact(this.value, multiplier)));
        } catch (ArithmeticException e) {
            return Result.failure(CommonErrorCode.SYSTEM_ERROR, "計算結果がオーバーフローしました");
        }
    }

    @Override
    public int compareTo(@NonNull PositiveInt other) {
        return Integer.compare(this.value, other.value);
    }
}
