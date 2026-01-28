package com.sqlcanvas.sharedkernel.shared.result;

import com.sqlcanvas.sharedkernel.shared.error.ErrorCode;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 処理の成功または失敗を表す結果型 (Railway Oriented Programming の基盤)。
 * <p>
 * Java 21の switch expression と pattern matching を活用しています。
 * </p>
 *
 * @param <T> 成功時に保持する値の型
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    /**
     * 成功
     */
    record Success<T>(T value) implements Result<T> {}

    /**
     * 失敗
     */
    record Failure<T>(ErrorCode errorCode, String message) implements Result<T> {}

    // --- Factories ---

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(ErrorCode errorCode) {
        return new Failure<>(errorCode, errorCode.getDefaultMessage()); // getDefaultMessage()がある前提
    }

    static <T> Result<T> failure(ErrorCode errorCode, String message) {
        return new Failure<>(errorCode, message);
    }

    // --- Intermediate Operations (Railway) ---

    /**
     * 成功時のみ値を変換します。
     */
    default <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return switch (this) {
            case Success<T>(var value) -> success(mapper.apply(value));
            case Failure<T>(var code, var msg) -> failure(code, msg);
        };
    }

    /**
     * 成功時のみ、次のResultを返す処理を連結します。
     */
    default <U> Result<U> flatMap(Function<? super T, ? extends Result<U>> mapper) {
        Objects.requireNonNull(mapper);
        return switch (this) {
            case Success<T>(var value) -> mapper.apply(value);
            case Failure<T>(var code, var msg) -> failure(code, msg);
        };
    }

    /**
     * 失敗時のみ、リカバリー処理を行い成功状態に復帰させます。
     * エラーハンドリングをしてデフォルト値を返す場合などに使用します。
     */
    default Result<T> recover(Function<Failure<T>, T> recovery) {
        Objects.requireNonNull(recovery);
        return switch (this) {
            case Success<T> s -> s;
            case Failure<T> f -> success(recovery.apply(f));
        };
    }

    // --- Side Effects (Peeking) ---

    default Result<T> tap(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (this instanceof Success<T>(var value)) {
            action.accept(value);
        }
        return this;
    }

    default Result<T> tapFailure(Consumer<Failure<T>> action) {
        Objects.requireNonNull(action);
        if (this instanceof Failure<T> f) {
            action.accept(f);
        }
        return this;
    }

    // --- Terminal Operations (Extracting) ---

    /**
     * 成功かどうかを判定します。
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * 失敗かどうかを判定します。
     */
    default boolean isFailure() {
        return this instanceof Failure;
    }

    /**
     * 成功時は値を返し、失敗時は指定された値を返します。
     */
    default T orElse(T other) {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Failure<T> f -> other;
        };
    }

    /**
     * 成功時は値を返し、失敗時は指定されたサプライヤーの結果を返します。
     */
    default T orElseGet(Supplier<? extends T> otherSupplier) {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Failure<T> f -> otherSupplier.get();
        };
    }

    /**
     * 成功時は値を返し、失敗時は指定された例外をスローします。
     * <p>
     * 使用例: {@code result.orElseThrow(() -> new MyCustomException(...))}
     * </p>
     */
    default <X extends Throwable> T orElseThrow(Function<Failure<T>, ? extends X> exceptionSupplier) throws X {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Failure<T> f -> throw exceptionSupplier.apply(f);
        };
    }

    /**
     * 成功と失敗の処理を分岐して、最終的な値を生成します。
     * Result型の終端操作として最も強力です。
     */
    default <R> R fold(Function<? super T, ? extends R> onSuccess, Function<Failure<T>, ? extends R> onFailure) {
        return switch (this) {
            case Success<T>(var value) -> onSuccess.apply(value);
            case Failure<T> f -> onFailure.apply(f);
        };
    }

    /**
     * 強制的に値を取り出します。
     * <p>
     * 注意: 失敗時は {@link RuntimeException} がスローされます。
     * ライブラリ外で特定の例外を投げたい場合は {@link #orElseThrow(Function)} を使用してください。
     * </p>
     */
    default T unwrap() {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Failure<T>(var code, var msg) ->
                    throw new RuntimeException("Result failure: [" + code + "] " + msg);
        };
    }

    /**
     * 強制的に失敗情報（Failure）を取り出します。
     * <p>
     * 成功時は {@link IllegalStateException} がスローされます。
     * 主にテストコードや、isFailure() 確定後の処理で使用します。
     * </p>
     */
    default Failure<T> unwrapFailure() {
        return switch (this) {
            case Success<T> s ->
                    throw new IllegalStateException("Called unwrapFailure() on a Success result: " + s.value());
            case Failure<T> f -> f;
        };
    }
}