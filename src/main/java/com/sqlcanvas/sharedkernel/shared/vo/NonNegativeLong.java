package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import lombok.NonNull;

/**
 * 非負の長整数 (値 >= 0) を表す Value Object。
 * <p>
 * 用途例: インデックス、バージョン番号、回数(0回許容)、タイムスタンプ
 * </p>
 */
public record NonNegativeLong(long value) implements ValueObject, Comparable<NonNegativeLong> {

    public NonNegativeLong {
        if (value < 0) {
            throw new IllegalArgumentException("NonNegativeLong must be 0 or greater: " + value);
        }
    }

    public static Result<NonNegativeLong> of(long value) {
        if (value < 0) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "値は0以上である必要があります");
        }
        return Result.success(new NonNegativeLong(value));
    }

    public static final NonNegativeLong ZERO = new NonNegativeLong(0);

    // --- Operations ---

    public NonNegativeLong increment() {
        return new NonNegativeLong(Math.addExact(this.value, 1));
    }

    public Result<NonNegativeLong> add(long other) {
        if (other < 0) {
            // 負の加算（＝減算）で結果がマイナスになる可能性がある場合はチェックが必要
            if (this.value + other < 0) {
                return Result.failure(CommonErrorCode.INVALID_PARAMETER, "計算結果が負になります");
            }
        }
        try {
            return Result.success(new NonNegativeLong(Math.addExact(this.value, other)));
        } catch (ArithmeticException e) {
            return Result.failure(CommonErrorCode.SYSTEM_ERROR, "オーバーフローしました");
        }
    }

    @Override
    public int compareTo(@NonNull NonNegativeLong other) {
        return Long.compare(this.value, other.value);
    }
}