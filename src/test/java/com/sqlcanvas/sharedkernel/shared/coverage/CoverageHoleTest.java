package com.sqlcanvas.sharedkernel.shared.coverage;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import com.sqlcanvas.sharedkernel.shared.util.RequestId;
import org.junit.jupiter.api.Test;

import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoverageHoleTest {

    // --- RequestId の穴埋め ---

    @Test
    void requestId_branches() {
        // nullチェックの分岐 (branch: true)
        assertThat(RequestId.from(null))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);

        // 空文字チェックの分岐 (branch: true)
        assertThat(RequestId.from(""))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);

        // 空白文字チェック
        assertThat(RequestId.from("   "))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }

    // --- Result の穴埋め ---

    @Test
    void result_branches() {
        Result<String> success = Result.success("OK");

        // 1. unwrapFailure を成功時に呼ぶ (例外が出るべき) -> switchのSuccess分岐
        assertThatThrownBy(success::unwrapFailure)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Called unwrapFailure() on a Success result");

        // 2. orElseThrow を成功時に呼ぶ (値が返るべき) -> switchのSuccess分岐
        String value = success.orElseThrow(f -> new RuntimeException("Should not happen"));
        assertThat(value).isEqualTo("OK");

        // 3. orElseGet を成功時に呼ぶ -> switchのSuccess分岐
        assertThat(success.orElseGet(() -> "Default")).isEqualTo("OK");
    }
}