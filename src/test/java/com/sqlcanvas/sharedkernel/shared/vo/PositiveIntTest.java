package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;

class PositiveIntTest {

    @Test
    void create_success() {
        assertThat(PositiveInt.of(10))
                .isSuccess()
                .hasValueSatisfying(v -> {
                    org.assertj.core.api.Assertions.assertThat(v.value()).isEqualTo(10);
                });
    }

    @Test
    void create_fail_zero() {
        assertThat(PositiveInt.of(0))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }

    @Test
    void create_fail_negative() {
        assertThat(PositiveInt.of(-1))
                .isFailure()
                .hasErrorCode(CommonErrorCode.INVALID_PARAMETER);
    }
}