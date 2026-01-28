package com.sqlcanvas.sharedkernel.shared.validation;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.error.ErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 複数のResult検証をまとめて行うためのユーティリティ。
 * 全て成功した場合のみ値を生成し、失敗がある場合は全てのエラーを収集する。
 */
public class Validation {

    // インスタンス化禁止
    private Validation() {}

    /**
     * エラー情報のリストを持つResultのFailure型定義（便宜用）
     * 通常のResult.Failureは単一エラーだが、バリデーション結果は複数エラーになりうるため。
     * ここではシンプルに、最初のエラーコードを代表としつつ、メッセージに全エラーを含める簡易実装とするか、
     * 専用のValidationError型を返す設計が考えられる。
     * * 今回は「my-shared-kernel」の既存Result型を壊さないよう、
     * 「失敗時は List<String> メッセージを持つ Result.Failure」を生成する戦略をとる。
     */

    // --- Combine 2 Results ---
    public static <T1, T2, R> Result<R> combine(
            Result<T1> r1,
            Result<T2> r2,
            BiFunction<T1, T2, R> combiner
    ) {
        List<String> errors = new ArrayList<>();
        if (r1 instanceof Result.Failure<T1> f) errors.add(formatError(f));
        if (r2 instanceof Result.Failure<T2> f) errors.add(formatError(f));

        if (!errors.isEmpty()) {
            return createFailure(errors);
        }

        // 全て成功 (unwrapは安全)
        return Result.success(combiner.apply(r1.unwrap(), r2.unwrap()));
    }

    // --- Combine 3 Results ---
    public static <T1, T2, T3, R> Result<R> combine(
            Result<T1> r1,
            Result<T2> r2,
            Result<T3> r3,
            TriFunction<T1, T2, T3, R> combiner
    ) {
        List<String> errors = new ArrayList<>();
        if (r1 instanceof Result.Failure<T1> f) errors.add(formatError(f));
        if (r2 instanceof Result.Failure<T2> f) errors.add(formatError(f));
        if (r3 instanceof Result.Failure<T3> f) errors.add(formatError(f));

        if (!errors.isEmpty()) {
            return createFailure(errors);
        }

        return Result.success(combiner.apply(r1.unwrap(), r2.unwrap(), r3.unwrap()));
    }

    // --- Helper Methods & Interfaces ---

    private static String formatError(Result.Failure<?> failure) {
        // 例: "[INVALID_PARAMETER] メールアドレスが不正です"
        return "[" + failure.errorCode().getCode() + "] " + failure.message();
    }

    private static <R> Result<R> createFailure(List<String> errors) {
        // 複数のエラーがある場合、代表として INVALID_PARAMETER を返しつつ、
        // メッセージには改行区切りで全てのエラーを含める
        String combinedMessage = String.join("\n", errors);
        return Result.failure(CommonErrorCode.INVALID_PARAMETER, combinedMessage);
    }

    // Java標準にない 3引数のFunction
    @FunctionalInterface
    public interface TriFunction<T1, T2, T3, R> {
        R apply(T1 t1, T2 t2, T3 t3);
    }
}