package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.error.CommonErrorCode;
import org.junit.jupiter.api.Test;
import static com.sqlcanvas.sharedkernel.shared.test.ResultAssert.assertThat;

class MoreVoTest {

    @Test
    void positiveInt_overflow() {
        PositiveInt max = PositiveInt.of(Integer.MAX_VALUE).unwrap();
        // オーバーフローすると SYSTEM_ERROR になる分岐
        assertThat(max.multiply(2))
                .isFailure()
                .hasErrorCode(CommonErrorCode.SYSTEM_ERROR);
    }

    @Test
    void money_overflow() {
        Money max = Money.of(Long.MAX_VALUE).unwrap();
        // 足し算でオーバーフロー
        assertThat(max.add(Money.of(1).unwrap()))
                .isFailure()
                .hasErrorCode(CommonErrorCode.SYSTEM_ERROR);
    }
}