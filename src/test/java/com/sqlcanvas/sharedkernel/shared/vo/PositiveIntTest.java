package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import com.sqlcanvas.sharedkernel.shared.result.Result;
import org.junit.jupiter.api.Test;

// testFixturesのstaticメソッドをインポート
import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;

class PositiveIntTest {

    @Test
    void create_success() {
        // 正常系: 値の検証もチェーンで書ける
        assertThat(PositiveInt.of(10))
                .isSuccess()
                .hasValueSatisfying(v -> {
                    // ここは通常のAssertJ
                    org.assertj.core.api.Assertions.assertThat(v.value()).isEqualTo(10);
                });
    }

    @Test
    void create_fail_zero() {
        // 異常系 (0は許可されない)
        assertThat(PositiveInt.of(0))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }

    @Test
    void create_fail_negative() {
        // 異常系 (マイナスは許可されない)
        assertThat(PositiveInt.of(-1))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }
}