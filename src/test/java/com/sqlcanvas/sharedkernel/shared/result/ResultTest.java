package com.sqlcanvas.sharedkernel.shared.result;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void map_success_to_success() {
        Result<Integer> result = Result.success(10);
        assertThat(result.map(i -> i * 2))
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v).isEqualTo(20));
    }

    @Test
    void map_failure_skips_processing() {
        Result<Integer> failure = Result.failure(CommonErrorCode.SYSTEM_ERROR);
        assertThat(failure.map(i -> i * 2))
                .isFailure()
                .hasErrorCode(CommonErrorCode.SYSTEM_ERROR);
    }

    @Test
    void flatMap_success_to_success() {
        Result<Integer> result = Result.success(10);
        assertThat(result.flatMap(i -> Result.success(i * 2)))
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v).isEqualTo(20));
    }

    @Test
    void flatMap_success_to_failure() {
        Result<Integer> result = Result.success(10);
        assertThat(result.flatMap(i -> Result.failure(CommonErrorCode.INVALID_PARAMETER)))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }

    @Test
    void recover_failure_to_success() {
        Result<String> failure = Result.failure(CommonErrorCode.RESOURCE_NOT_FOUND);
        // 失敗時にデフォルト値を返すリカバリー
        Result<String> recovered = failure.recover(f -> "Recovered");

        assertThat(recovered)
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v).isEqualTo("Recovered"));
    }

    @Test
    void recover_success_skips_processing() {
        Result<String> success = Result.success("Original");
        Result<String> recovered = success.recover(f -> "Recovered");

        assertThat(recovered)
                .isSuccess()
                .hasValueSatisfying(v -> assertThat(v).isEqualTo("Original"));
    }

    @Test
    void unwrap_failure_throws_exception() {
        Result<String> failure = Result.failure(CommonErrorCode.SYSTEM_ERROR, "Bomb");
        assertThatThrownBy(failure::unwrap)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Bomb");
    }

    @Test
    void orElse_methods() {
        Result<String> failure = Result.failure(CommonErrorCode.SYSTEM_ERROR);

        assertThat(failure.orElse("Default")).isEqualTo("Default");
        assertThat(failure.orElseGet(() -> "Default")).isEqualTo("Default");
        assertThatThrownBy(() -> failure.orElseThrow(f -> new IllegalArgumentException("Ex")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fold_test() {
        Result<Integer> success = Result.success(10);
        String sMsg = success.fold(
                val -> "Success: " + val,
                err -> "Error"
        );
        assertThat(sMsg).isEqualTo("Success: 10");

        Result<Integer> fail = Result.failure(CommonErrorCode.SYSTEM_ERROR);
        String fMsg = fail.fold(
                val -> "Success",
                // ★修正: err.errorCode() -> err.errorCode().getCode() に変更
                err -> "Error: " + err.errorCode().getCode()
        );
        assertThat(fMsg).isEqualTo("Error: SYS-500");
    }

    @Test
    void tap_coverage() {
        // 成功時のtap (実行される)
        Result<String> success = Result.success("OK");
        StringBuilder sb = new StringBuilder();
        success.tap(sb::append);
        assertThat(sb.toString()).isEqualTo("OK");

        // 失敗時のtap (実行されない -> if文のfalse分岐網羅)
        Result<String> failure = Result.failure(CommonErrorCode.SYSTEM_ERROR);
        failure.tap(v -> sb.append("ShouldNotRun"));
        assertThat(sb.toString()).isEqualTo("OK"); // 変わっていないこと
    }

    @Test
    void tapFailure_coverage() {
        // 失敗時のtapFailure (実行される)
        Result<String> failure = Result.failure(CommonErrorCode.SYSTEM_ERROR);
        StringBuilder sb = new StringBuilder();
        failure.tapFailure(f -> sb.append("Error"));
        assertThat(sb.toString()).isEqualTo("Error");

        // 成功時のtapFailure (実行されない -> if文のfalse分岐網羅)
        Result<String> success = Result.success("OK");
        success.tapFailure(f -> sb.append("ShouldNotRun"));
        assertThat(sb.toString()).isEqualTo("Error"); // 変わっていないこと
    }

    @Test
    void isSuccess_isFailure_coverage() {
        Result<String> success = Result.success("OK");
        Result<String> failure = Result.failure(CommonErrorCode.SYSTEM_ERROR);

        assertThat(success.isSuccess()).isTrue();
        assertThat(success.isFailure()).isFalse();

        assertThat(failure.isSuccess()).isFalse();
        assertThat(failure.isFailure()).isTrue();
    }
}