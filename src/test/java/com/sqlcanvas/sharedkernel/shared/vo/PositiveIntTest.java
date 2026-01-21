package com.sqlcanvas.sharedkernel.shared.vo;

import com.sqlcanvas.sharedkernel.shared.result.Result;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PositiveIntTest {

    @Test
    void create_success() {
        // 正常系のテスト
        Result<PositiveInt> result = PositiveInt.of(10);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap().value()).isEqualTo(10);
    }

    @Test
    void create_fail_zero() {
        // 異常系のテスト (0は許可されない)
        Result<PositiveInt> result = PositiveInt.of(0);
        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void create_fail_negative() {
        // 異常系のテスト (マイナスは許可されない)
        Result<PositiveInt> result = PositiveInt.of(-1);
        assertThat(result.isFailure()).isTrue();
    }
}