package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import lombok.NonNull;

/**
 * 金額を表す Value Object。
 * <p>
 * 負の値を許容しない完全な不変オブジェクト。
 * 計算ロジック（足し算・引き算）を内包する。
 * </p>
 */
public record Money(long amount) implements ValueObject, Comparable<Money> {

    // コンストラクタでの防御的ガード（不変条件の強制）
    public Money {
        if (amount < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount);
        }
    }

    // --- Factories ---

    public static Money zero() {
        return new Money(0);
    }

    /**
     * 安全にMoneyを生成するファクトリ。
     */
    public static Result<Money> of(long amount) {
        if (amount < 0) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "金額は0以上である必要があります");
        }
        return Result.success(new Money(amount));
    }

    // --- Business Logic ---

    public Result<Money> add(Money other) {
        try {
            long result = Math.addExact(this.amount, other.amount); // オーバーフロー検知
            return Result.success(new Money(result));
        } catch (ArithmeticException e) {
            return Result.failure(CommonErrorCode.SYSTEM_ERROR, "金額の計算でオーバーフローが発生しました");
        }
    }

    public Result<Money> subtract(Money other) {
        if (this.amount < other.amount) {
            return Result.failure(CommonErrorCode.INVALID_PARAMETER, "残高不足です");
        }
        return Result.success(new Money(this.amount - other.amount));
    }

    // --- Utilities ---

    @Override
    public int compareTo(@NonNull Money other) {
        return Long.compare(this.amount, other.amount);
    }

    public boolean isZero() { return this.amount == 0; }

    public boolean isGreaterThan(Money other) { return this.amount > other.amount; }

    public boolean isGreaterThanOrEqual(Money other) { return this.amount >= other.amount; }
}