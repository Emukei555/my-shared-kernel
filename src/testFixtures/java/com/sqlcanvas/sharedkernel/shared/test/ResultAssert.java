package com.sqlcanvas.sharedkernel.shared.test;

import com.sqlcanvas.sharedkernel.shared.error.ErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Result型専用のAssertJアサーション。
 * テストコードで assertThat(result)... と書けるようにする。
 */
public class ResultAssert<T> extends AbstractAssert<ResultAssert<T>, Result<T>> {

    public ResultAssert(Result<T> actual) {
        super(actual, ResultAssert.class);
    }

    /**
     * エントリーポイント
     */
    public static <T> ResultAssert<T> assertThat(Result<T> actual) {
        return new ResultAssert<>(actual);
    }

    /**
     * 成功していることを検証
     */
    public ResultAssert<T> isSuccess() {
        isNotNull();
        if (actual.isFailure()) {
            failWithMessage("Expected success but was failure. Error: <%s>", getFailureMessage(actual));
        }
        return this;
    }

    /**
     * 失敗していることを検証
     */
    public ResultAssert<T> isFailure() {
        isNotNull();
        if (actual.isSuccess()) {
            failWithMessage("Expected failure but was success. Value: <%s>", getSuccessValue(actual));
        }
        return this;
    }

    /**
     * 特定のエラーコードで失敗していることを検証
     */
    public ResultAssert<T> hasErrorCode(ErrorCode expectedErrorCode) {
        isFailure();
        if (actual instanceof Result.Failure<T> f) {
            if (!Objects.equals(f.errorCode(), expectedErrorCode)) {
                failWithMessage("Expected error code <%s> but was <%s>", expectedErrorCode, f.errorCode());
            }
        }
        return this;
    }

    /**
     * 成功した値をさらに検証するためのチェーンメソッド
     */
    public ResultAssert<T> hasValueSatisfying(Consumer<T> requirements) {
        isSuccess();
        if (actual instanceof Result.Success<T> s) {
            requirements.accept(s.value());
        }
        return this;
    }

    // --- Helper methods ---

    private String getFailureMessage(Result<T> result) {
        if (result instanceof Result.Failure<T> f) {
            return f.errorCode() + ": " + f.message();
        }
        return "Unknown failure";
    }

    private Object getSuccessValue(Result<T> result) {
        if (result instanceof Result.Success<T> s) {
            return s.value();
        }
        return "Unknown value";
    }
}